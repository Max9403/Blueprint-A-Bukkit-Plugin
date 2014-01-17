package com.emberringstudios.blueprint;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.bukkit.Bukkit;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.UnknownDependencyException;
import org.bukkit.plugin.java.JavaPlugin;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Blueprint Bukkit plugin
 *
 */
public class Blueprint extends JavaPlugin {

    private static volatile Blueprint plugin;

    /**
     * @return the plugin
     */
    public static Blueprint getPlugin() {
        return plugin;
    }

    /**
     * @param aPlugin the plugin to set
     */
    public static void setPlugin(Blueprint aPlugin) {
        plugin = aPlugin;
    }

    public static void info(String log) {
        getPlugin().getServer().getLogger().log(Level.INFO, "[Blueprint Builder] {0}", log);
    }

    public static void error(String log, Exception ex) {
        getPlugin().getServer().getLogger().log(Level.SEVERE, "[Blueprint Builder] {0}, disabaling", log);
        getPlugin().getServer().getLogger().log(Level.SEVERE, null, ex);
        Bukkit.getServer().getPluginManager().disablePlugin(plugin);
    }

    public static void error(String log) {
        getPlugin().getServer().getLogger().log(Level.SEVERE, "[Blueprint Builder] {0}, disabaling", log);
        Bukkit.getServer().getPluginManager().disablePlugin(plugin);
    }

    public static void warn(String log) {
        getPlugin().getServer().getLogger().log(Level.WARNING, "[Blueprint Builder] {0}", log);
    }

    public static void debug(String log) {
        getPlugin().getServer().getLogger().log(Level.FINE, "[Blueprint Builder] {0}", log);
    }

    @Override
    public void onEnable() {
        setPlugin(this);
        if (!this.getServer().getPluginManager().isPluginEnabled("SQLibrary") && this.getConfig().getBoolean("use.downloads", true)) {
            info("Downloadind dependecy: SQLibrary");
            if (PluginDownloader.downloadPlugin("43840")) {
                info("Loaded SQLibrary");
            }
        }

        setConfigDefaults();
        ConfigHandler.getDefaultBukkitConfig().options().copyDefaults(true);
        setGreenlistDefaults();
        ConfigHandler.getGreenlistConfig().options().copyDefaults(true);
        setBlacklistDefaults();
        ConfigHandler.getBlacklistConfig().options().copyDefaults(true);
        getServer().getPluginManager().registerEvents(new PlayerListener(), this);
        DataHandler.setupDB();
        DataHandler.setupCache();
        Commands.register();
        ConfigHandler.saveGreenlistConfig();
        ConfigHandler.saveBlacklistConfig();
        plugin.getServer().getScheduler().runTaskTimer(plugin, new BlueprintBuild(plugin), ConfigHandler.getDefaultBukkitConfig().getInt("limits.time to check", 60), ConfigHandler.getDefaultBukkitConfig().getInt("limits.time to check", 60));
        plugin.getServer().getScheduler().runTaskTimer(plugin, new BlockSetter(), ConfigHandler.getDefaultBukkitConfig().getInt("limits.time to check", 60), ConfigHandler.getDefaultBukkitConfig().getInt("limits.time to check", 60));
        if (ConfigHandler.getDefaultBukkitConfig().getBoolean("limits.blacklist")) {
            info("Running in blacklist mode");
        } else {
            info("Running in whitelist mode");
        }
    }

    @Override
    public void onDisable() {
        saveConfig();
        ConfigHandler.getTheDataHub().close();
    }

    private void setConfigDefaults() {
        ConfigHandler.getDefaultBukkitConfig().addDefault("use.UUIDs", true);
        ConfigHandler.getDefaultBukkitConfig().addDefault("use.downloads", true);
        ConfigHandler.getDefaultBukkitConfig().addDefault("database.type", 0);
        ConfigHandler.getDefaultBukkitConfig().addDefault("database.hostname", "localhost");
        ConfigHandler.getDefaultBukkitConfig().addDefault("database.port", 3306);
        ConfigHandler.getDefaultBukkitConfig().addDefault("database.database", "blueprint");
        ConfigHandler.getDefaultBukkitConfig().addDefault("database.user", "root");
        ConfigHandler.getDefaultBukkitConfig().addDefault("database.password", "");
        ConfigHandler.getDefaultBukkitConfig().addDefault("limits.blocks at a time", 20);
        ConfigHandler.getDefaultBukkitConfig().addDefault("limits.time to check", 60);
        ConfigHandler.getDefaultBukkitConfig().addDefault("limits.blacklist", true);
    }

    private void setGreenlistDefaults() {
        ConfigHandler.getGreenlistConfig().addDefault("Greenlist Items", Arrays.asList(new Integer[]{
            31,
            32,
            175
        }));
    }

    private void setBlacklistDefaults() {
        ConfigHandler.getBlacklistConfig().addDefault("List Items", Arrays.asList(new Integer[]{
            46
        }));
    }
}
