package pw.saber.blocktop.listener;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import pw.saber.blocktop.BlockTop;
import pw.saber.blocktop.utils.PlayerObject;
import pw.saber.blocktop.utils.Util;
import pw.saber.blocktop.utils.XMaterial;

import java.util.UUID;

public class BreakListener implements Listener {

    @EventHandler
    public void login(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        UUID uuid = p.getUniqueId();
        if (Util.getPlayerObject(uuid) == null) {

            PlayerObject object = new PlayerObject(uuid, 0);

            Util.playerBlock.add(object);
        }
    }

    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        Block block = e.getBlock();
        if (block == null) return;
        Player player = e.getPlayer();
        if (player == null) return;
        if (block.getType().equals(XMaterial.matchXMaterial(BlockTop.getInstance().getConfig().getString("Block-Item")).parseMaterial())) {

            PlayerObject object = Util.getPlayerObject(player.getUniqueId());

            if (object == null) return;

            object.addBlockBroke(1);
        }
    }
}
