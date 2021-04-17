package pw.saber.blocktop.storage.file;

import com.github.benmanes.caffeine.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import org.spongepowered.configurate.BasicConfigurationNode;
import org.spongepowered.configurate.ConfigurationNode;
import pw.saber.blocktop.NeptunTopPlugin;
import pw.saber.blocktop.storage.StorageImplementation;
import pw.saber.blocktop.storage.file.loader.ConfigurateLoader;
import pw.saber.blocktop.storage.file.loader.JsonLoader;
import pw.saber.blocktop.user.User;
import pw.saber.blocktop.util.CaffeineFactory;
import pw.saber.blocktop.util.MoreFiles;

public class FileStorage implements StorageImplementation {
  private final String fileExtension;
  private final Map<StorageLocation, FileGroup> fileGroups;
  private final FileGroup users;
  private final FileGroup miscellanous;
  private final NeptunTopPlugin plugin;
  private final String dataFolderName;
  private final String implementationName;
  private final ConfigurateLoader loader;
  private final LoadingCache<Path, ReentrantLock> ioLocks;

  public FileStorage(
      NeptunTopPlugin plugin,
      String implementationName,
      ConfigurateLoader loader,
      String fileExtension,
      String dataFolderName) {
    this.plugin = plugin;
    this.dataFolderName = dataFolderName;
    this.implementationName = implementationName;
    this.loader = loader;
    this.fileExtension = fileExtension;

    this.users = new FileGroup();
    this.miscellanous = new FileGroup();

    EnumMap<StorageLocation, FileGroup> fileGroups = new EnumMap<>(StorageLocation.class);
    fileGroups.put(StorageLocation.USERS, this.users);
    this.fileGroups = ImmutableMap.copyOf(fileGroups);

    this.ioLocks =
        CaffeineFactory.newBuilder()
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .build(key -> new ReentrantLock());
  }

  private Path getDirectory(StorageLocation location) {
    return this.fileGroups.get(location).directory;
  }

  protected ConfigurationNode readFile(StorageLocation location, String name) throws IOException {
    Path file = this.getDirectory(location).resolve(name + this.fileExtension);
    return this.readFile(file);
  }

  private ConfigurationNode readFile(Path file) throws IOException {
    ReentrantLock lock = Objects.requireNonNull(this.ioLocks.get(file));
    lock.lock();
    try {
      if (!Files.exists(file)) {
        return null;
      }

      return this.loader.loader(file).load();
    } finally {
      lock.unlock();
    }
  }

  protected void deleteFile(StorageLocation location, String name) throws IOException {
    this.saveFile(location, name, null);
  }

  protected void saveFile(StorageLocation location, String name, ConfigurationNode node)
      throws IOException {
    Path file = this.getDirectory(location).resolve(name + this.fileExtension);
    this.saveFile(file, node);
  }

  private void saveFile(Path file, ConfigurationNode node) throws IOException {
    ReentrantLock lock = Objects.requireNonNull(this.ioLocks.get(file));
    lock.lock();
    try {
      if (node == null) {
        Files.deleteIfExists(file);
        return;
      }

      this.loader.loader(file).save(node);
    } finally {
      lock.unlock();
    }
  }

  @Override
  public String getImplementationName() {
    return this.implementationName;
  }

  @Override
  public void init() throws IOException {
    Path dataDirectory = this.plugin.getDataDirectory().resolve(this.dataFolderName);
    MoreFiles.createDirectoriesIfNotExists(dataDirectory);

    this.users.directory = MoreFiles.createDirectoryIfNotExists(dataDirectory.resolve("users"));
    this.miscellanous.directory =
        MoreFiles.createDirectoryIfNotExists(dataDirectory.resolve("miscellanous"));
  }

  @Override
  public void shutdown() {}

  @Override
  public User loadUser(UUID uuid) throws IOException {
    User user = this.plugin.getUserManager().getOrMake(uuid);
    try {
      ConfigurationNode file = this.readFile(StorageLocation.USERS, user.toString());
      if (file != null) {
        final boolean isJsonLoader = this.loader instanceof JsonLoader;
        final int logsBroken = file.node(isJsonLoader ? "logsBroken" : "logs-broken").getInt();
        final int fishesFished = file.node(isJsonLoader ? "fishesFished" : "fishes-fished").getInt();

        user.logsBroken().set(logsBroken);
        user.fishesFished().set(fishesFished);

        this.plugin.getUserManager().giveDefaultIfNeeded(user);
      } else {
        if (this.plugin.getUserManager().isNonDefaultUser(user)) {
          user.reset();
          this.plugin.getUserManager().giveDefaultIfNeeded(user);
        }
      }
    } catch (Exception e) {
      throw new FileIOException(uuid.toString(), e);
    }
    return user;
  }

  @Override
  public void saveUser(User user) throws IOException {
    try {
      if (this.plugin.getUserManager().isDefaultUser(user)) {
        this.deleteFile(StorageLocation.USERS, user.getUniqueId().toString());
      } else {
        ConfigurationNode file = BasicConfigurationNode.root();

        final boolean isJsonLoader = this.loader instanceof JsonLoader;
        final int logsBroken = user.logsBroken().get();
        final int fishesFished = user.fishesFished().get();

        file.node(isJsonLoader ? "logsBroken" : "logs-broken").set(logsBroken);
        file.node(isJsonLoader ? "fishesFinished" : "fished-finished").set(fishesFished);

        this.saveFile(StorageLocation.USERS, user.getUniqueId().toString(), file);
      }
    } catch (Exception e) {
      throw new FileIOException(user.getUniqueId().toString(), e);
    }
  }

  private static final class FileGroup {
    private Path directory;
  }
}