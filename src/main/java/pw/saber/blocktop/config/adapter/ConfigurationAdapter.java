package pw.saber.blocktop.config.adapter;

import java.util.List;
import java.util.Map;
import pw.saber.blocktop.NeptunTopPlugin;

public interface ConfigurationAdapter {
  NeptunTopPlugin getPlugin();

  void reload();

  String getString(String path, String def);

  int getInteger(String path, int def);

  boolean getBoolean(String path, boolean def);

  List<String> getStringList(String path, List<String> def);

  List<String> getKeys(String path, List<String> def);

  Map<String, String> getStringMap(String path, Map<String, String> def);
}
