package pw.saber.blocktop.config.adapter;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import pw.saber.blocktop.NeptunTopPlugin;

public class BukkitConfigAdapter implements ConfigurationAdapter {
  private final NeptunTopPlugin plugin;
  private final File file;
  private YamlConfiguration configuration;

  public BukkitConfigAdapter(NeptunTopPlugin plugin, File file) {
    this.plugin = plugin;
    this.file = file;
    this.reload();
  }

  @Override
  public void reload() {
    this.configuration = YamlConfiguration.loadConfiguration(this.file);
  }

  @Override
  public String getString(String path, String def) {
    return this.configuration.getString(path, def);
  }

  @Override
  public int getInteger(String path, int def) {
    return this.configuration.getInt(path, def);
  }

  @Override
  public boolean getBoolean(String path, boolean def) {
    return this.configuration.getBoolean(path, def);
  }

  @Override
  public List<String> getStringList(String path, List<String> def) {
    List<String> list = this.configuration.getStringList(path);
    return this.configuration.isSet(path) ? list : def;
  }

  @Override
  public List<String> getKeys(String path, List<String> def) {
    ConfigurationSection section = this.configuration.getConfigurationSection(path);
    if (section == null) {
      return def;
    }

    Set<String> keys = section.getKeys(false);
    return keys == null ? def : new ArrayList<>(keys);
  }

  @Override
  public Map<String, String> getStringMap(String path, Map<String, String> def) {
    Map<String, String> map = new HashMap<>();
    ConfigurationSection section = this.configuration.getConfigurationSection(path);
    if (section == null) {
      return def;
    }

    for (String key : section.getKeys(false)) {
      map.put(key, section.getString(key));
    }

    return map;
  }

  @Override
  public NeptunTopPlugin getPlugin() {
    return this.plugin;
  }
}