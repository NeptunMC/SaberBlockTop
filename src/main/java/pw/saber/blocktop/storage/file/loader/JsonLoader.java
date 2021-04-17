package pw.saber.blocktop.storage.file.loader;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.gson.GsonConfigurationLoader;
import org.spongepowered.configurate.loader.ConfigurationLoader;

public class JsonLoader implements ConfigurateLoader {
  @Override
  public ConfigurationLoader<? extends ConfigurationNode> loader(Path path) {
    return GsonConfigurationLoader.builder()
        .indent(2)
        .source(() -> Files.newBufferedReader(path, StandardCharsets.UTF_8))
        .sink(() -> Files.newBufferedWriter(path, StandardCharsets.UTF_8))
        .build();
  }
}