package com.emberringstudios.blueprint;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.List;
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
    private volatile static FileConfiguration customConfig = null;
    private volatile static File customConfigFile = null;

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
                    theDataHub = new SQLite(Logger.getLogger("Minecraft"), "[Blueprint]", Blueprint.getPlugin().getDataFolder() + "/data", "users");
                    break;
            }
            DataHandler.setDatabaseType(localConfig.getInt("database.type", 0));
        }
        return theDataHub;
    }

    //public static List<Integer> getGreenList() {

        /*
         ignoreList.add(Material.DEAD_BUSH);
         ignoreList.add(Material.LONG_GRASS);
         ignoreList.add(Material.THIN_GLASS);
         ignoreList.add(Material.DOUBLE_PLANT);
         */
    //}

    public static void reloadCustomConfig() {
        if (customConfigFile == null) {
            customConfigFile = new File(Blueprint.getPlugin().getDataFolder(), "Greenlist.yml");
        }
        customConfig = YamlConfiguration.loadConfiguration(customConfigFile);

        // Look for defaults in the jar
        InputStream defConfigStream = Blueprint.getPlugin().getResource("Greenlist.yml");
        if (defConfigStream != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
            customConfig.setDefaults(defConfig);
        }
    }

    public static FileConfiguration getCustomConfig() {
        if (customConfig == null) {
            reloadCustomConfig();
        }
        return customConfig;
    }

    public static void saveCustomConfig() {
        if (customConfig == null || customConfigFile == null) {
            return;
        }
        try {
            getCustomConfig().save(customConfigFile);
        } catch (IOException ex) {
            Blueprint.error("Could not save config to " + customConfigFile, ex);
        }
    }
}
