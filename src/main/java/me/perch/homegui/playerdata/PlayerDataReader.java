package me.perch.homegui.playerdata;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class PlayerDataReader {

    private static final String dir = "plugins/HomeGUI/userdata/";
    private static Map<String, File> playerFiles;

    public PlayerDataReader() {
        File data = new File(dir);
        data.mkdirs();
        playerFiles = new HashMap<>();
    }

    public File getFile(String playerUUID) {
        return playerFiles.computeIfAbsent(playerUUID, k -> new File(dir, k + ".yml"));
    }

    public void create(String playerUUID) {
        try {
            File dataFile = getFile(playerUUID);
            if (!dataFile.exists()) {
                dataFile.createNewFile();
            }
        } catch (IOException e) {
            Bukkit.getLogger().severe("Could not create data file for " + playerUUID);
        }
    }

    public void write(String playerUUID, String key, Material icon) {
        try {
            File dataFile = getFile(playerUUID);
            YamlConfiguration config = YamlConfiguration.loadConfiguration(dataFile);
            config.set(key, icon.name());
            config.save(dataFile);
        } catch (IOException e) {
            Bukkit.getLogger().severe("Could not save data for " + playerUUID);
        }
    }

    public ItemStack getItem(String playerUUID, String homeName) {
        File dataFile = getFile(playerUUID);
        if (dataFile.exists()) {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(dataFile);
            String name = config.getString(homeName);
            if (name != null) {
                // matchMaterial is better than getMaterial as it handles some legacy names
                Material mat = Material.matchMaterial(name);

                // Manual fallback for old specific items if matchMaterial fails
                if (mat == null || mat == Material.AIR) {
                    if (name.equalsIgnoreCase("BED")) mat = Material.RED_BED;
                    else if (name.contains("PISTON")) mat = Material.PISTON;
                    else mat = Material.GRASS_BLOCK;
                }
                return new ItemStack(mat);
            }
        }
        return new ItemStack(Material.GRASS_BLOCK);
    }

    public void removeIcon(String playerUUID, String homeName) {
        try {
            File dataFile = getFile(playerUUID);
            if (dataFile.exists()) {
                YamlConfiguration config = YamlConfiguration.loadConfiguration(dataFile);
                config.set(homeName, null);
                config.save(dataFile);
            }
        } catch (IOException e) {
            Bukkit.getLogger().severe("Could not remove icon for " + playerUUID);
        }
    }
}