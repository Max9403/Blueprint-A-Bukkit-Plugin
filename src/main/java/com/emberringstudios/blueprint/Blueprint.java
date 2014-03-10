package com.emberringstudios.blueprint;

import com.emberringstudios.blueprint.background.QueryProcessor;
import com.emberringstudios.blueprint.background.BlockSetter;
import com.emberringstudios.blueprint.background.BlueprintBuild;
import java.util.Arrays;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Blueprint Bukkit plugin
 *
 */
public class Blueprint extends JavaPlugin {

    private static volatile Blueprint plugin;

    /**
     * Returns the plugin value of Blueprint
     *
     * @return Plugin value
     */
    public static Blueprint getPlugin() {
        return plugin;
    }

    /**
     * Logs a String as info to the consol
     *
     * @param log String to be send to the consol
     */
    public static void info(String log) {
        getPlugin().getServer().getLogger().log(Level.INFO, "[Blueprint Builder] {0}", log);
    }

    /**
     * Logs a String and an exception to the consol before disabling Blueprint
     * Builder
     *
     * @param log String to be logged to the consol
     * @param ex  The exception that will be logged to the consol
     */
    public static void error(String log, Exception ex) {
        getPlugin().getServer().getLogger().log(Level.SEVERE, "[Blueprint Builder] {0}, disabaling", log);
        getPlugin().getServer().getLogger().log(Level.SEVERE, null, ex);
        Bukkit.getServer().getPluginManager().disablePlugin(plugin);
    }

    /**
     * Logs a String to the consol before disabling Blueprint Builder
     *
     * @param log String to be logged to the consol
     */
    public static void error(String log) {
        getPlugin().getServer().getLogger().log(Level.SEVERE, "[Blueprint Builder] {0}, disabaling", log);
        Bukkit.getServer().getPluginManager().disablePlugin(plugin);
    }

    /**
     * Logs a String to the consol as a Warning
     *
     * @param log String to be logged to the consol
     */
    public static void warn(String log) {
        getPlugin().getServer().getLogger().log(Level.WARNING, "[Blueprint Builder] {0}", log);
    }

    /**
     * Logs a debug String to as a debug message
     *
     * @param log String to be logged to the consol
     */
    public static void debug(String log) {
        getPlugin().getServer().getLogger().log(Level.FINE, "[Blueprint Builder] {0}", log);
    }

    @Override
    public void onEnable() {
        plugin = this;
        if (!this.getServer().getPluginManager().isPluginEnabled("SQLibrary")) {
            if (this.getConfig().getBoolean("use.downloads", true)) {
                info("Downloadind dependecy: SQLibrary");
                if (PluginDownloader.downloadPlugin("43840")) {
                    info("Loaded SQLibrary");
                }
            } else {
                error("Please download and install SQLibrary, a link can be found on my plugins page");
                return;
            }
        }
        
        setConfigDefaults();
        ConfigHandler.getDefaultBukkitConfig().options().copyDefaults(true);
        setGreenlistDefaults();
        ConfigHandler.getGreenlistConfig().options().copyDefaults(true);
        setBlockBlacklistDefaults();
        ConfigHandler.getBlockBlacklistConfig().options().copyDefaults(true);
        setCommandsBlacklistDefaults();
        ConfigHandler.getCommandsBlacklistConfig().options().copyDefaults(true);
        getServer().getPluginManager().registerEvents(new PlayerListener(), this);
        DataHandler.setupDB();
        DataHandler.setupCache();
        Commands.register();
        ConfigHandler.saveGreenlistConfig();
        ConfigHandler.saveBlockBlacklistConfig();
        ConfigHandler.saveCommandsBlacklistConfig();
        saveConfig();
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new QueryProcessor());
        plugin.getServer().getScheduler().runTaskTimer(plugin, new BlueprintBuild(plugin), ConfigHandler.getDefaultBukkitConfig().getInt("limits.time to check", 60), ConfigHandler.getDefaultBukkitConfig().getInt("limits.time to check", 60));
        plugin.getServer().getScheduler().runTaskTimer(plugin, new BlockSetter(), ConfigHandler.getDefaultBukkitConfig().getInt("limits.time to check", 60), ConfigHandler.getDefaultBukkitConfig().getInt("limits.time to check", 60));
        if (ConfigHandler.getDefaultBukkitConfig().getBoolean("limits.blacklist.blocks")) {
            info("Running in block blacklist mode");
        } else {
            info("Running in block whitelist mode");
        }
        if (ConfigHandler.getDefaultBukkitConfig().getBoolean("limits.blacklist.commands")) {
            info("Running in commands blacklist mode");
        } else {
            info("Running in commands whitelist mode");
        }
    }

    @Override
    public void onDisable() {
        saveConfig();
        ConfigHandler.getTheDataHub().close();
    }

    /**
     * Sets Blueprint Builders default configuration
     */
    private void setConfigDefaults() {
        ConfigHandler.getDefaultBukkitConfig().addDefault("use.UUIDs", true);
        ConfigHandler.getDefaultBukkitConfig().addDefault("use.downloads", true);
        ConfigHandler.getDefaultBukkitConfig().addDefault("database.type", 0);
        ConfigHandler.getDefaultBukkitConfig().addDefault("database.prefix", "blueprint");
        ConfigHandler.getDefaultBukkitConfig().addDefault("database.hostname", "localhost");
        ConfigHandler.getDefaultBukkitConfig().addDefault("database.port", 3306);
        ConfigHandler.getDefaultBukkitConfig().addDefault("database.database", "blueprint");
        ConfigHandler.getDefaultBukkitConfig().addDefault("database.user", "root");
        ConfigHandler.getDefaultBukkitConfig().addDefault("database.password", "");
        ConfigHandler.getDefaultBukkitConfig().addDefault("limits.disable signs", true);
        ConfigHandler.getDefaultBukkitConfig().addDefault("limits.blocks at a time", 20);
        ConfigHandler.getDefaultBukkitConfig().addDefault("limits.time to check", 60);
        ConfigHandler.getDefaultBukkitConfig().addDefault("limits.blacklist.blocks", true);
        ConfigHandler.getDefaultBukkitConfig().addDefault("limits.blacklist.commands", true);
        ConfigHandler.getDefaultBukkitConfig().addDefault("limits.disable signs", true);
        ConfigHandler.getDefaultBukkitConfig().addDefault("limits.combat cooldown", 200);
        ConfigHandler.getDefaultBukkitConfig().addDefault("limits.disabale on logout", false);
        ConfigHandler.getDefaultBukkitConfig().addDefault("limits.logging destroy and build", true);
    }

    /**
     * Sets the default green list items
     */
    private void setGreenlistDefaults() {
        ConfigHandler.getGreenlistConfig().addDefault("Greenlist Items", Arrays.asList(new Integer[]{
            31,
            32,
            175
        }));
    }

    /**
     * Sets the default blacklist items
     */
    private void setBlockBlacklistDefaults() {
        ConfigHandler.getBlockBlacklistConfig().addDefault("List Items", Arrays.asList(new Integer[]{
            46
        }));
    }

    /**
     * Sets the default blacklist items
     */
    private void setCommandsBlacklistDefaults() {
        ConfigHandler.getCommandsBlacklistConfig().addDefault("List Commands", Arrays.asList(new String[]{
            "/give.*"
        }));
    }
}
