package pw.saber.blocktop.config;

import static pw.saber.blocktop.config.key.ConfigKeyFactory.key;
import static pw.saber.blocktop.config.key.ConfigKeyFactory.notReloadable;

import java.util.List;
import pw.saber.blocktop.config.key.ConfigKey;
import pw.saber.blocktop.config.key.SimpleConfigKey;
import pw.saber.blocktop.storage.StorageType;

@SuppressWarnings("CodeBlock2Expr")
public final class ConfigKeys {
  public static final ConfigKey<StorageType> STORAGE_METHOD =
      notReloadable(
          key(
              c -> {
                return StorageType.parse(c.getString("storage-method", "json"), StorageType.JSON);
              }));
  private static final List<SimpleConfigKey<?>> KEYS =
      KeyedConfiguration.initialise(ConfigKeys.class);

  private ConfigKeys() {
    throw new AssertionError();
  }

  public static List<? extends ConfigKey<?>> getKeys() {
    return KEYS;
  }
}