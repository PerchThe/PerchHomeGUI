package me.perch.homegui.events;

import me.perch.homegui.Homegui;
import me.perch.homegui.gui.ChangeIconGUI;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;

public class HomeEvents implements Listener {

    @EventHandler
    public void onGuiActivation(InventoryClickEvent event) {
        if (event.getInventory().getHolder() instanceof me.perch.homegui.gui.HomeGUI) {
            event.setCancelled(true);
            return;
        }

        if (event.getInventory().getHolder() instanceof ChangeIconGUI) {
            if (event.getClickedInventory() == null) return;
            Player player = (Player) event.getWhoClicked();
            if (event.getCurrentItem() == null) return;

            Material type = event.getCurrentItem().getType();
            if (type == Material.AIR) return;

            event.setCancelled(true);
            if (event.getClickedInventory().getType() == InventoryType.PLAYER) return;

            if (event.getClick() == ClickType.LEFT) {
                String itemName = getFriendlyName(type);
                String playerID = player.getUniqueId().toString();
                String homeName = ChangeIconGUI.homes.get(playerID).getName();

                // Use the material directly
                Homegui.dataReader.write(playerID, homeName, type);

                String msg = Homegui.PLUGIN.getConfig().getString("icon-select-message", "")
                        .replace("&", "§")
                        .replace("{home}", homeName)
                        .replace("{icon}", itemName);

                player.sendMessage(msg);
                player.closeInventory();
            }
        }
    }

    private String format(String s) {
        if (!s.contains("_")) return capitalize(s);
        String[] words = s.split("_");
        StringBuilder sb = new StringBuilder();
        for (String word : words) {
            sb.append(capitalize(word)).append(" ");
        }
        return sb.toString().trim();
    }

    private String capitalize(String text) {
        if (text == null || text.isEmpty()) return "";
        return text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();
    }

    public String getFriendlyName(Material m) {
        return format(m.name());
    }
}