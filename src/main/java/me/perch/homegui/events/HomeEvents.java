package me.perch.homegui.events;

import com.cryptomorin.xseries.XMaterial;
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
            if (event.getCurrentItem().getType() == Material.AIR) return;
            event.setCancelled(true);
            if (event.getClickedInventory().getType() == InventoryType.PLAYER) return;

            if (event.getClick() == ClickType.LEFT) {
                String itemName = getFriendlyName(event.getCurrentItem().getType());
                String playerID = player.getUniqueId().toString();
                XMaterial icon = XMaterial.matchXMaterial(event.getCurrentItem().getType());
                String homeName = ChangeIconGUI.homes.get(playerID).getName();
                Homegui.dataReader.write(playerID, homeName, icon.parseMaterial());
                String msg = Homegui.PLUGIN.getConfig().getString("icon-select-message").replace("&", "§");
                msg = msg.replace("{home}", homeName);
                msg = msg.replace("{icon}", itemName);
                player.sendMessage(msg);
                player.closeInventory();
            }
        }
    }

    private String format(String s) {
        if (!s.contains("_")) return capitalize(s);
        String[] j = s.split("_");
        String c = "";
        for (String f : j) {
            f = capitalize(f);
            c += c.equalsIgnoreCase("") ? f : " " + f;
        }
        return c;
    }

    private String capitalize(String text) {
        String firstLetter = text.substring(0, 1).toUpperCase();
        String next = text.substring(1).toLowerCase();
        return firstLetter + next;
    }

    public String getFriendlyName(Material m) {
        return format(m.name());
    }
}
