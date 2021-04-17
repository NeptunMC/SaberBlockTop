package pw.saber.blocktop.util;

import com.github.benmanes.caffeine.cache.Cache;
import com.google.common.collect.ForwardingSet;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.checkerframework.checker.nullness.qual.NonNull;

public class ExpiringSet<E> extends ForwardingSet<E> {

  private final Set<E> setView;

  public ExpiringSet(long duration, TimeUnit unit) {
    Cache<@NonNull E, @NonNull Boolean> cache =
        CaffeineFactory.newBuilder().expireAfterAccess(duration, unit).build();
    this.setView = Collections.newSetFromMap(cache.asMap());
  }

  @Override
  protected Set<E> delegate() {
    return this.setView;
  }
}