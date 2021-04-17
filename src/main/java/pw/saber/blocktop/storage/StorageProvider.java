package pw.saber.blocktop.storage;

import pw.saber.blocktop.NeptunTopPlugin;
import pw.saber.blocktop.config.ConfigKeys;
import pw.saber.blocktop.storage.file.FileStorage;
import pw.saber.blocktop.storage.file.loader.JsonLoader;

public class StorageProvider {
  private final NeptunTopPlugin plugin;

  public StorageProvider(NeptunTopPlugin plugin) {
    this.plugin = plugin;
  }

  public Storage getInstance() {
    Storage storage;

    StorageType type = this.plugin.getConfiguration().get(ConfigKeys.STORAGE_METHOD);
    this.plugin.getLogger().info("Loading storage provider... [" + type.name() + "]");
    storage = new Storage(this.plugin, this.createNewImplementation(type));

    storage.init();
    return storage;
  }

  private StorageImplementation createNewImplementation(StorageType method) {
    switch (method) {
      case JSON:
        return new FileStorage(this.plugin, "JSON", new JsonLoader(), ".json", "json-storage");
      default:
        throw new RuntimeException("Unknown method: " + method);
    }
  }
}