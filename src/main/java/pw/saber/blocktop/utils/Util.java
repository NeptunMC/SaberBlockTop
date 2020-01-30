package pw.saber.blocktop.utils;

import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Util {

    public static List<PlayerObject> playerBlock = new ArrayList<>();

    public static String color(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static List<String> color(List<String> messages) {
        List<String> list = new ArrayList<>();
        for (String line : messages) {
            list.add(color(line));
        }
        return list;
    }

    public static PlayerObject getPlayerObject(UUID uuid) {
        for (PlayerObject object : playerBlock) {
            if (object.getUuid().equals(uuid)) {
                return object;
            }
        }
        return null;
    }

    public static void savePlayerObjects() {
        List<String> dataList = new ArrayList<>();
        for (PlayerObject object : playerBlock) {
            UUID uuid = object.getUuid();
            int blocks = object.getBlockBroke();

            String built = uuid.toString() + ":" + blocks;

            dataList.add(built);
        }
        Configs.d.set("data", dataList);
        Configs.saveData();
    }

    public static void loadPlayerObjects() {
        List<String> data = Configs.getData().getStringList("data");

        for (String d : data) {
            String[] l = d.split(":");
            UUID uuid = UUID.fromString(l[0]);
            int blocks = Integer.parseInt(l[1]);
            PlayerObject object = new PlayerObject(uuid, blocks);
            playerBlock.add(object);
        }
    }


}
