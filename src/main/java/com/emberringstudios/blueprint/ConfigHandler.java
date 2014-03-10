package com.emberringstudios.blueprint;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import lib.PatPeter.SQLibrary.DB2;
import lib.PatPeter.SQLibrary.Database;
import lib.PatPeter.SQLibrary.Firebird;
import lib.PatPeter.SQLibrary.FrontBase;
import lib.PatPeter.SQLibrary.H2;
import lib.PatPeter.SQLibrary.Informix;
import lib.PatPeter.SQLibrary.Ingres;
import lib.PatPeter.SQLibrary.MaxDB;
import lib.PatPeter.SQLibrary.MicrosoftSQL;
import lib.PatPeter.SQLibrary.Mongo;
import lib.PatPeter.SQLibrary.MySQL;
import lib.PatPeter.SQLibrary.Oracle;
import lib.PatPeter.SQLibrary.PostgreSQL;
import lib.PatPeter.SQLibrary.SQLite;
import lib.PatPeter.SQLibrary.mSQL;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 *
 * @author Max9403 <Max9403@live.com>
 */
public class ConfigHandler {

    private volatile static Database theDataHub = null;
    private volatile static FileConfiguration greenlistConfig = null;
    private volatile static File greenlistConfigFile = null;
    private volatile static FileConfiguration blockBlacklistConfig = null;
    private volatile static File blockBlacklistConfigFile = null;
    private volatile static FileConfiguration commandsBlacklistConfig = null;
    private volatile static File commandsBlacklistConfigFile = null;

    /**
     * @return the defaultBukkitConfig
     */
    public static FileConfiguration getDefaultBukkitConfig() {
        return Blueprint.getPlugin().getConfig();
    }

    /**
     * @return the theDataHub
     */
    public static Database getTheDataHub() {
        if (theDataHub == null) {
            Blueprint.getPlugin().getDataFolder().mkdirs();
            FileConfiguration localConfig = ConfigHandler.getDefaultBukkitConfig();
            switch (localConfig.getInt("database.type", 0)) {
                case 1:
                    theDataHub = new Firebird(Logger.getLogger("Minecraft"), "[Blueprint]", localConfig.getString("database.hostname"), localConfig.getInt("database.port"), localConfig.getString("database.database"), localConfig.getString("database.user"), localConfig.getString("database.password"));
                    break;
                case 2:
                    theDataHub = new FrontBase(Logger.getLogger("Minecraft"), "[Blueprint]", localConfig.getString("database.hostname"), localConfig.getInt("database.port"), localConfig.getString("database.database"), localConfig.getString("database.user"), localConfig.getString("database.password"));
                    break;
                case 3:
                    theDataHub = new DB2(Logger.getLogger("Minecraft"), "[Blueprint]", localConfig.getString("database.hostname"), localConfig.getInt("database.port"), localConfig.getString("database.database"), localConfig.getString("database.user"), localConfig.getString("database.password"));
                    break;
                case 4:
                    theDataHub = new H2(Logger.getLogger("Minecraft"), "[Blueprint]", Blueprint.getPlugin().getDataFolder() + "/data", "users");
                    break;
                case 5:
                    theDataHub = new Informix(Logger.getLogger("Minecraft"), "[Blueprint]", localConfig.getString("database.hostname"), localConfig.getInt("database.port"), localConfig.getString("database.database"), localConfig.getString("database.user"), localConfig.getString("database.password"));
                    break;
                case 6:
                    theDataHub = new Ingres(Logger.getLogger("Minecraft"), "[Blueprint]", localConfig.getString("database.hostname"), localConfig.getInt("database.port"), localConfig.getString("database.database"), localConfig.getString("database.user"), localConfig.getString("database.password"));
                    break;
                case 7:
                    theDataHub = new MaxDB(Logger.getLogger("Minecraft"), "[Blueprint]", localConfig.getString("database.hostname"), localConfig.getInt("database.port"), localConfig.getString("database.database"), localConfig.getString("database.user"), localConfig.getString("database.password"));
                    break;
                case 8:
                    try {
                        theDataHub = new MicrosoftSQL(Logger.getLogger("Minecraft"), "[Blueprint]", localConfig.getString("database.hostname"), localConfig.getInt("database.port"), localConfig.getString("database.database"), localConfig.getString("database.user"), localConfig.getString("database.password"));
                    } catch (SQLException ex) {
                        Blueprint.error("Failed to connect to database", ex);
                    }
                    break;
                case 9:
                    theDataHub = new MySQL(Logger.getLogger("Minecraft"), "[Blueprint]", localConfig.getString("database.hostname"), localConfig.getInt("database.port"), localConfig.getString("database.database"), localConfig.getString("database.user"), localConfig.getString("database.password"));
                    break;
                case 10:
                    theDataHub = new Mongo(Logger.getLogger("Minecraft"), "[Blueprint]", localConfig.getString("database.hostname"), localConfig.getInt("database.port"), localConfig.getString("database.database"), localConfig.getString("database.user"), localConfig.getString("database.password"));
                    break;
                case 11:
                    theDataHub = new mSQL(Logger.getLogger("Minecraft"), "[Blueprint]", localConfig.getString("database.hostname"), localConfig.getInt("database.port"), localConfig.getString("database.database"), localConfig.getString("database.user"), localConfig.getString("database.password"));
                    break;
                case 12:
                    try {
                        theDataHub = new Oracle(Logger.getLogger("Minecraft"), "[Blueprint]", localConfig.getString("database.hostname"), localConfig.getInt("database.port"), localConfig.getString("database.database"), localConfig.getString("database.user"), localConfig.getString("database.password"));
                    } catch (SQLException ex) {
                        Blueprint.error("Failed to connect to database", ex);
                    }
                    break;
                case 13:
                    theDataHub = new PostgreSQL(Logger.getLogger("Minecraft"), "[Blueprint]", localConfig.getString("database.hostname"), localConfig.getInt("database.port"), localConfig.getString("database.database"), localConfig.getString("database.user"), localConfig.getString("database.password"));
                    break;
                default:
                    theDataHub = new SQLite(Logger.getLogger("Minecraft"), "[Blueprint]", Blueprint.getPlugin().getDataFolder() + "/data", localConfig.getString("database.database"));
                    break;
            }
            DataHandler.setDatabaseType(localConfig.getInt("database.type", 0));
            theDataHub.open();
        }
        return theDataHub;
    }
    
    /**
     *
     */
    public static void reloadGreenlistConfig() {
        if (greenlistConfigFile == null) {
            greenlistConfigFile = new File(Blueprint.getPlugin().getDataFolder(), "Greenlist.yml");
        }
        greenlistConfig = YamlConfiguration.loadConfiguration(greenlistConfigFile);

        // Look for defaults in the jar
        InputStream defConfigStream = Blueprint.getPlugin().getResource("Greenlist.yml");
        if (defConfigStream != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
            greenlistConfig.setDefaults(defConfig);
        }
    }

    /**
     *
     * @return
     */
    public static FileConfiguration getGreenlistConfig() {
        if (greenlistConfig == null) {
            reloadGreenlistConfig();
        }
        return greenlistConfig;
    }

    /**
     *
     */
    public static void saveGreenlistConfig() {
        if (greenlistConfig == null || greenlistConfigFile == null) {
            return;
        }
        try {
            getGreenlistConfig().save(greenlistConfigFile);
        } catch (IOException ex) {
            Blueprint.error("Could not save config to " + greenlistConfigFile, ex);
        }
    }

    /**
     *
     */
    public static void reloadBlockBlacklistConfig() {
        if (blockBlacklistConfigFile == null) {
            blockBlacklistConfigFile = new File(Blueprint.getPlugin().getDataFolder(), "List.yml");
        }
       blockBlacklistConfig = YamlConfiguration.loadConfiguration(blockBlacklistConfigFile);

        // Look for defaults in the jar
        InputStream defConfigStream = Blueprint.getPlugin().getResource("List.yml");
        if (defConfigStream != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
            blockBlacklistConfig.setDefaults(defConfig);
        }
    }

    /**
     *
     * @return
     */
    public static FileConfiguration getBlockBlacklistConfig() {
        if (blockBlacklistConfig == null) {
            reloadBlockBlacklistConfig();
        }
        return blockBlacklistConfig;
    }

    /**
     *
     */
    public static void saveBlockBlacklistConfig() {
        if (blockBlacklistConfig == null ||blockBlacklistConfigFile == null) {
            return;
        }
        try {
            getBlockBlacklistConfig().save(blockBlacklistConfigFile);
        } catch (IOException ex) {
            Blueprint.error("Could not save config to " + blockBlacklistConfigFile, ex);
        }
    }

    /**
     *
     */
    public static void reloadCommandsBlacklistConfig() {
        if (commandsBlacklistConfigFile == null) {
            commandsBlacklistConfigFile = new File(Blueprint.getPlugin().getDataFolder(), "Commands.yml");
        }
       commandsBlacklistConfig = YamlConfiguration.loadConfiguration(commandsBlacklistConfigFile);

        // Look for defaults in the jar
        InputStream defConfigStream = Blueprint.getPlugin().getResource("Commands.yml");
        if (defConfigStream != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
            commandsBlacklistConfig.setDefaults(defConfig);
        }
    }

    /**
     *
     * @return
     */
    public static FileConfiguration getCommandsBlacklistConfig() {
        if (commandsBlacklistConfig == null) {
            reloadCommandsBlacklistConfig();
        }
        return commandsBlacklistConfig;
    }

    /**
     *
     */
    public static void saveCommandsBlacklistConfig() {
        if (commandsBlacklistConfig == null ||commandsBlacklistConfigFile == null) {
            return;
        }
        try {
            getCommandsBlacklistConfig().save(commandsBlacklistConfigFile);
        } catch (IOException ex) {
            Blueprint.error("Could not save config to " + commandsBlacklistConfigFile, ex);
        }
    }
}
