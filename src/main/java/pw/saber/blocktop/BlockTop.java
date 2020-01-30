package pw.saber.blocktop;

import pw.saber.blocktop.commands.CmdBlockTop;
import pw.saber.blocktop.gui.BlockGUI;
import pw.saber.blocktop.listener.BreakListener;
import pw.saber.blocktop.utils.Configs;
import pw.saber.blocktop.utils.Util;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class BlockTop extends JavaPlugin {

    public static BlockTop instance;

    public static BlockTop getInstance() {
        return instance;
    }

    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        getConfig().options().copyDefaults(true);

        //setup the data.yml file
        Configs.setup();
        getServer().getPluginManager().registerEvents(new BreakListener(), this);
        getServer().getPluginManager().registerEvents(new BlockGUI(), this);
        getCommand("blocktop").setExecutor(new CmdBlockTop());
        Bukkit.getScheduler().runTaskAsynchronously(BlockTop.instance, Util::loadPlayerObjects);


    }

    public void onDisable() {
        Util.savePlayerObjects(); // save
    }
}
