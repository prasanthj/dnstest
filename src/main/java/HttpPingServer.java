import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.List;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

/**
 * Created by prasanthj on 2020-06-20.
 */
public class HttpPingServer {
  public static void main(String[] args) throws Exception {
    String portEnv = System.getenv("HTTP_PORT");
    int port = 80;
    if (portEnv != null && !portEnv.isEmpty()) {
      port = Integer.parseInt(portEnv);
    }
    System.setProperty("http.keepAlive", "true");
    System.setProperty("http.maxConnections", System.getenv("MAX_CONNECTIONS"));
    String keepAlive = System.getProperty("http.keepAlive");
    String maxConn = System.getProperty("http.maxConnections");
    System.out.println("keepAlive: " + keepAlive + " maxConn: " + maxConn);
    InetSocketAddress inetSocketAddress = new InetSocketAddress(port);
    HttpServer server = HttpServer.create(inetSocketAddress, 0);
    server.createContext("/ping", new PingHandler());
    server.setExecutor(null); // creates a default executor
    System.out.println("Starting http ping server in port: " + port + " hostname: " + InetAddress.getLocalHost().getHostName() +
      " hostIp: " + InetAddress.getLocalHost().getHostAddress());
    server.start();
  }

  static class PingHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange t) throws IOException {
      List<String> forwardedFors = t.getRequestHeaders().get("X-Forwarded-For");
      String remoteHost = t.getRemoteAddress().getHostName();
      if (forwardedFors != null && !forwardedFors.isEmpty()) {
        remoteHost += "[" + forwardedFors.toString() + "]";
      }
      String response = "pong. Hello " + remoteHost + "!";
      t.sendResponseHeaders(200, response.length());
      OutputStream os = t.getResponseBody();
      os.write(response.getBytes());
      os.close();
    }
  }
}
