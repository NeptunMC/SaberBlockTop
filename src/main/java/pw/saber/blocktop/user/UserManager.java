package pw.saber.blocktop.user;

import com.google.common.collect.ImmutableMap;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import pw.saber.blocktop.NeptunTopPlugin;
import pw.saber.blocktop.util.LoadingMap;

public class UserManager implements Function<UUID, User> {
  private final LoadingMap<UUID, User> objects = LoadingMap.of(this);
  private final NeptunTopPlugin plugin;
  private final UserHousekeeper housekeeper;

  public UserManager(NeptunTopPlugin plugin) {
    this.plugin = plugin;
    this.housekeeper =
        new UserHousekeeper(plugin, this, UserHousekeeper.timeoutSettings(1, TimeUnit.MINUTES));
    this.plugin.getScheduler().asyncRepeating(this.housekeeper, 30, TimeUnit.SECONDS);
  }

  public UserHousekeeper getHouseKeeper() {
    return this.housekeeper;
  }

  public Map<UUID, User> getAll() {
    return ImmutableMap.copyOf(this.objects);
  }

  public User getOrMake(UUID uuid) {
    return this.objects.get(uuid);
  }

  public User getIfLoaded(UUID uuid) {
    return this.objects.getIfPresent(uuid);
  }

  public boolean isLoaded(UUID uuid) {
    return this.objects.containsKey(uuid);
  }

  public void unload(UUID uuid) {
    this.objects.remove(uuid);
  }

  public void retainAll(Collection<UUID> ids) {
    this.objects.keySet().stream().filter(g -> !ids.contains(g)).forEach(this::unload);
  }

  public boolean giveDefaultIfNeeded(User user) {
    boolean requireSave = false;
    return requireSave;
  }

  public boolean isNonDefaultUser(User user) {
    if (user.logsBroken().get() != 0) {
      return false;
    }

    if (user.fishesFished().get() != 0) {
      return false;
    }

    return true;
  }

  public boolean isDefaultUser(User user) {
    return !this.isNonDefaultUser(user);
  }

  public CompletableFuture<Void> loadAllUsers() {
    Set<UUID> ids = new HashSet<>(this.getAll().keySet());
    ids.addAll(this.plugin.getOnlinePlayers());

    CompletableFuture<?>[] loadTasks =
        ids.stream()
            .map(id -> this.plugin.getStorage().loadUser(id))
            .toArray(CompletableFuture[]::new);

    return CompletableFuture.allOf(loadTasks);
  }

  @Override
  public User apply(UUID uuid) {
    return new User(uuid);
  }
}
