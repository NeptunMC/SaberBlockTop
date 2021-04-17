package pw.saber.blocktop.storage.file.loader;

import java.nio.file.Path;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.loader.ConfigurationLoader;

public interface ConfigurateLoader {

  ConfigurationLoader<? extends ConfigurationNode> loader(Path path);
}