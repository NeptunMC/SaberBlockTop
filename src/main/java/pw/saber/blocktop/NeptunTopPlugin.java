package pw.saber.blocktop;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import pw.saber.blocktop.commands.CmdBlockTop;
import pw.saber.blocktop.config.ConfigKeys;
import pw.saber.blocktop.config.KeyedConfiguration;
import pw.saber.blocktop.config.adapter.BukkitConfigAdapter;
import pw.saber.blocktop.config.adapter.ConfigurationAdapter;
import pw.saber.blocktop.gui.BlockGUI;
import pw.saber.blocktop.listener.BreakListener;
import pw.saber.blocktop.listener.ConnectionListener;
import pw.saber.blocktop.logger.JavaPluginLogger;
import pw.saber.blocktop.logger.PluginLogger;
import pw.saber.blocktop.scheduler.AbstractSchedulerAdapter;
import pw.saber.blocktop.scheduler.SchedulerAdapter;
import pw.saber.blocktop.storage.Storage;
import pw.saber.blocktop.storage.StorageProvider;
import pw.saber.blocktop.tasks.SyncTask;
import pw.saber.blocktop.user.UserManager;
import pw.saber.blocktop.util.CaffeineFactory;

public final class NeptunTopPlugin extends JavaPlugin {
  private final SchedulerAdapterImpl scheduler;
  private final JavaPluginLogger logger;
  private final CountDownLatch enableLatch = new CountDownLatch(1);
  private Configuration configuration;
  private Storage storage;
  private UserManager userManager;
  private SyncTask.Buffer syncTaskBuffer;
  private ConnectionListener connectionListener;

  public NeptunTopPlugin() {
    this.logger = new JavaPluginLogger(this.getLogger());
    this.scheduler = new SchedulerAdapterImpl(this);
  }

  @SuppressWarnings({"SameParameterValue", "ConstantConditions"})
  private Path resolveConfig(String fileName) {
    Path configFile = this.getDataDirectory().resolve(fileName);
    if (!Files.exists(configFile)) {
      final Path parent = configFile.getParent();
      if (!Files.exists(parent)) {
        try {
          Files.createDirectories(parent);
        } catch (IOException e) {
          // ignore
        }
      }

      try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(fileName)) {
        Files.copy(is, configFile);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    return configFile;
  }

  private void enable() {
    File configFile = this.resolveConfig("config.yml").toFile();
    BukkitConfigAdapter configAdapter = new BukkitConfigAdapter(this, configFile);
    this.configuration = new Configuration(configAdapter);

    StorageProvider storageFactory = new StorageProvider(this);
    this.storage = storageFactory.getInstance();

    this.connectionListener = new ConnectionListener(this);
    this.getServer().getPluginManager().registerEvents(this.connectionListener, this);

    this.syncTaskBuffer = new SyncTask.Buffer(this);

    this.getScheduler().asyncRepeating(() -> this.syncTaskBuffer.request(), 1, TimeUnit.MINUTES);

    this.getLogger().info("Performing initial data load...");
    try {
      new SyncTask(this).run();
    } catch (Exception e) {
      e.printStackTrace();
    }

    for (Player player : this.getServer().getOnlinePlayers()) {
      this.scheduler.executeAsync(
          () -> {
            try {
              this.connectionListener.loadUser(player.getUniqueId(), player.getName());
            } catch (Exception e) {
              this.getPluginLogger()
                  .severe(
                      "Exception occurred whilst loading data for "
                          + player.getUniqueId()
                          + " - "
                          + player.getName(),
                      e);
            }
          });
    }

  }

  @Override
  public void onEnable() {
    try {
      this.enable();
    } finally {
        this.enableLatch.countDown();
    }
  }

  @Override
  public void onDisable() {
    this.scheduler.shutdownScheduler();

    this.storage.shutdown();

    this.scheduler.shutdownExecutor();
    CaffeineFactory.shutdown();
  }

  public boolean isPlayerOnline(UUID uuid) {
    final Player player = this.getServer().getPlayer(uuid);
    return player != null && player.isOnline();
  }

  public Collection<UUID> getOnlinePlayers() {
    Collection<? extends Player> players = this.getServer().getOnlinePlayers();
    List<UUID> list = new ArrayList<>(players.size());
    for (Player player : players) {
      list.add(player.getUniqueId());
    }
    return list;
  }

  public Path getDataDirectory() {
    return this.getDataFolder().toPath().toAbsolutePath();
  }

  public SchedulerAdapter getScheduler() {
    return this.scheduler;
  }

  public PluginLogger getPluginLogger() {
    return this.logger;
  }

  public UserManager getUserManager() {
    return this.userManager;
  }

  public CountDownLatch getEnableLatch() {
    return this.enableLatch;
  }

  public Storage getStorage() {
    return this.storage;
  }

  public KeyedConfiguration getConfiguration() {
    return this.configuration;
  }

  static class Configuration extends KeyedConfiguration {
    Configuration(ConfigurationAdapter adapter) {
      super(adapter, ConfigKeys.getKeys());
      this.init();
    }
  }

  static class SchedulerAdapterImpl extends AbstractSchedulerAdapter {
    final Executor sync;

    SchedulerAdapterImpl(NeptunTopPlugin plugin) {
      this.sync = r -> plugin.getServer().getScheduler().runTask(plugin, r);
    }

    @Override
    public Executor sync() {
      return this.sync;
    }
  }

}
