import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;

/**
 * Created by prasanthj on 2020-06-20.
 */
public class HttpPingClient {
  public static void main(String[] args) {
    String destHostEnv = System.getenv("SERVER_HOST");
    String destPortEnv = System.getenv("SERVER_PORT");
    final String connectTimeoutEnv = System.getenv("CONNECT_TIMEOUT");
    final String readTimeoutEnv = System.getenv("READ_TIMEOUT");
    final String[] destHost = {"localhost"};
    if (destHostEnv != null && !destHostEnv.isEmpty()) {
      destHost[0] = destHostEnv;
    }
    int destPort = 80;
    if (destPortEnv != null && !destPortEnv.isEmpty()) {
      destPort = Integer.parseInt(destPortEnv);
    }
    String iterationsEnv = System.getenv("ITERATIONS");
    int iterations = 10;
    if (iterationsEnv != null && !iterationsEnv.isEmpty()) {
      iterations = Integer.parseInt(iterationsEnv);
    }
    System.setProperty("http.keepAlive", "true");
    String maxConn = System.getenv("MAX_CONNECTIONS");
    if (maxConn == null || maxConn.isEmpty()) {
      maxConn = "5";
    }
    System.setProperty("http.maxConnections", maxConn);
    String keepAlive = System.getProperty("http.keepAlive");
    maxConn = System.getProperty("http.maxConnections");
    System.out.println("keepAlive: " + keepAlive + " maxConn: " + maxConn);
    Random random = new Random(123);
    for (int i = 0; i < iterations; i++) {
      final String destHostNamePattern = System.getenv("SERVER_HOSTNAME_PATTERN");
      String serverConcurrency = System.getenv("SERVER_CONCURRENCY");
      if (serverConcurrency == null || serverConcurrency.isEmpty()) {
        serverConcurrency = "1";
      }
      final String namespace = System.getenv("NAMESPACE");
      String intervalEnv = System.getenv("INTERVAL");
      int interval = 1000;
      if (intervalEnv != null && !intervalEnv.isEmpty()) {
        interval = Integer.parseInt(intervalEnv);
      }
      int sc = Integer.parseInt(serverConcurrency);
      for (int j = 0; j < sc; j++) {
        final int finalInterval = random.nextInt(interval);
        final int finalI = i;
        final int finalJ = j;
        final int finalDestPort = destPort;
        final byte[] buf = new byte[4096];
        Thread thread = new Thread(new Runnable() {
          @Override
          public void run() {
            try {
              Thread.sleep(finalInterval);
            } catch (InterruptedException e) {
              e.printStackTrace();
            }
            final long start = System.currentTimeMillis();
            HttpURLConnection connection = null;
            try {
              destHost[0] = String.format(destHostNamePattern, finalJ, namespace);
              URL url = new URL("http", destHost[0], finalDestPort, "/ping");
              connection = (HttpURLConnection) url.openConnection();
              if (connectTimeoutEnv != null && !connectTimeoutEnv.isEmpty()) {
                connection.setConnectTimeout(Integer.parseInt(connectTimeoutEnv));
              }
              if (readTimeoutEnv != null && !readTimeoutEnv.isEmpty()) {
                connection.setReadTimeout(Integer.parseInt(readTimeoutEnv));
              }
              connection.connect();
              InputStream is = connection.getInputStream();
              while (is.read(buf) > 0) {
                processBuf(buf);
              }
              // close the inputstream
              is.close();
              int code = connection.getResponseCode();
              final long end = System.currentTimeMillis();
              System.out.println(
                "Code: " + code + " Thread: " + Thread.currentThread().getId() + " Iteration: " + finalI +
                  " Host: " + destHost[0] + ":" + finalDestPort + " Time: " + (end - start) + "ms");
            } catch (Exception e) {
              int respCode = 0;
              try {
                if (connection != null) {
                  respCode = connection.getResponseCode();
                  InputStream es = connection.getErrorStream();
                  // read the response body
                  while (es.read(buf) > 0) {
                    processBuf(buf);
                  }
                  // close the errorstream
                  es.close();
                }
              } catch(IOException ex) {
                // deal with the exception
              }
              final long end = System.currentTimeMillis();
              System.out.println(
                  "Connection to " + destHost[0] + ":" + finalDestPort + " failed in " + (end - start) + "ms! " +
                    "Code: " + respCode + " Msg: " + e.getMessage());
              // e.printStackTrace();
            } finally {
              if (connection != null) {
                connection.disconnect();
              }
            }
          }

          private void processBuf(final byte[] buf) {
            if (buf.length == 0) {
              System.err.println(buf.length);
            }
          }
        });
        thread.start();
      }
    }

    System.out.println("Done iterations!");
    while (true) {
      // so that docker image continues to run
    }
  }
}
