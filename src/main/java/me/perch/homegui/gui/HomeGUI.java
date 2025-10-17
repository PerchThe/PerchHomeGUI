package me.perch.homegui.gui;

import me.perch.homegui.Homegui;
import me.perch.homegui.playerdata.EssentialsReader;
import me.perch.homegui.playerdata.Home;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class HomeGUI implements InventoryHolder {

    private static final int GUI_SIZE = 54;
    private static final int HOME_START_SLOT = 9;
    private static final int HOME_END_SLOT = 44;
    private static final int PAGE_SIZE = (HOME_END_SLOT - HOME_START_SLOT + 1);
    public static final int SLOT_PREV = 45;
    public static final int SLOT_PAGE = 49;
    public static final int SLOT_NEXT = 53;
    private static final int[] TOP_ROW = new int[]{0,1,2,3,4,5,6,7,8};

    public static final NamespacedKey PDC_KEY_HOME_NAME = new NamespacedKey(Homegui.PLUGIN, "home_name");
    public static final NamespacedKey PDC_KEY_NAV = new NamespacedKey(Homegui.PLUGIN, "nav");

    private final java.util.UUID playerUUID;
    private final List<Home> homes;
    private final String headerTemplate;
    private int page = 0;
    private Inventory inv;

    public HomeGUI(java.util.UUID playerUUID) {
        this.playerUUID = playerUUID;
        EssentialsReader reader = new EssentialsReader(playerUUID.toString());
        List<Home> hs = reader.getHomes();
        this.homes = (hs != null) ? hs : new ArrayList<>();
        this.headerTemplate = Homegui.PLUGIN.getConfig().getString("gui-main-header", "&8Homes &7— &fPage %p&7/&f%tp");
        Homegui.dataReader.create(playerUUID.toString());
        this.inv = buildPageInventory(0);
    }

    public void open(Player player) {
        player.openInventory(inv);
    }

    public void enforceLayout(Player player, Inventory targetTop) {
        Inventory fresh = buildPageInventory(this.page);
        targetTop.setContents(fresh.getContents());
        this.inv = targetTop;
    }

    public void nextPage(Player player) {
        if (page < getMaxPage()) {
            page++;
            Inventory fresh = buildPageInventory(page);
            this.inv = fresh;
            player.openInventory(fresh);
        }
    }

    public void prevPage(Player player) {
        if (page > 0) {
            page--;
            Inventory fresh = buildPageInventory(page);
            this.inv = fresh;
            player.openInventory(fresh);
        }
    }

    public int getCurrentPage() { return page; }

    public int getMaxPage() {
        if (homes.isEmpty()) return 0;
        return (homes.size() - 1) / PAGE_SIZE;
    }

    public Optional<Home> getHomeByName(String name) {
        if (name == null) return Optional.empty();
        for (Home h : homes) {
            if (h.getName().equalsIgnoreCase(name)) return Optional.of(h);
        }
        return Optional.empty();
    }

    private Inventory buildPageInventory(int newPage) {
        int max = getMaxPage();
        if (newPage < 0) newPage = 0;
        if (newPage > max) newPage = max;
        this.page = newPage;

        String title = formatTitle(headerTemplate, page + 1, max + 1);
        Inventory inv = Bukkit.createInventory(this, GUI_SIZE, title);

        ItemStack topDeco = decorativePane(Material.ORANGE_STAINED_GLASS_PANE, "&7 ");
        for (int s = 0; s <= 8; s++) inv.setItem(s, topDeco);

        int start = page * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, homes.size());
        List<Home> slice = homes.subList(start, end);

        String nameColor = Homegui.PLUGIN.getConfig().getString("home-color", "&f").replace("&", "§");
        List<String> loreTemplate = Homegui.PLUGIN.getConfig().getStringList("home-lore");

        int slot = HOME_START_SLOT;
        for (Home home : slice) {
            if (slot > HOME_END_SLOT) break;
            ItemStack base = Homegui.dataReader.getItem(playerUUID.toString(), home.getName());
            inv.setItem(slot++, createHomeItem(base, home, nameColor, loreTemplate));
        }

        inv.setItem(SLOT_PREV, navGlass(Material.RED_STAINED_GLASS_PANE, "§c« Previous page", "prev"));
        ItemStack bottomOrange = decorativePane(Material.ORANGE_STAINED_GLASS_PANE, "&7 ");
        for (int s = 46; s <= 52; s++) inv.setItem(s, bottomOrange);
        inv.setItem(SLOT_NEXT, navGlass(Material.LIME_STAINED_GLASS_PANE, "§aNext page »", "next"));

        return inv;
    }

    private String formatTitle(String template, int currentPage, int totalPages) {
        if (template == null) template = "&8Homes &7— &fPage %p&7/&f%tp";
        String t = template.replace("%p", String.valueOf(currentPage))
                .replace("%tp", String.valueOf(totalPages))
                .replace('&', '§')
                .replace("§8", "");
        return t;
    }

    private ItemStack createHomeItem(ItemStack baseItem, Home home, String nameColor, List<String> loreTemplate) {
        ItemStack item = (baseItem == null ? new ItemStack(Material.OAK_DOOR) : baseItem.clone());
        String displayName = nameColor + safeCap(home.getName());
        String worldDisplay = resolveWorldNameForDisplay(home.getWorld());
        String location = "§f " + home.getX() + "x§7,§f " + home.getY() + "y§7,§f " + home.getZ() + "z";
        List<String> lore = new ArrayList<>(loreTemplate.size());
        for (String line : loreTemplate) {
            String nl = line.replace("{location}", location).replace("{world}", worldDisplay);
            nl = nl.contains("&") ? nl.replace("&", "§") : "§f" + nl;
            lore.add(nl);
        }
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(displayName);
            meta.setLore(lore);
            meta.getPersistentDataContainer().set(PDC_KEY_HOME_NAME, PersistentDataType.STRING, home.getName());
            meta.getPersistentDataContainer().remove(PDC_KEY_NAV);
            item.setItemMeta(meta);
        }
        return item;
    }

    private String safeCap(String s) {
        if (s == null || s.isEmpty()) return "";
        return (s.length() == 1) ? s.toUpperCase() : s.substring(0,1).toUpperCase() + s.substring(1);
    }

    private String resolveWorldNameForDisplay(String raw) {
        if (raw == null || raw.isEmpty()) return "unknown";
        try {
            java.util.UUID uid = java.util.UUID.fromString(raw);
            World wUuid = Bukkit.getWorld(uid);
            if (wUuid != null) return wUuid.getName();
        } catch (IllegalArgumentException ignored) {}
        World wName = Bukkit.getWorld(raw);
        if (wName != null) return wName.getName();
        return raw;
    }

    private ItemStack decorativePane(Material mat, String name) {
        ItemStack pane = new ItemStack(mat);
        ItemMeta im = pane.getItemMeta();
        if (im != null) {
            im.setDisplayName(name.replace("&", "§"));
            im.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);
            pane.setItemMeta(im);
        }
        return pane;
    }

    private ItemStack navGlass(Material mat, String title, String which) {
        ItemStack it = new ItemStack(mat);
        ItemMeta im = it.getItemMeta();
        if (im != null) {
            im.setDisplayName(title);
            im.getPersistentDataContainer().remove(PDC_KEY_HOME_NAME);
            im.getPersistentDataContainer().set(PDC_KEY_NAV, PersistentDataType.STRING, which);
            it.setItemMeta(im);
        }
        return it;
    }

    @Override
    public Inventory getInventory() {
        return inv;
    }
}
