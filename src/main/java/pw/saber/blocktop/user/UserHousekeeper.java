package pw.saber.blocktop.user;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import pw.saber.blocktop.NeptunTopPlugin;
import pw.saber.blocktop.util.ExpiringSet;

public class UserHousekeeper implements Runnable {

  private final NeptunTopPlugin plugin;
  private final UserManager userManager;

  // contains the uuids of users who have recently logged in / out
  private final ExpiringSet<UUID> recentlyUsed;

  public UserHousekeeper(
      NeptunTopPlugin plugin, UserManager userManager, TimeoutSettings timeoutSettings) {
    this.plugin = plugin;
    this.userManager = userManager;
    this.recentlyUsed = new ExpiringSet<>(timeoutSettings.duration, timeoutSettings.unit);
  }

  public static TimeoutSettings timeoutSettings(long duration, TimeUnit unit) {
    return new TimeoutSettings(duration, unit);
  }

  // called when a player attempts a connection or logs out
  public void registerUsage(UUID uuid) {
    this.recentlyUsed.add(uuid);
  }

  @Override
  public void run() {
    for (UUID entry : this.userManager.getAll().keySet()) {
      this.cleanup(entry);
    }
  }

  public void cleanup(UUID uuid) {
    // unload users which aren't online and who haven't been online (or tried to login) recently
    if (this.recentlyUsed.contains(uuid) || this.plugin.isPlayerOnline(uuid)) {
      return;
    }

    User user = this.userManager.getIfLoaded(uuid);
    if (user == null) {
      return;
    }

    this.userManager.unload(uuid);
  }

  public static final class TimeoutSettings {

    private final long duration;
    private final TimeUnit unit;

    TimeoutSettings(long duration, TimeUnit unit) {
      this.duration = duration;
      this.unit = unit;
    }
  }
}
