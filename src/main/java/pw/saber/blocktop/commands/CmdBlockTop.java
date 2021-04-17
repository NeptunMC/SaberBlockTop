package pw.saber.blocktop.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pw.saber.blocktop.NeptunTopPlugin;
import pw.saber.blocktop.gui.BlockGUI;

public class CmdBlockTop implements CommandExecutor {

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (!(sender instanceof Player)) {
      return false;
    }

    Player player = (Player) sender;

    if (args.length == 0) {
      Bukkit.getScheduler()
          .runTaskAsynchronously(NeptunTopPlugin.instance, () -> BlockGUI.openGUI(player));
    } else if (args.length == 1) {
      if (player.hasPermission("blocktop.reload")) {
        if (args[0].equalsIgnoreCase("reload")) {
          NeptunTopPlugin.getInstance().reloadConfig();
        } else {
          player.sendMessage(Util.color("&c&l[!] &7Try /blocktop reload!"));
        }
      } else {
        player.sendMessage(Util.color("&c&l[!] &7You do not have permission to use this command"));
      }
    } else {
      player.sendMessage(Util.color("&c&l[!] &7Try /blocktop reload!"));
    }
    return false;
  }
}
