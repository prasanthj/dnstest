import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.ipc.RPC;

/**
 * Created by prasanthj on 2020-06-19.
 */
public class PingRPCServer implements PingRPC {
  @Override
  public String ping() {
    return "pong";
  }

  public static void main(String[] args) throws IOException {
    final RPC.Server server = new RPC.Builder(new Configuration()).
      setInstance(new PingRPCServer()).
      setProtocol(PingRPC.class).
      build();
    server.start();
  }
}
