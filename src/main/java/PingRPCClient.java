import java.io.IOException;
import java.net.InetSocketAddress;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.ipc.RPC;

/**
 * Created by prasanthj on 2020-06-19.
 */
public class PingRPCClient {
  public static void main(String[] args) throws IOException {
    String serverAddress = args[0];
    int port = Integer.parseInt(args[1]);
    InetSocketAddress inetAddress = InetSocketAddress.createUnresolved(serverAddress, port);
    PingRPC ping = RPC.getProxy(PingRPC.class,
      RPC.getProtocolVersion(PingRPC.class),
      inetAddress, new Configuration());
    System.out.println("Ping host: " + serverAddress + ":" + port + " returned -> " + ping.ping());
  }
}
