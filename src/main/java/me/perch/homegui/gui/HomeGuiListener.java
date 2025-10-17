package me.perch.homegui.gui;

import me.perch.homegui.Homegui;
import me.perch.homegui.playerdata.Home;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Optional;

public class HomeGuiListener implements Listener {

    private static final int HOME_START_SLOT = 9;
    private static final int HOME_END_SLOT   = 44;
    private static final int SLOT_PREV = HomeGUI.SLOT_PREV;
    private static final int SLOT_NEXT = HomeGUI.SLOT_NEXT;

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryOpen(InventoryOpenEvent e) {
        Inventory top = e.getView().getTopInventory();
        InventoryHolder holder = top.getHolder();
        if (!(holder instanceof HomeGUI)) return;
        if (!(e.getPlayer() instanceof Player)) return;
        ((HomeGUI) holder).enforceLayout((Player) e.getPlayer(), top);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onInventoryClick(InventoryClickEvent e) {
        Inventory top = e.getView().getTopInventory();
        if (!(top.getHolder() instanceof HomeGUI)) return;
        HomeGUI gui = (HomeGUI) top.getHolder();

        int raw = e.getRawSlot();
        if (raw < 0 || raw >= top.getSize()) return;

        e.setCancelled(true);
        if (!(e.getWhoClicked() instanceof Player)) return;
        Player p = (Player) e.getWhoClicked();

        ItemStack clicked = e.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;
        ItemMeta meta = clicked.getItemMeta();
        if (meta == null) return;

        String nav = meta.getPersistentDataContainer().get(HomeGUI.PDC_KEY_NAV, PersistentDataType.STRING);
        if (raw == SLOT_PREV || "prev".equalsIgnoreCase(nav)) { gui.prevPage(p); return; }
        if (raw == SLOT_NEXT || "next".equalsIgnoreCase(nav)) { gui.nextPage(p); return; }

        if (raw < HOME_START_SLOT || raw > HOME_END_SLOT) return;

        String homeName = meta.getPersistentDataContainer().get(HomeGUI.PDC_KEY_HOME_NAME, PersistentDataType.STRING);
        if (homeName == null || homeName.isEmpty()) return;

        boolean isRight  = e.isRightClick();
        boolean isShift  = e.isShiftClick();
        boolean isLeft   = e.isLeftClick();

        if (isRight && isShift) {
            p.closeInventory();
            p.performCommand("essentials:delhome " + homeName);
            Homegui.dataReader.removeIcon(p.getUniqueId().toString(), homeName);
            return;
        }

        if (isRight) {
            Optional<Home> oh = gui.getHomeByName(homeName);
            if (oh.isPresent()) {
                p.closeInventory();
                ChangeIconGUI iconGUI = new ChangeIconGUI();
                iconGUI.openInventory(p, oh.get());
            }
            return;
        }

        if (isLeft) {
            p.closeInventory();
            p.performCommand("essentials:home " + homeName);
        }
    }
}
