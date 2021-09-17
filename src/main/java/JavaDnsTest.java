import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.Security;
import java.sql.DriverManager;

/**
 * Created by prasanthj on 2020-02-14.
 */
public class JavaDnsTest {
  public static void main(String[] args) throws UnknownHostException, InterruptedException {
    String targetHost = args.length > 0 ? args[0] : "www.google.com";
    System.out.println("Java DNS cache TTL: " + Security.getProperty("networkaddress.cache.ttl"));
    while(true) {
      InetAddress inetAddress = InetAddress.getByName(targetHost);
      System.out.println("TargetHost: " + targetHost + " CanonicalHostName: " + inetAddress.getCanonicalHostName() +
        " HostName: " + inetAddress.getHostName() + " HostAddress: " + inetAddress.getHostAddress() + " Sleeping 5s..");
      Thread.sleep(5000);
    }
  }
}
