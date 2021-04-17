package pw.saber.blocktop.listener;

import com.google.common.base.Enums;
import java.util.UUID;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import pw.saber.blocktop.NeptunTopPlugin;

public class BreakListener implements Listener {

  private final NeptunTopPlugin plugin;

  public BreakListener(NeptunTopPlugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler
  public void login(PlayerJoinEvent e) {
    final Player p = e.getPlayer();
    final UUID uuid = p.getUniqueId();

    if (Util.getPlayerObject(uuid) != null) {
      return;
    }

    final PlayerObject object = new PlayerObject(uuid, 0);
    Util.playerBlock.add(object);
  }

  @EventHandler
  public void onBreak(BlockBreakEvent e) {
    final Block block = e.getBlock();

    if (block == null) {
      return;
    }

    final Player player = e.getPlayer();

    if (player == null) {
      return;
    }

    final FileConfiguration config = this.plugin.getConfig();
    final String string = config.getString("Block-Item");
    final Material material = Enums.getIfPresent(Material.class, string).orNull();

    if (material != block.getType()) {
      return;
    }

    final PlayerObject object = Util.getPlayerObject(player.getUniqueId());

    if (object == null) {
      return;
    }

    object.addBlockBroke(1);

  }
}
