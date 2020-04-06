package pw.saber.blocktop.utils;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class Configs {
    public static File data = new File("plugins/SaberBlockTop/data.yml");
    public static FileConfiguration d = YamlConfiguration.loadConfiguration(data);

    public static FileConfiguration getData() {
        return d;
    }

    public static void loadData() { d = YamlConfiguration.loadConfiguration(data); }

    public static void saveData() {
        try {
            getData().save(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void setup() {
        if (!data.exists()) {
            try {
                data.createNewFile();
                getData().set("data", new ArrayList<>());
                saveData();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
