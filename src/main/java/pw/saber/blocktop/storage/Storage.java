package pw.saber.blocktop.storage;

import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import pw.saber.blocktop.NeptunTopPlugin;
import pw.saber.blocktop.user.User;
import pw.saber.blocktop.util.Throwing;

@SuppressWarnings("CodeBlock2Expr")
public class Storage {
  private final NeptunTopPlugin plugin;
  private final StorageImplementation implementation;

  public Storage(NeptunTopPlugin plugin, StorageImplementation implementation) {
    this.plugin = plugin;
    this.implementation = implementation;
  }

  private <T> CompletableFuture<T> future(Callable<T> supplier) {
    return CompletableFuture.supplyAsync(
        () -> {
          try {
            return supplier.call();
          } catch (Exception e) {
            if (e instanceof RuntimeException) {
              throw (RuntimeException) e;
            }
            throw new CompletionException(e);
          }
        },
        this.plugin.getScheduler().async());
  }

  private CompletableFuture<Void> future(Throwing.Runnable runnable) {
    return CompletableFuture.runAsync(
        () -> {
          try {
            runnable.run();
          } catch (Exception e) {
            if (e instanceof RuntimeException) {
              throw (RuntimeException) e;
            }
            throw new CompletionException(e);
          }
        },
        this.plugin.getScheduler().async());
  }

  public StorageImplementation getImplementation() {
    return this.implementation;
  }

  public void init() {
    try {
      this.implementation.init();
    } catch (Exception e) {
      this.plugin.getPluginLogger().severe("Failed to init storage implementation", e);
    }
  }

  public void shutdown() {
    try {
      this.implementation.shutdown();
    } catch (Exception e) {
      this.plugin.getPluginLogger().severe("Failed to shutdown storage implementation", e);
    }
  }

  public CompletableFuture<User> loadUser(UUID uniqueId) {
    return this.future(
        () -> {
          return this.implementation.loadUser(uniqueId);
        });
  }

  public CompletableFuture<Void> saveUser(User user) {
    return this.future(() -> this.implementation.saveUser(user));
  }
}