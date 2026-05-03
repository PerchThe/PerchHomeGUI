package me.perch.homegui.gui;

import me.perch.homegui.Homegui;
import me.perch.homegui.playerdata.Home;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class ChangeIconGUI implements InventoryHolder, Listener {

    public static List<String> activeGui = new ArrayList<>();
    public static Map<String, Home> homes = new HashMap<>();
    private Inventory inv;

    public ChangeIconGUI() {
        String rawTitle = Homegui.PLUGIN.getConfig().getString("gui-icon-header", "Select Icon");
        String title = ChatColor.translateAlternateColorCodes('&', rawTitle);
        inv = Bukkit.createInventory(this, 54, title);
        initItems();
    }

    private void initItems() {
        List<String> icons = Homegui.PLUGIN.getConfig().getStringList("icons");
        String loreMsg = Homegui.PLUGIN.getConfig().getString("icon-select-lore-message", "").replace('&', '§');

        for (String iconName : icons) {
            Material mat = Material.matchMaterial(iconName);
            if (mat != null && mat != Material.AIR) {
                ItemStack item = new ItemStack(mat);
                ItemMeta meta = item.getItemMeta();
                if (meta != null && !loreMsg.isEmpty()) {
                    meta.setLore(Collections.singletonList(loreMsg));
                    item.setItemMeta(meta);
                }
                inv.addItem(item);
            } else {
                Homegui.PLUGIN.getLogger().warning("Invalid Material in config: " + iconName);
            }
        }
    }

    public void openInventory(Player player, Home home) {
        player.openInventory(inv);
        activeGui.add(player.getName());
        homes.put(player.getUniqueId().toString(), home);
    }

    @Override
    public Inventory getInventory() {
        return inv;
    }
}