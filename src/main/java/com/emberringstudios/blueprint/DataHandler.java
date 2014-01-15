package com.emberringstudios.blueprint;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import lib.PatPeter.SQLibrary.Database;
import lib.PatPeter.SQLibrary.SQLite;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;
import org.yaml.snakeyaml.Yaml;

public class DataHandler {

    private static int databaseType;
    public static final int SQLite = 0;
    public static final int Firebird = 1;
    public static final int DB2 = 2;
    public static final int FrontBase = 3;
    public static final int H2 = 4;
    public static final int Informix = 5;
    public static final int Ingres = 6;
    public static final int MaxDB = 7;
    public static final int MicrosoftSQL = 8;
    public static final int MySQL = 9;
    public static final int Mongo = 10;
    public static final int mSQL = 11;
    public static final int Oracle = 12;
    public static final int PostgreSQL = 13;

    public static int getDatabaseType() {
        return databaseType;
    }

    public static void setDatabaseType(final int aDatabaseType) {
        databaseType = aDatabaseType;
    }

    public static void addPlayerBlock(final String name, final ItemStack item, final Block placedBlock) {
        try {
            if (Integer.parseInt(query("SELECT COUNT(*) AS Count FROM players WHERE playerID = '" + name + "';").get(0).getKey("Count")) == 0) {
                setOriginalPlayerGameMode(name, GameMode.SURVIVAL);
            }
            query("INSERT INTO blocks (playerID,  itemID, itemMeta, blockID, blockX, blockY, blockZ, blockMeta, world) VALUES ('" + name + "', " + item.getType().getId() + ", '" + (int) item.getData().getData() + "', '" + placedBlock.getType().getId() + "', " + placedBlock.getX() + ", " + placedBlock.getY() + ", " + placedBlock.getZ() + ", " + (int) placedBlock.getData() + ", '" + placedBlock.getWorld().getName() + "');");
        } catch (SQLException ex) {
            Blueprint.error("Couldn't add block to player", ex);
        }
    }

    public static void addPlayerBlock(final String name, final int itemID, final int itemData, final Block placedBlock) {
        try {
            if (Integer.parseInt(query("SELECT COUNT(*) AS Count FROM players WHERE playerID = '" + name + "';").get(0).getKey("Count")) == 0) {
                setOriginalPlayerGameMode(name, GameMode.SURVIVAL);
            }
            query("INSERT INTO blocks (playerID,  itemID, itemMeta, blockID, blockX, blockY, blockZ, blockMeta, world) VALUES ('" + name + "', " + itemID + ", '" + itemData + "', '" + placedBlock.getType().getId() + "', " + placedBlock.getX() + ", " + placedBlock.getY() + ", " + placedBlock.getZ() + ", " + (int) placedBlock.getData() + ", '" + placedBlock.getWorld().getName() + "');");
        } catch (SQLException ex) {
            Blueprint.error("Couldn't add block to player", ex);
        }
    }

    public static void removePlayerBlock(final String name, final BlockData placedBlock, final String world) {
        try {
            if (Integer.parseInt(query("SELECT COUNT(*) AS Count FROM players WHERE playerID = '" + name + "';").get(0).getKey("Count")) == 0) {
                setOriginalPlayerGameMode(name, GameMode.SURVIVAL);
            }
            query("DELETE FROM blocks WHERE playerID = '" + name
                    + "' AND blockID = '" + placedBlock.getType()
                    + "' AND blockX = " + placedBlock.getX()
                    + " AND blockY = " + placedBlock.getY()
                    + " AND blockZ = " + placedBlock.getZ()
                    + " AND blockMeta = " + (int) placedBlock.getData()
                    + " AND world = '" + world + "';");
        } catch (SQLException ex) {
            Blueprint.error("Couldn't activate player", ex);
        }
    }

    public static void removePlayerBlock(final String name, final Block placedBlock, final String world) {
        try {
            if (Integer.parseInt(query("SELECT COUNT(*) AS Count FROM players WHERE playerID = '" + name + "';").get(0).getKey("Count")) == 0) {
                setOriginalPlayerGameMode(name, GameMode.SURVIVAL);
            }
            query("DELETE FROM blocks WHERE playerID = '" + name
                    + "' AND blockID = '" + placedBlock.getType().getId()
                    + "' AND blockX = " + placedBlock.getX()
                    + " AND blockY = " + placedBlock.getY()
                    + " AND blockZ = " + placedBlock.getZ()
                    + " AND blockMeta = " + (int) placedBlock.getData()
                    + " AND world = '" + world + "';");
        } catch (SQLException ex) {
            Blueprint.error("Couldn't activate player", ex);
        }
    }

    public static boolean checkPlayerBlock(final String name, final int lockX, final int lockY, final int lockZ, final String world) {
        boolean found = false;
        try {
            if (Integer.parseInt(query("SELECT COUNT(*) AS Count FROM players WHERE playerID = '" + name + "';").get(0).getKey("Count")) == 0) {
                setOriginalPlayerGameMode(name, GameMode.SURVIVAL);
            }
            found = Integer.parseInt(query("SELECT COUNT(*) AS Count FROM blocks WHERE playerID = '" + name
                    + "' AND blockX = " + lockX
                    + " AND blockY = " + lockY
                    + " AND blockZ = " + lockZ
                    + " AND world = '" + world + "';").get(0).getKey("Count")) > 0;
        } catch (SQLException ex) {
            Blueprint.error("Couldn't activate player", ex);
        }
        return found;
    }

    public static boolean checkPlayerBlock(final String name, final Block placedBlock) {
        boolean found = false;
        try {
            if (Integer.parseInt(query("SELECT COUNT(*) AS Count FROM players WHERE playerID = '" + name + "';").get(0).getKey("Count")) == 0) {
                setOriginalPlayerGameMode(name, GameMode.SURVIVAL);
            }
            found = Integer.parseInt(query("SELECT COUNT(*) AS Count FROM blocks WHERE playerID = '" + name
                    + "' AND blockID = '" + placedBlock.getType().getId()
                    + "' AND blockX = " + placedBlock.getX()
                    + " AND blockY = " + placedBlock.getY()
                    + " AND blockZ = " + placedBlock.getZ()
                    + " AND blockMeta = " + (int) placedBlock.getData()
                    + " AND world = '" + placedBlock.getWorld().getName() + "';").get(0).getKey("Count")) > 0;
        } catch (SQLException ex) {
            Blueprint.error("Couldn't activate player", ex);
        }
        return found;
    }

    public static boolean checkPlayerBlockNoData(final String name, final Block placedBlock) {
        boolean found = false;
        try {
            if (Integer.parseInt(query("SELECT COUNT(*) AS Count FROM players WHERE playerID = '" + name + "';").get(0).getKey("Count")) == 0) {
                setOriginalPlayerGameMode(name, GameMode.SURVIVAL);
            }
            found = Integer.parseInt(query("SELECT COUNT(*) AS Count FROM blocks WHERE playerID = '" + name
                    + "' AND blockID = '" + placedBlock.getType().getId()
                    + "' AND blockX = " + placedBlock.getX()
                    + " AND blockY = " + placedBlock.getY()
                    + " AND blockZ = " + placedBlock.getZ()
                    + " AND world = '" + placedBlock.getWorld().getName() + "';").get(0).getKey("Count")) > 0;
        } catch (SQLException ex) {
            Blueprint.error("Couldn't activate player", ex);
        }
        return found;
    }

    public static GameMode getOriginalPlayerGameMode(final String name) {
        GameMode result = null;
        try {
            if (Integer.parseInt(query("SELECT COUNT(*) AS Count FROM players WHERE playerID = '" + name + "';").get(0).getKey("Count")) == 0) {
                setOriginalPlayerGameMode(name, GameMode.SURVIVAL);
            }
            int temp = Integer.parseInt(query("SELECT gameMode FROM players WHERE playerID = '" + name + "';").get(0).getKey("gameMode"));
            switch (temp) {
                case 1:
                    result = GameMode.ADVENTURE;
                    break;
                case 0:
                default:
                    result = GameMode.SURVIVAL;
                    break;
            }
        } catch (SQLException ex) {
            Blueprint.error("Could not retrieve game mode", ex);
        }
        return result;
    }

    public static boolean setOriginalPlayerGameMode(final String name, final GameMode originalGameMode) {
        try {
            int gameMode = originalGameMode == GameMode.ADVENTURE ? 1 : 0;
            if (Integer.parseInt(query("SELECT COUNT(*) AS Count FROM players WHERE playerID = '" + name + "';").get(0).getKey("Count")) == 0) {
                query("INSERT INTO players (playerID, gameMode) VALUES ('" + name + "', " + gameMode + ");");
            } else {
                query("UPDATE players SET gameMode = " + gameMode + " WHERE playerID = '" + name + "';");
            }
        } catch (SQLException ex) {
            Blueprint.error("Could not alter table data", ex);
            return false;
        }

        return true;
    }

    public static void setPlayerLocation(final String name, final Location place) {
        setPlayerLocation(name, place.getX(), place.getY(), place.getZ());
    }

    public static void setPlayerLocation(final String name, final double locX, final double locY, final double locZ) {

        try {
            if (Integer.parseInt(query("SELECT COUNT(*) AS Count FROM players WHERE playerID = '" + name + "';").get(0).getKey("Count")) == 0) {
                query("INSERT INTO players (playerID, locX, locY, locZ, gameMode) VALUES ('" + name + "', " + locX + ", " + locY + ", " + locZ + ", 0);");
            } else {
                query("UPDATE players SET locX =  " + locX + ", locY =  " + locY + ", locZ = " + locZ + " WHERE playerID = '" + name + "';");
            }
        } catch (SQLException ex) {
            Blueprint.error("Could not alter table data", ex);

        }
    }

    public static BasicLocation getPlayerLocation(final String name) {
        BasicLocation baseLoc = new BasicLocation(0, 0, 0);

        try {
            if (Integer.parseInt(query("SELECT COUNT(*) AS Count FROM players WHERE playerID = '" + name + "';").get(0).getKey("Count")) == 0) {
                setOriginalPlayerGameMode(name, GameMode.SURVIVAL);
            }
            ResultData returned = query("SELECT locX, locY, locZ FROM players WHERE playerID = '" + name + "';").get(0);
            baseLoc = new BasicLocation(Double.parseDouble(returned.getKey("locX")), Double.parseDouble(returned.getKey("locY")), Double.parseDouble(returned.getKey("locZ")));
        } catch (SQLException ex) {
            Blueprint.error("Could not retrieve game mode", ex);
        }
        return baseLoc;
    }

    public static void activatePlayer(final String name, final String invData, final String armData) {
        try {
            if (Integer.parseInt(query("SELECT COUNT(*) AS Count FROM players WHERE playerID = '" + name + "';").get(0).getKey("Count")) == 0) {
                setOriginalPlayerGameMode(name, GameMode.SURVIVAL);
            }
            query("UPDATE players SET active = 1 WHERE playerID = '" + name + "';");
        } catch (SQLException ex) {
            Blueprint.error("Couldn't activate player", ex);
        }
        setPlayerInventory(name, invData, armData);
    }

    public static PlayerInventory deactivatePlayer(final String name) {
        try {
            if (Integer.parseInt(query("SELECT COUNT(*) AS Count FROM players WHERE playerID = '" + name + "';").get(0).getKey("Count")) == 0) {
                setOriginalPlayerGameMode(name, GameMode.SURVIVAL);
            }
            query("UPDATE players SET active = 0 WHERE playerID = '" + name + "';");
        } catch (SQLException ex) {

            Blueprint.error("Couldn't deactivate player", ex);
        }
        return getAndDeserializeFullPlayerInventory(name);
    }

    public static boolean isPlayerActive(final String name) {
        boolean result = false;

        try {
            result = Integer.parseInt(query("SELECT COUNT(*) AS Count FROM players WHERE playerID = '" + name + "' AND active = 1;").get(0).getKey("Count")) != 0;
        } catch (SQLException ex) {

            Blueprint.error("Couldn't deactivate player", ex);
        }
        return result;
    }

    public static List<BlockData> getBlueprint(final String playerID, final String worldID) {
        List<BlockData> blockList = new CopyOnWriteArrayList();
        try {
            List<ResultData> result = query("SELECT blockID, blockX, blockY, blockZ, blockMeta FROM blocks WHERE playerID = '" + playerID + "' AND world = '" + worldID + "';");
            for (ResultData data : result) {
                blockList.add(new BlockData(Integer.parseInt(data.getKey("blockID")), Integer.parseInt(data.getKey("blockX")), Integer.parseInt(data.getKey("blockY")), Integer.parseInt(data.getKey("blockZ")), Byte.parseByte(data.getKey("blockMeta")), Bukkit.getWorld(worldID)));
            }
        } catch (SQLException ex) {
            Blueprint.error("Couldn't deactivate player", ex);
        }
        return blockList;
    }

    public static void setPlayerInventory(final String playerId, final String inv, final String arm) {
        try {
            query("UPDATE players SET inventory = '" + inv + "', armour = '" + arm + "'  WHERE playerID = '" + playerId + "';");
        } catch (SQLException ex) {
            Blueprint.error("Couldn't deactivate player", ex);
        }

    }

    public static String getPlayerInventory(String playerId) {
        String result = "";
        try {
            result = query("SELECT inventory FROM players  WHERE playerID = '" + playerId + "';").get(0).getKey("inventory");
        } catch (SQLException ex) {
            Blueprint.error("Couldn't deactivate player", ex);
        }
        return result;
    }

    public static String getPlayerArmour(String playerId) {
        String result = "";
        try {
            result = query("SELECT armour FROM players  WHERE playerID = '" + playerId + "';").get(0).getKey("armour");
        } catch (SQLException ex) {
            Blueprint.error("Couldn't deactivate player", ex);
        }
        return result;
    }

    public static PlayerInventory getAndDeserializeFullPlayerInventory(final String playerId) {
        String inv = getPlayerInventory(playerId);
        String arm = getPlayerArmour(playerId);
        Yaml tempSer = new Yaml();
        List<Map<String, Object>> invPre = (List<Map<String, Object>>) tempSer.load(inv);
        List<Map<String, Object>> armPre = (List<Map<String, Object>>) tempSer.load(arm);
        List<ConfigurationSerializable> invMid = ItemSerial.deserializeItemList(invPre);
        List<ConfigurationSerializable> armMid = ItemSerial.deserializeItemList(armPre);
        ItemStack[] invPost = invMid.toArray(new ItemStack[invMid.size()]);
        ItemStack[] armPost = armMid.toArray(new ItemStack[armMid.size()]);
        return new PlayerInventory(invPost, armPost);
    }

    public static void updatePlayerBlock(final String name, final Block clickedBlock) {
        try {
            if (Integer.parseInt(query("SELECT COUNT(*) AS Count FROM players WHERE playerID = '" + name + "';").get(0).getKey("Count")) == 0) {
                setOriginalPlayerGameMode(name, GameMode.SURVIVAL);
            }
            query("UPDATE blocks SET blockMeta = " + (int) clickedBlock.getData() + "  WHERE"
                    + " playerID = '" + name
                    + "' AND blockID = '" + clickedBlock.getType()
                    + "' AND blockX = " + clickedBlock.getX()
                    + " AND blockY = " + clickedBlock.getY()
                    + " AND blockZ = " + clickedBlock.getZ()
                    + " AND world = '" + clickedBlock.getWorld().getName() + "';");
        } catch (SQLException ex) {
            Blueprint.error("Couldn't activate player", ex);
        }
    }

    public static boolean addPlayerChest(final String name, final Block placedBlock) {
        try {
            if (Integer.parseInt(query("SELECT COUNT(*) AS Count FROM players WHERE playerID = '" + name + "';").get(0).getKey("Count")) == 0) {
                setOriginalPlayerGameMode(name, GameMode.SURVIVAL);
            }
            if (!isPlayerChest(placedBlock)) {
                query("INSERT INTO chests (playerID, blockX, blockY, blockZ, world) VALUES ('" + name + "', " + placedBlock.getX() + ", " + placedBlock.getY() + ", " + placedBlock.getZ() + ", '" + placedBlock.getWorld().getName() + "');");
                return true;
            }
        } catch (SQLException ex) {
            Blueprint.error("Couldn't activate player", ex);
        }
        return false;
    }

    public static void setupDB() {
        try {
            switch (databaseType) {
                case 0:
                    query("CREATE TABLE IF NOT EXISTS players (\n"
                            + " id INTEGER PRIMARY KEY AUTOINCREMENT,\n"
                            + " playerID TEXT NOT NULL,\n"
                            + " locX REAL NOT NULL DEFAULT 0,\n"
                            + " locY REAL NOT NULL DEFAULT 0,\n"
                            + " locZ REAL NOT NULL DEFAULT 0,\n"
                            + " gameMode INTEGER NOT NULL,\n"
                            + " inventory MEDIUMTEXT NOT NULL DEFAULT '',\n"
                            + " armour MEDIUMTEXT NOT NULL DEFAULT '',\n"
                            + " active INTEGER NOT NULL DEFAULT 0);");

                    query("CREATE TABLE IF NOT EXISTS blocks (\n"
                            + " id INTEGER PRIMARY KEY AUTOINCREMENT,\n"
                            + " playerID TEXT NOT NULL,\n"
                            + " itemID INTEGER NOT NULL,\n"
                            + " itemMeta MEDIUMTEXT NOT NULL,\n"
                            + " blockID TEXT NOT NULL,\n"
                            + " blockX INTEGER NOT NULL,\n"
                            + " blockY INTEGER NOT NULL,\n"
                            + " blockZ INTEGER NOT NULL,\n"
                            + " blockMeta INTEGER NOT NULL,\n"
                            + " world TEXT NOT NULL);");

                    query("CREATE TABLE IF NOT EXISTS chests (\n"
                            + " id INTEGER PRIMARY KEY AUTOINCREMENT,\n"
                            + " playerID TEXT NOT NULL,\n"
                            + " world TEXT NOT NULL,\n"
                            + " blockX INTEGER NOT NULL,\n"
                            + " blockY INTEGER NOT NULL,\n"
                            + " blockZ INTEGER NOT NULL);");
                    break;
                case 9:
                default:
                    query("CREATE TABLE IF NOT EXISTS players (\n"
                            + " id INT NOT NULL AUTO_INCREMENT,\n"
                            + " playerID VARCHAR(45) NOT NULL,\n"
                            + " locX DECIMAL NOT NULL DEFAULT 0,\n"
                            + " locY DECIMAL NOT NULL DEFAULT 0,\n"
                            + " locZ DECIMAL NOT NULL DEFAULT 0,\n"
                            + " gameMode INT NOT NULL,\n"
                            + " inventory MEDIUMTEXT NOT NULL DEFAULT '',\n"
                            + " armour MEDIUMTEXT NOT NULL DEFAULT '',\n"
                            + " active INT NOT NULL DEFAULT 0,\n"
                            + " PRIMARY KEY (id),\n"
                            + " UNIQUE INDEX id_UNIQUE (id ASC),\n"
                            + " UNIQUE INDEX playerName_UNIQUE (playerName ASC));");

                    query("CREATE TABLE IF NOT EXISTS blocks (\n"
                            + " id INT NOT NULL AUTO_INCREMENT,\n"
                            + " playerID VARCHAR(45) NOT NULL,\n"
                            + " itemID INT NOT NULL,\n"
                            + " itemMeta MEDIUMTEXT NOT NULL,\n"
                            + " blockID INT NOT NULL,\n"
                            + " blockX INT NOT NULL,\n"
                            + " blockY INT NOT NULL,\n"
                            + " blockZ INT NOT NULL,\n"
                            + " blockMeta INT NOT NULL,\n"
                            + " world VARCHAR(45) NOT NULL,\n"
                            + " PRIMARY KEY (id));");

                    query("CREATE TABLE IF NOT EXISTS chests (\n"
                            + " id INT NOT NULL AUTO_INCREMENT,\n"
                            + " playerID VARCHAR(45) NOT NULL,\n"
                            + " world VARCHAR(45) NOT NULL,\n"
                            + " blockX INT NOT NULL,\n"
                            + " blockY INT NOT NULL,\n"
                            + " blockZ INT NOT NULL,\n"
                            + " PRIMARY KEY (id));");
            }
        } catch (SQLException ex) {
            Blueprint.error("Could not create needed table", ex);
        }
    }

    public static List<String> getPlayerIds() {
        List<String> playerIds = new CopyOnWriteArrayList();
        try {
            List<ResultData> query = query("SELECT playerId FROM players;");
            for (ResultData data : query) {
                playerIds.add(data.getKey("playerId"));
            }
        } catch (SQLException ex) {
            Blueprint.error("Couldn't activate player", ex);
        }
        return playerIds;
    }

    public static List<Location> getPlayerChestLocations(final String name) {
        List<Location> locations = new CopyOnWriteArrayList();
        try {
            List<ResultData> query = query("SELECT world, blockX, blockY, blockZ FROM chests WHERE playerID = '" + name + "';");
            for (ResultData data : query) {
                locations.add(new Location(Blueprint.getPlugin().getServer().getWorld(data.getKey("world")), Double.parseDouble(data.getKey("blockX")), Double.parseDouble(data.getKey("blockY")), Double.parseDouble(data.getKey("blockZ"))));
            }
        } catch (SQLException ex) {
            Blueprint.error("Couldn't activate player", ex);
        }
        return locations;
    }

    public static List<BlockData> getBlueprintAllWorlds(final String playerID) {
        List<BlockData> blockList = new CopyOnWriteArrayList();
        try {
            List<ResultData> result = query("SELECT world, blockID, blockX, blockY, blockZ, blockMeta FROM blocks WHERE playerID = '" + playerID + "';");
            for (ResultData data : result) {
                blockList.add(new BlockData(Integer.parseInt(data.getKey("blockID")), Integer.parseInt(data.getKey("blockX")), Integer.parseInt(data.getKey("blockY")), Integer.parseInt(data.getKey("blockZ")), Byte.parseByte(data.getKey("blockMeta")), Bukkit.getWorld(data.getKey("world"))));
            }
        } catch (SQLException ex) {
            Blueprint.error("Couldn't deactivate player", ex);
        }
        return blockList;
    }

    public static List<Integer> getBlueprintBlockTypes(final String playerID, final String worldID) {
        List<Integer> blockList = new CopyOnWriteArrayList();
        try {
            List<ResultData> result = query("SELECT DISTINCT  blockID FROM blocks WHERE playerID = '" + playerID + "' AND world = '" + worldID + "';");
            for (ResultData data : result) {
                blockList.add(Integer.parseInt(data.getKey("blockID")));
            }
        } catch (SQLException ex) {
            Blueprint.error("Couldn't deactivate player", ex);
        }
        return blockList;
    }

    public static int getBlueprintBlockOfTypInWorldNeeded(String playerID, int mat, String worldID) {
        int count = 0;
        try {
            count = Integer.parseInt(query("SELECT COUNT(*) AS Count FROM blocks WHERE playerID = '" + playerID + "' AND world = '" + worldID + "' AND blockID =" + mat + ";").get(0).getKey("Count"));

        } catch (SQLException ex) {
            Blueprint.error("Couldn't deactivate player", ex);
        }
        return count;
    }

    public static List<BlockData> getBlueprintBuildBlockOfTypInWorld(String playerID, int mat, String worldID) {
        List<BlockData> blockList = new CopyOnWriteArrayList();
        try {
            List<ResultData> result = query("SELECT blockX, blockY, blockZ, blockMeta FROM blocks WHERE playerID = '" + playerID + "' AND world = '" + worldID + "' AND blockID = " + mat + ";");
            for (ResultData data : result) {
                blockList.add(new BlockData(mat,
                        Integer.parseInt(data.getKey("blockX")),
                        Integer.parseInt(data.getKey("blockY")),
                        Integer.parseInt(data.getKey("blockZ")),
                        Byte.parseByte(data.getKey("blockMeta")),
                        Bukkit.getWorld(worldID)));
            }
        } catch (SQLException ex) {
            Blueprint.error("Couldn't deactivate player", ex);
        }
        return blockList;
    }

    public static boolean isBlueprintBlock(final Material blockType, final Location placedBlock) {
        return isBlueprintBlock(blockType.getId(), placedBlock);
    }

    public static boolean isBlueprintBlock(final int blockTypeID, final Location placedBlock) {

        return isBlueprintBlock(blockTypeID, (int) (placedBlock.getX() + 0.5D), (int) (placedBlock.getY() + 0.5D), (int) (placedBlock.getZ() + 0.5D), placedBlock.getWorld().getName());
    }

    public static boolean isBlueprintBlock(final Block placedBlock) {
        return isBlueprintBlock(placedBlock.getType().getId(), placedBlock.getX(), placedBlock.getY(), placedBlock.getZ(), placedBlock.getWorld().getName());
    }

    public static boolean isBlueprintBlock(int blockTypeID, final int locX, final int locY, final int locZ, final String worldID) {
        boolean found = false;
        if (blockTypeID == Material.PISTON_MOVING_PIECE.getId()) {
            blockTypeID = Material.TNT.getId();
        }
        try {
            found = Integer.parseInt(query("SELECT COUNT(*) AS Count FROM blocks WHERE  blockID = '" + blockTypeID
                    + "' AND blockX = " + locX
                    + " AND blockY = " + locY
                    + " AND blockZ = " + locZ
                    + " AND world = '" + worldID + "';").get(0).getKey("Count")) > 0;
        } catch (SQLException ex) {
            Blueprint.error("Couldn't activate player", ex);
        }
        return found;
    }

    private static List<ResultData> query(final String query) throws SQLException {
        List<ResultData> data = new CopyOnWriteArrayList();
        synchronized (ConfigHandler.getTheDataHub()) {
            final Database tempDB = ConfigHandler.getTheDataHub();
            if (tempDB instanceof SQLite ? !tempDB.open() : !tempDB.isOpen() && !tempDB.open()) {
                Blueprint.error("Could not work with database");
            }
            try {
                ResultSet result = tempDB.query(query);
                ResultSetMetaData meta = result.getMetaData();
                List<String> columns = new CopyOnWriteArrayList();
                for (int i = 1; i <= meta.getColumnCount(); i++) {
                    columns.add(meta.getColumnName(i));
                }
                while (result.next()) {
                    ResultData temp = new ResultData();
                    for (int col = 0; col < columns.size(); col++) {
                        temp.setKey(columns.get(col), result.getString(columns.get(col)));
                    }
                    data.add(temp);
                }
                result.close();
            } catch (SQLException ex) {
                throw ex;
            }
            tempDB.close();
        }
        return data;
    }

    public static boolean isPlayerChest(Block placedBlock) {
        try {
            return Integer.parseInt(query("SELECT COUNT(*) AS Count FROM chests WHERE blockX = " + placedBlock.getX() + " AND blockY = " + placedBlock.getY() + " AND blockZ  = " + placedBlock.getX() + ";").get(0).getKey("Count")) > 0;
        } catch (SQLException ex) {
            Blueprint.error("Couldn't activate player", ex);
        }
        return false;
    }

    public static void updateBlock(Block clickedBlock) {
        try {
            query("UPDATE blocks SET blockMeta = " + (int) clickedBlock.getData() + "  WHERE"
                    + " blockID = '" + clickedBlock.getType().getId()
                    + "' AND blockX = " + clickedBlock.getX()
                    + " AND blockY = " + clickedBlock.getY()
                    + " AND blockZ = " + clickedBlock.getZ()
                    + " AND world = '" + clickedBlock.getWorld().getName() + "';");
        } catch (SQLException ex) {
            Blueprint.error("Couldn't activate player", ex);
        }
    }

    public static boolean isBlueprintBlockAtLocation(final Location location) {
        return isBlueprintBlockAtLocation(location.getBlockX(), location.getBlockY(), location.getBlockZ(), location.getWorld().getName());
    }

    public static boolean isBlueprintBlockAtLocation(final int locX, final int locY, final int locZ, final String worldID) {
        boolean found = false;
        try {
            found = Integer.parseInt(query("SELECT COUNT(*) AS Count FROM blocks WHERE   blockX = " + locX
                    + " AND blockY = " + locY
                    + " AND blockZ = " + locZ
                    + " AND world = '" + worldID + "';").get(0).getKey("Count")) > 0;
        } catch (SQLException ex) {
            Blueprint.error("Couldn't activate player", ex);
        }
        return found;
    }

    public static List<Integer> getBlueprintItemTypes(final String playerID, final String worldID) {
        List<Integer> blockList = new CopyOnWriteArrayList();
        try {
            List<ResultData> result = query("SELECT DISTINCT  itemID FROM blocks WHERE playerID = '" + playerID + "' AND world = '" + worldID + "';");
            for (ResultData data : result) {
                blockList.add(Integer.parseInt(data.getKey("itemID")));
            }
        } catch (SQLException ex) {
            Blueprint.error("Couldn't deactivate player", ex);
        }
        return blockList;
    }

    public static int getBlueprintBlockOfTypInWorldNeededFromItem(final String playerID, final int mat, final String worldID) {
        int count = 0;
        try {
            count = Integer.parseInt(query("SELECT COUNT(*) AS Count FROM blocks WHERE playerID = '" + playerID + "' AND world = '" + worldID + "' AND itemID =" + mat + ";").get(0).getKey("Count"));

        } catch (SQLException ex) {
            Blueprint.error("Couldn't deactivate player", ex);
        }
        return count;
    }

    public static List<BlockData> getBlueprintBuildBlockOfTypInWorldFromItem(String playerID, int mat, String worldID) {
        List<BlockData> blockList = new CopyOnWriteArrayList();
        try {
            List<ResultData> result = query("SELECT blockID, blockX, blockY, blockZ, blockMeta FROM blocks WHERE playerID = '" + playerID + "' AND world = '" + worldID + "' AND itemID = " + mat + ";");
            for (ResultData data : result) {
                blockList.add(new BlockData(Integer.parseInt(data.getKey("blockID")),
                        Integer.parseInt(data.getKey("blockX")),
                        Integer.parseInt(data.getKey("blockY")),
                        Integer.parseInt(data.getKey("blockZ")),
                        Byte.parseByte(data.getKey("blockMeta")),
                        Bukkit.getWorld(worldID)));
            }
        } catch (SQLException ex) {
            Blueprint.error("Couldn't deactivate player", ex);
        }
        return blockList;
    }

    public static String getBlockOwnerAtLocation(final Location loc) {
        String result = null;
        try {
            result = query("SELECT playerID FROM blocks WHERE  world = '" + loc.getWorld().getName() + "' AND blockX = " + loc.getBlockX() + " AND blockY = " + loc.getBlockY() + " AND blockZ = " + loc.getBlockZ() + ";").get(0).getKey("playerID");
        } catch (SQLException ex) {
            Blueprint.error("Couldn't deactivate player", ex);
        }
        return result;
    }
}
