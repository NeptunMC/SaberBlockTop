package pw.saber.blocktop.config.key;

import pw.saber.blocktop.config.adapter.ConfigurationAdapter;

public interface ConfigKey<T> {
  int ordinal();

  boolean reloadable();

  T get(ConfigurationAdapter adapter);
}