package pw.saber.blocktop.util;

import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.concurrent.ForkJoinPool;

public final class CaffeineFactory {

  private static final ForkJoinPool LOADER_POOL = new ForkJoinPool();

  private CaffeineFactory() {
    throw new AssertionError();
  }

  public static Caffeine<Object, Object> newBuilder() {
    return Caffeine.newBuilder().executor(LOADER_POOL);
  }

  public static void shutdown() {
    LOADER_POOL.shutdownNow();
  }
}
