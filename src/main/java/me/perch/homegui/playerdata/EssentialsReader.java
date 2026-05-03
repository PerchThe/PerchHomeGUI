package me.perch.homegui.playerdata;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class EssentialsReader {

    private YamlConfiguration fileReader;
    private List<Home> homes;

    public EssentialsReader(String playerID) {
        try {
            fileReader = new YamlConfiguration();
            File essentialsFolder = Bukkit.getServer().getPluginManager().getPlugin("Essentials").getDataFolder();
            File playerData = new File(essentialsFolder, "userdata/" + playerID + ".yml");

            if (playerData.exists()) {
                fileReader.load(playerData);
                initHomes();
            } else {
                homes = new ArrayList<>();
            }
        } catch (Exception e) {
            Bukkit.getLogger().warning("[HomeGUI] Could not load Essentials data for " + playerID);
            homes = new ArrayList<>();
        }
    }

    private void initHomes() {
        homes = new ArrayList<>();
        ConfigurationSection homesSec = fileReader.getConfigurationSection("homes");
        if (homesSec == null) return;

        Material defaultMat = Material.GRASS_BLOCK;

        for (String name : homesSec.getKeys(false)) {
            ConfigurationSection h = homesSec.getConfigurationSection(name);
            if (h == null) continue;

            String world = h.getString("world", "world");
            int x = (int) Math.round(h.getDouble("x", 0.0));
            int y = (int) Math.round(h.getDouble("y", 64.0));
            int z = (int) Math.round(h.getDouble("z", 0.0));

            homes.add(new Home(name, world, x, y, z, defaultMat));
        }
    }

    public List<Home> getHomes() {
        return homes;
    }
}