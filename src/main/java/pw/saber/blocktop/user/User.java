package pw.saber.blocktop.user;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.checkerframework.checker.units.qual.A;

public class User {
  private final UUID uuid;
  private final AtomicInteger fishesFished = new AtomicInteger();
  private final AtomicInteger logsBroken = new AtomicInteger();

  public User(UUID uuid) {
    this.uuid = uuid;
  }

  public UUID getUniqueId() {
    return this.uuid;
  }

  public AtomicInteger fishesFished() {
    return this.fishesFished;
  }

  public AtomicInteger logsBroken() {
    return this.logsBroken;
  }

  public void reset() {
    this.logsBroken.set(0);
    this.fishesFished.set(0);
  }
}