package com.emberringstudios.blueprint;

import java.sql.SQLException;
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

/**
 *
 * @author Max9403 <Max9403@live.com>
 */
public class ConfigHandler {

    private static FileConfiguration defaultBukkitConfig = Blueprint.getPlugin().getConfig();
    private static Database theDataHub;

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
        return theDataHub;
    }
}
