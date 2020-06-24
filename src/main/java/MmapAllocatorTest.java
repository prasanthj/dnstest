import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Arrays;
import java.util.Set;

/**
 * Created by prasanthj on 2020-05-15.
 */
public class MmapAllocatorTest {
  private static final FileAttribute<Set<PosixFilePermission>> RWX = PosixFilePermissions
    .asFileAttribute(PosixFilePermissions.fromString("rwx------"));
  private static ByteBuffer preallocateArenaBuffer(Path cacheDir, boolean isMapped, boolean isDirect, int arenaSize) {
    if (isMapped) {
      RandomAccessFile rwf = null;
      File rf = null;
      try {
        rf = File.createTempFile("arena-", ".cache", cacheDir.toFile());
        rwf = new RandomAccessFile(rf, "rw");
        rwf.setLength(arenaSize); // truncate (TODO: posix_fallocate?)
        // Use RW, not PRIVATE because the copy-on-write is irrelevant for a deleted file
        // see discussion in YARN-5551 for the memory accounting discussion
        System.out.println("Allocating arena buffer at " + rf.getAbsolutePath() + " arenaSize: " + arenaSize);
        return rwf.getChannel().map(FileChannel.MapMode.READ_WRITE, 0, arenaSize);
      } catch (IOException ioe) {
        ioe.printStackTrace();
        // fail similarly when memory allocations fail
        throw new OutOfMemoryError("Failed trying to allocate memory mapped arena: " + ioe.getMessage());
      } finally {
        // A mapping, once established, is not dependent upon the file channel that was used to
        // create it. delete file and hold onto the map
        try {
          if (rwf != null) {
            rwf.close();
          }
        } catch (IOException e) {
          e.printStackTrace();
        }
        if (rf != null) {
          System.out.println("Deleting file: " + rf.getAbsolutePath());
          rf.delete();
        }
      }
    }
    return isDirect ? ByteBuffer.allocateDirect(arenaSize) : ByteBuffer.allocate(arenaSize);
  }

  public static void main(String[] args) throws Exception {
    int initCount = args.length > 0 ? Integer.parseInt(args[0]) : 200;
//    String cacheDir = args.length > 1 ? args[1] : "/apps/llap/work/";
    String cacheDir = args.length > 1 ? args[1] : "/tmp";
    boolean isDirect = true;
    boolean isMapped = true;
    int arenaSize = 1 << 30;
    Path path = FileSystems.getDefault().getPath(cacheDir);
    if (!Files.exists(path)) {
      Files.createDirectory(path);
    }
    Path cacheDirPath = Files.createTempDirectory(path, "llap-", RWX);
    for (int i = 0; i < initCount; ++i) {
      System.out.println("Arena: " + i + " isMapped: " + isMapped + " isDirect: " + isDirect + " arenaSize: " + arenaSize);
      preallocateArenaBuffer(cacheDirPath, isMapped, isDirect, arenaSize);
//      ByteBuffer allocatedBuffer = preallocateArenaBuffer(cacheDirPath, isMapped, isDirect, arenaSize);
//      byte[] randomData = new byte[16 * 1024 * 1024];
//      Arrays.fill(randomData, (byte) 1);
//      allocatedBuffer.put(randomData);
    }
  }
}
