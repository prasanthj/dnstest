import java.io.IOException;
import java.net.InetAddress;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.ipc.RPC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by prasanthj on 2020-06-19.
 */
public class PingRPCServer implements PingRPC {
  public static final Logger LOG = LoggerFactory.getLogger(PingRPCServer.class);
  @Override
  public String ping() {
    return "pong";
  }

  public static void main(String[] args) throws IOException {
    String destPortEnv = System.getenv("SERVER_PORT");
    int destPort = 9999;
    if (destPortEnv != null && !destPortEnv.isEmpty()) {
      destPort = Integer.parseInt(destPortEnv);
    }
    final RPC.Server server = new RPC.Builder(new Configuration()).
      setInstance(new PingRPCServer()).
      setProtocol(PingRPC.class).
      setPort(destPort).
      build();
    LOG.info("Starting ipc ping server in port: " + destPort + " hostname: " + InetAddress.getLocalHost().getHostName() +
      " hostIp: " + InetAddress.getLocalHost().getHostAddress());
    server.start();
  }
}
