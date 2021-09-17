import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.ipc.RPC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by prasanthj on 2020-06-19.
 */
public class PingRPCClient {
  public static final Logger LOG = LoggerFactory.getLogger(PingRPCClient.class);
  public static AtomicLong unknownHostsCount = new AtomicLong(0);
  public static AtomicLong connectionResetCount = new AtomicLong(0);
  public static AtomicLong ioExceptionCount = new AtomicLong(0);
  public static AtomicLong otherExceptionCount = new AtomicLong(0);
  public static void main(String[] args) throws InterruptedException {
    String destHostEnv = System.getenv("SERVER_HOST");
    String destPortEnv = System.getenv("SERVER_PORT");
    final String[] destHost = {"localhost"};
    if (destHostEnv != null && !destHostEnv.isEmpty()) {
      destHost[0] = destHostEnv;
    }
    String initialSleepStr = System.getenv("INITIAL_SLEEP");
    if (initialSleepStr != null && !initialSleepStr.isEmpty()) {
      long initialSleep = Long.parseLong(initialSleepStr);
      LOG.info("Initial sleep for " + initialSleep + "s");
      Thread.sleep(initialSleep * 1000L);
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

    startScheduledExecutor();
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
        final int finalJ = j;
        final int finalDestPort = destPort;
        Thread thread = new Thread(new Runnable() {
          @Override
          public void run() {
            try {
              LOG.info("Sleeping " + finalInterval + "ms before ping..");
              Thread.sleep(finalInterval);
            } catch (InterruptedException e) {
              e.printStackTrace();
            }
            try {
              destHost[0] = String.format(destHostNamePattern, finalJ, namespace);
              InetSocketAddress inetAddress = new InetSocketAddress(destHost[0], finalDestPort);
              PingRPC ping = RPC.getProxy(PingRPC.class,
                RPC.getProtocolVersion(PingRPC.class),
                inetAddress, new Configuration());
//              LOG.info("Pinging host: " + destHost[0] + ":" + finalDestPort);
              LOG.info("Ping host: " + destHost[0] + ":" + finalDestPort + " returned -> " + ping.ping());
            } catch (Exception e) {
              LOG.debug("", e);
              if (ExceptionUtils.getRootCause(e) instanceof UnknownHostException) {
                long count = unknownHostsCount.incrementAndGet();
                LOG.warn("#UnknownHostExceptions: " + count);
              } else if (ExceptionUtils.getRootCause(e) instanceof IOException) {
                if (ExceptionUtils.getRootCauseMessage(e).contains("Connection reset by peer")) {
                  long count = connectionResetCount.incrementAndGet();
                  LOG.info("#ConnectionResets: " + count);
                } else {
                  long count = ioExceptionCount.incrementAndGet();
                  LOG.info("#IOExceptions: " + count);
                }
              } else {
                long count = otherExceptionCount.incrementAndGet();
                LOG.info("#OtherExceptions: " + count);
              }
            }
          }
        });
        thread.start();
      }
    }

    LOG.info("Submitted all iterations!");
    // so that docker image continues to run
    Thread.sleep(1_000_000_000L);
  }

  private static void startScheduledExecutor() {
    ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
    executor.scheduleAtFixedRate(new Runnable() {
      @Override
      public void run() {
        LOG.warn("#UnknownHostExceptions: " + unknownHostsCount.get() + " #ConnectionResets: " + connectionResetCount.get()
                + " #IOExceptions: " + ioExceptionCount.get() + " #OtherExceptions: " + otherExceptionCount.get());
      }
    }, 0,10, TimeUnit.SECONDS);
  }
}
