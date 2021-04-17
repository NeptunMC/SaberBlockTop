package pw.saber.blocktop.config;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import pw.saber.blocktop.config.adapter.ConfigurationAdapter;
import pw.saber.blocktop.config.key.ConfigKey;
import pw.saber.blocktop.config.key.SimpleConfigKey;
import pw.saber.blocktop.util.ImmutableCollectors;

public class KeyedConfiguration {
  private final ConfigurationAdapter adapter;
  private final List<? extends ConfigKey<?>> keys;
  private final ValuesMap values;

  public KeyedConfiguration(ConfigurationAdapter adapter, List<? extends ConfigKey<?>> keys) {
    this.adapter = adapter;
    this.keys = keys;
    this.values = new ValuesMap(keys.size());
  }

  public static List<SimpleConfigKey<?>> initialise(Class<?> keysClass) {
    // get a list of all keys
    List<SimpleConfigKey<?>> keys =
        Arrays.stream(keysClass.getFields())
            .filter(f -> Modifier.isStatic(f.getModifiers()))
            .filter(f -> ConfigKey.class.equals(f.getType()))
            .map(
                f -> {
                  try {
                    return (SimpleConfigKey<?>) f.get(null);
                  } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                  }
                })
            .collect(ImmutableCollectors.toList());

    // set ordinal values
    for (int i = 0; i < keys.size(); i++) {
      keys.get(i).setOrdinal(i);
    }

    return keys;
  }

  protected void init() {
    this.load(true);
  }

  public <T> T get(ConfigKey<T> key) {
    return this.values.get(key);
  }

  protected void load(boolean initial) {
    for (ConfigKey<?> key : this.keys) {
      if (initial || key.reloadable()) {
        this.values.put(key, key.get(this.adapter));
      }
    }
  }

  public void reload() {
    this.adapter.reload();
    this.load(false);
  }

  public static class ValuesMap {
    private final Object[] values;

    public ValuesMap(int size) {
      this.values = new Object[size];
    }

    @SuppressWarnings("unchecked")
    public <T> T get(ConfigKey<T> key) {
      return (T) this.values[key.ordinal()];
    }

    public void put(ConfigKey<?> key, Object value) {
      this.values[key.ordinal()] = value;
    }
  }
}