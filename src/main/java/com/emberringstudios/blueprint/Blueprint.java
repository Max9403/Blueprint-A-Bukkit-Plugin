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
        getPlugin().getServer().getLogger().log(Level.INFO, "[Blueprint] {0}", log);
    }

    public static void error(String log, Exception ex) {
        getPlugin().getServer().getLogger().log(Level.SEVERE, "[Blueprint] {0}, disabaling", log);
       getPlugin().getServer().getLogger().log(Level.SEVERE, null, ex);
        Bukkit.getServer().getPluginManager().disablePlugin(plugin);
    }

    public static void error(String log) {
        getPlugin().getServer().getLogger().log(Level.SEVERE, "[Blueprint] {0}, disabaling", log);
        Bukkit.getServer().getPluginManager().disablePlugin(plugin);
    }

    public static void warn(String log) {
        getPlugin().getServer().getLogger().log(Level.WARNING, "[Blueprint] {0}", log);
    }

    public static void debug(String log) {
        getPlugin().getServer().getLogger().log(Level.FINE, "[Blueprint] {0}", log);
    }

    @Override
    public void onEnable() {
        setPlugin(this);
        if (!this.getServer().getPluginManager().isPluginEnabled("SQLibrary")) {
            try {
                info("Downloadind dependecy: SQLibrary");
                URL url = new URL("http://repo.dakanilabs.com/content/repositories/public/lib/PatPeter/SQLibrary/SQLibrary/maven-metadata.xml");
                URLConnection urlConnection = url.openConnection();
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());

                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                Document doc = dBuilder.parse(in);
                final String version = doc.getElementsByTagName("latest").item(0).getTextContent();
                in.close();
                url = new URL("http://repo.dakanilabs.com/content/repositories/public/lib/PatPeter/SQLibrary/SQLibrary/" + version + "/SQLibrary-" + version + ".jar");
                final String path = plugin.getDataFolder().getParentFile().getAbsoluteFile() + "/SQLibrary-" + version + ".jar";
                ReadableByteChannel rbc = Channels.newChannel(url.openStream());
                FileOutputStream fos = new FileOutputStream(path);
                fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
                info("Finished downloading SQLibrary-" + version + ". Loading dependecy");
                this.getServer().getPluginManager().loadPlugin(new File(path));
                info("Loaded SQLibrary");
            } catch (MalformedURLException ex) {
                error("Failed to download dependency", ex);
            } catch (IOException ex) {
                error("Failed to download dependency", ex);
            } catch (ParserConfigurationException ex) {
                error("Failed to download dependency", ex);
            } catch (SAXException ex) {
                error("Failed to download dependency", ex);
            } catch (InvalidPluginException ex) {
                error("Failed to load dependency", ex);
            } catch (InvalidDescriptionException ex) {
                error("Failed to load dependency", ex);
            } catch (UnknownDependencyException ex) {
                error("Failed to load dependency", ex);
            }
        }

        setConfigDefaults();
        ConfigHandler.getDefaultBukkitConfig().options().copyDefaults(true);
        getServer().getPluginManager().registerEvents(new PlayerListener(), this);
        DataHandler.setupDB();
        Commands.register();
        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, new BlueprintBuild(plugin), ConfigHandler.getDefaultBukkitConfig().getInt("limits.time to check", 60), ConfigHandler.getDefaultBukkitConfig().getInt("limits.time to check", 60));
    }

    @Override
    public void onDisable() {
        saveConfig();
        ConfigHandler.getTheDataHub().close();
    }

    private void setConfigDefaults() {
        ConfigHandler.getDefaultBukkitConfig().addDefault("use.UUIDs", true);
        ConfigHandler.getDefaultBukkitConfig().addDefault("database.type", 0);
        ConfigHandler.getDefaultBukkitConfig().addDefault("database.hostname", "localhost");
        ConfigHandler.getDefaultBukkitConfig().addDefault("database.port", 3306);
        ConfigHandler.getDefaultBukkitConfig().addDefault("database.database", "blueprint");
        ConfigHandler.getDefaultBukkitConfig().addDefault("database.user", "root");
        ConfigHandler.getDefaultBukkitConfig().addDefault("database.password", "");
        ConfigHandler.getDefaultBukkitConfig().addDefault("limits.blocks at a time", 20);
        ConfigHandler.getDefaultBukkitConfig().addDefault("limits.time to check", 60);
    }
}
