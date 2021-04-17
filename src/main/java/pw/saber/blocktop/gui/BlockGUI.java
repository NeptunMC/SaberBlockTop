package pw.saber.blocktop.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import pw.saber.blocktop.NeptunTopPlugin;

public class BlockGUI implements Listener {

  private final NeptunTopPlugin plugin;

  public BlockGUI(NeptunTopPlugin plugin) {
    this.plugin = plugin;
  }

  public static void openGUI(Player p) {
    Inventory i = Bukkit
        .createInventory(null, this.plugin.getConfig().getInt("Gui.Rows") * 9, Util.color(
            NeptunTopPlugin.getInstance().getConfig().getString("Gui.Title")));
    List<Integer> numbers = new ArrayList<>();
    List<UUID> alreadyUUID = new ArrayList<>();
    for (PlayerObject object : Util.playerBlock) {
      int block = object.getBlockBroke();
      numbers.add(block);
    }
    numbers.sort(Collections.reverseOrder());
    int currentPlace = 1;
    List<String> defaultLore = NeptunTopPlugin.getInstance().getConfig()
        .getStringList("Head-Item.Lore");
    for (int block : numbers) {
      for (PlayerObject object : Util.playerBlock) {
        UUID uuid = object.getUuid();
        if (!alreadyUUID.contains(uuid)) {
          if (object.getBlockBroke() == block) {
            alreadyUUID.add(uuid);
            int slot = currentPlace - 1;
            String name = Bukkit.getOfflinePlayer(uuid).getName();
            List<String> goodLore = new ArrayList<>();
            for (String s : defaultLore) {
              s = s.replace("{breaks}", block + "");
              goodLore.add(Util.color(s));
            }
            ItemStack head = createHead(name, "SKULL_ITEM", 3,
                NeptunTopPlugin.getInstance().getConfig().getString("Head-Item.Name")
                    .replace("{place}", currentPlace + "").replace("{player}", name), goodLore, 1);
            i.setItem(slot, head);
            currentPlace++;
          }
        }
      }
    }
    p.openInventory(i);
  }

  public static ItemStack createHead(String owner, String materialName, int data, String name,
      List<String> lore, int amount) {
    Material material;
    if (XMaterial.matchXMaterial(materialName, (byte) data) != null
        && XMaterial.matchXMaterial(materialName, (byte) data).parseMaterial() != null) {
      material = XMaterial.matchXMaterial(materialName, (byte) data).parseMaterial();
    } else {
      material = Material.valueOf(materialName.toUpperCase());
    }
    ItemStack itemStack = new ItemStack(material, amount, (short) data);
    SkullMeta meta = (SkullMeta) itemStack.getItemMeta();
    meta.setOwner(owner);
    meta.setDisplayName(Util.color(name));
    meta.setLore(Util.color(lore));
    itemStack.setAmount(amount);
    itemStack.setItemMeta(meta);
    return itemStack;
  }


  @EventHandler
  public void click(InventoryClickEvent e) {
    Inventory i = e.getClickedInventory();
      if (i == null) {
          return;
      }
    if (i.getName().equalsIgnoreCase(
        Util.color(NeptunTopPlugin.getInstance().getConfig().getString("gui-name")))) {
      e.setCancelled(true);
    }
  }
}
