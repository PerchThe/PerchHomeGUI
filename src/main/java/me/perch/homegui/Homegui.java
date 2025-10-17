package me.perch.homegui;

import me.perch.homegui.commands.HomeCommand;
import me.perch.homegui.events.HomeEvents;
import me.perch.homegui.gui.HomeGuiListener;
import me.perch.homegui.playerdata.PlayerDataReader;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class Homegui extends JavaPlugin {

    public static Homegui PLUGIN;
    public static PlayerDataReader dataReader;

    @Override
    public void onEnable() {
        PLUGIN = this;
        dataReader = new PlayerDataReader();
        loadConfig();

        // Register events
        getServer().getPluginManager().registerEvents(new HomeEvents(), this);
        getServer().getPluginManager().registerEvents(new HomeGuiListener(), this); // <-- add listener

        // Register commands (defensive against null if plugin.yml is missing entries)
        HomeCommand homeExecutor = new HomeCommand();
        PluginCommand homeCmd = getCommand(HomeCommand.HOME);
        if (homeCmd != null) homeCmd.setExecutor(homeExecutor);

        PluginCommand hCmd = getCommand(HomeCommand.H);
        if (hCmd != null) hCmd.setExecutor(homeExecutor);

        getLogger().info("[HomeGUI] Enabled.");
    }

    @Override
    public void onDisable() {
        getLogger().info("[HomeGUI] Disabled.");
    }

    private void loadConfig() {
        // Save defaults if not present, then copy defaults into live config
        saveDefaultConfig();
        getConfig().options().copyDefaults(true);
        getConfig().options().copyHeader(true);
        saveConfig();
    }
}
