package com.emberringstudios.blueprint;

import com.emberringstudios.blueprint.queries.QueryData;
import com.emberringstudios.blueprint.queries.ResultData;
import com.emberringstudios.blueprint.queries.QueryCallback;
import com.emberringstudios.blueprint.background.QueryProcessor;
import com.emberringstudios.blueprint.blockdata.BlockDataCache;
import com.emberringstudios.blueprint.blockdata.BlockDataChest;
import com.emberringstudios.blueprint.blockdata.BlockData;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.LazyMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.yaml.snakeyaml.Yaml;

/**
 *
 * @author Benjamin
 */
public class DataHandler {

    private static volatile int databaseType = ConfigHandler.getDefaultBukkitConfig().getInt("database.type", 0);

    /**
     *
     */
    public static final int SQLite = 0;

    /**
     *
     */
    public static final int Firebird = 1;

    /**
     *
     */
    public static final int DB2 = 2;

    /**
     *
     */
    public static final int FrontBase = 3;

    /**
     *
     */
    public static final int H2 = 4;

    /**
     *
     */
    public static final int Informix = 5;

    /**
     *
     */
    public static final int Ingres = 6;

    /**
     *
     */
    public static final int MaxDB = 7;

    /**
     *
     */
    public static final int MicrosoftSQL = 8;

    /**
     *
     */
    public static final int MySQL = 9;

    /**
     *
     */
    public static final int Mongo = 10;

    /**
     *
     */
    public static final int mSQL = 11;

    /**
     *
     */
    public static final int Oracle = 12;

    /**
     *
     */
    public static final int PostgreSQL = 13;
    private static volatile ConcurrentHashMap<String, PlayerData> activeUsers = new ConcurrentHashMap();
    private static volatile ConcurrentHashMap<String, BlockDataCache> blocks = new ConcurrentHashMap();
    private static volatile ConcurrentHashMap<String, BlockDataChest> chests = new ConcurrentHashMap();
    private static volatile ConcurrentHashMap<Player, Player> markModeUsers = new ConcurrentHashMap();
    private static volatile ConcurrentHashMap<Player, Player> unmarkModeUsers = new ConcurrentHashMap();

    /**
     *
     * @return
     */
    public static int getDatabaseType() {
        return databaseType;
    }

    public static boolean inUnmarkMode(Player player) {
        return unmarkModeUsers.containsKey(player);
    }

    public static boolean inMarkMode(Player player) {
        return markModeUsers.containsKey(player);
    }

    public static Player getUnmarkMode(Player player) {
        return unmarkModeUsers.get(player);
    }

    public static void putUnmarkMode(Player player, Player player2) {
        unmarkModeUsers.put(player, player2);
    }

    public static void addUnmarkMode(Player player) {
        unmarkModeUsers.put(player, player);
    }

    public static void removeUnmarkMode(Player player) {
        unmarkModeUsers.remove(player);
    }

    public static Player getMarkMode(Player player) {
        return markModeUsers.get(player);
    }

    public static void putMarkMode(Player player, Player player2) {
        markModeUsers.put(player, player2);
    }

    public static void addMarkMode(Player player) {
        markModeUsers.put(player, player);
    }

    public static void removeMarkMode(Player player) {
        markModeUsers.remove(player);
    }

    public static void unmarkPlayer(Player player, Block block) {
        if (player != null && inUnmarkMode(player)) {
            if (DataHandler.isPlayerChest(block) && DataHandler.getPlayerChestLocations(ConfigHandler.getDefaultBukkitConfig().getBoolean("use.UUIDs", true) ? player.getUniqueId().toString() : player.getName()).contains(new BlockDataChest(block, ConfigHandler.getDefaultBukkitConfig().getBoolean("use.UUIDs", true) ? player.getUniqueId().toString() : player.getName()))) {
                DataHandler.removePlayerChest(ConfigHandler.getDefaultBukkitConfig().getBoolean("use.UUIDs", true) ? player.getUniqueId().toString() : player.getName(), block);
                unmarkModeUsers.remove(player);
                player.sendMessage("Resource chest unmarked");
            } else {
                player.sendMessage("This chest is not marked");
            }
        }
    }

    public static void markPlayer(Player player, Block block) {
        if (player != null && inMarkMode(player)) {
            if (!DataHandler.isPlayerChest(block)) {
                DataHandler.addPlayerChest(ConfigHandler.getDefaultBukkitConfig().getBoolean("use.UUIDs", true) ? player.getUniqueId().toString() : player.getName(), block);
                markModeUsers.remove(player);
                player.sendMessage("Resource chest marked");
            } else {
                player.sendMessage("This chest is already marked");
            }
        }
    }

    /**
     *
     * @param aDatabaseType
     */
    public static void setDatabaseType(final int aDatabaseType) {
        databaseType = aDatabaseType;
    }

    /**
     *
     * @param name
     * @param item
     * @param placedBlock
     */
    public static void addPlayerBlock(final String name, final ItemStack item, final BlockData placedBlock) {
        addPlayerBlock(name, item.getTypeId(), item.getData().getData(), placedBlock);
    }

    /**
     *
     * @param name
     * @param itemID
     * @param itemData
     * @param placedBlock
     */
    public static void addPlayerBlock(final String name, final int itemID, final short itemData, final BlockData placedBlock) {
        if (blocks.get(placedBlock.convertToKey()) == null) {
            blocks.put(placedBlock.convertToKey(), new BlockDataCache(placedBlock, name, itemID, (byte) itemData));
            activeUsers.get(name).getPlayerBlocks().put(placedBlock.convertToKey(), new BlockDataCache(placedBlock, name, itemID, (byte) itemData));
            QueryProcessor.addQuery(new QueryData("SELECT COUNT(*) AS Count FROM " + ConfigHandler.getDefaultBukkitConfig().getString("database.prefix", "blueprint") + "_players WHERE playerID = '" + name + "';", new QueryCallback() {
                public void result(List<ResultData> result) {
                    if (Integer.parseInt(result.get(0).getKey("Count")) == 0) {
                        setOriginalPlayerGameMode(name, GameMode.SURVIVAL);
                    }
                    QueryProcessor.addQuery(new QueryData("INSERT INTO " + ConfigHandler.getDefaultBukkitConfig().getString("database.prefix", "blueprint") + "_blocks (playerID,  itemID, itemMeta, blockID, blockX, blockY, blockZ, blockMeta, world) VALUES ('" + name + "', " + itemID + ", '" + itemData + "', '" + placedBlock.getType() + "', " + placedBlock.getX() + ", " + placedBlock.getY() + ", " + placedBlock.getZ() + ", " + (int) placedBlock.getData() + ", '" + placedBlock.getBlockWorld().getName() + "');", new QueryCallback() {
                        public void result(List<ResultData> result) {
                        }
                    }));
                }
            }));
        } else {
            updatePlayerBlockAndItem(name, new ItemStack(itemID, 1, itemData), placedBlock);
        }
    }

    /**
     *
     * @param name
     * @param placedBlock
     * @param world
     */
    public static void removePlayerBlock(final String name, final BlockData placedBlock, final String world) {
        blocks.remove(placedBlock.convertToKey());
        activeUsers.get(name).getPlayerBlocks().remove(placedBlock.convertToKey());
        QueryProcessor.addQuery(new QueryData("SELECT COUNT(*) AS Count FROM " + ConfigHandler.getDefaultBukkitConfig().getString("database.prefix", "blueprint") + "_players WHERE playerID = '" + name + "';", new QueryCallback() {
            public void result(List<ResultData> result) {
                if (Integer.parseInt(result.get(0).getKey("Count")) == 0) {
                    setOriginalPlayerGameMode(name, GameMode.SURVIVAL);
                }
                QueryProcessor.addQuery(new QueryData("DELETE FROM " + ConfigHandler.getDefaultBukkitConfig().getString("database.prefix", "blueprint") + "_blocks WHERE playerID = '" + name
                        + "' AND blockID = '" + placedBlock.getType()
                        + "' AND blockX = " + placedBlock.getX()
                        + " AND blockY = " + placedBlock.getY()
                        + " AND blockZ = " + placedBlock.getZ()
                        + " AND blockMeta = " + (int) placedBlock.getData()
                        + " AND world = '" + world + "';", new QueryCallback() {
                            public void result(List<ResultData> result) {
                            }
                        }));
            }
        }));
    }

    /**
     *
     * @param name
     * @param placedBlock
     * @param world
     */
    public static void removePlayerBlock(final String name, final Block placedBlock, final String world) {
        final BlockData bd = new BlockData(placedBlock);
        blocks.remove(bd.convertToKey());
        activeUsers.get(name).getPlayerBlocks().remove(bd.convertToKey());
        QueryProcessor.addQuery(new QueryData("SELECT COUNT(*) AS Count FROM " + ConfigHandler.getDefaultBukkitConfig().getString("database.prefix", "blueprint") + "_players WHERE playerID = '" + name + "';", new QueryCallback() {
            public void result(List<ResultData> result) {
                if (Integer.parseInt(result.get(0).getKey("Count")) == 0) {
                    setOriginalPlayerGameMode(name, GameMode.SURVIVAL);
                }
                QueryProcessor.addQuery(new QueryData("DELETE FROM " + ConfigHandler.getDefaultBukkitConfig().getString("database.prefix", "blueprint") + "_blocks WHERE playerID = '" + name
                        + "' AND blockID = '" + bd.getType()
                        + "' AND blockX = " + bd.getX()
                        + " AND blockY = " + bd.getY()
                        + " AND blockZ = " + bd.getZ()
                        + " AND world = '" + world + "';", new QueryCallback() {
                            public void result(List<ResultData> result) {
                            }
                        }));
            }
        }));
    }

    /**
     *
     * @param name
     * @param placedBlock
     * @return
     */
    public static boolean checkPlayerBlock(final String name, final Block placedBlock) {
        BlockDataCache get = blocks.get(new BlockData(placedBlock).convertToKey());
        if (get != null) {
            return get.getPlayerID().equalsIgnoreCase(name) && get.equalToBlock(placedBlock);
        } else {
            return false;
        }
    }

    /**
     *
     * @param name
     * @param locX
     * @param locY
     * @param locZ
     * @param worldID
     * @return
     */
    public static boolean checkPlayerBlockNoData(final String name, final int locX, final int locY, final int locZ, final String worldID) {
        BlockDataCache get = blocks.get(locX + "" + locY + "" + locZ + worldID);
        if (get != null) {
            return get.getPlayerID().equalsIgnoreCase(name);
        } else {
            return false;
        }
    }

    /**
     *
     * @param name
     * @param placedBlock
     * @return
     */
    public static boolean checkPlayerBlockNoData(final String name, final Block placedBlock) {
        BlockDataCache get = blocks.get(new BlockData(placedBlock).convertToKey());
        if (get != null) {
            return get.getPlayerID().equalsIgnoreCase(name);
        } else {
            return false;
        }
    }

    /**
     *
     * @param name
     * @return
     */
    public static GameMode getOriginalPlayerGameMode(final String name) {
        return GameMode.getByValue(activeUsers.get(name).getGameMode());
    }

    /**
     *
     * @param name
     * @param originalGameMode
     */
    public static void setOriginalPlayerGameMode(final String name, final GameMode originalGameMode) {
        final int gameMode = originalGameMode.getValue();
        if (activeUsers.get(name) == null) {
            activeUsers.put(name, new PlayerData(name));
        }
        activeUsers.get(name).setGameMode(gameMode);
        QueryProcessor.addQuery(new QueryData("SELECT COUNT(*) AS Count FROM " + ConfigHandler.getDefaultBukkitConfig().getString("database.prefix", "blueprint") + "_players WHERE playerID = '" + name + "';", new QueryCallback() {

            public void result(List<ResultData> result) {
                if (Integer.parseInt(result.get(0).getKey("Count")) == 0) {
                    QueryProcessor.addQuery(new QueryData("INSERT INTO " + ConfigHandler.getDefaultBukkitConfig().getString("database.prefix", "blueprint") + "_players (playerID, gameMode) VALUES ('" + name + "', " + gameMode + ");", new QueryCallback() {
                        public void result(List<ResultData> result) {
                        }
                    }) {
                        @Override
                        public boolean runError(Exception ex) {
                            return !(ex instanceof SQLException);
                        }
                    });
                } else {
                    QueryProcessor.addQuery(new QueryData("UPDATE " + ConfigHandler.getDefaultBukkitConfig().getString("database.prefix", "blueprint") + "_players SET gameMode = " + gameMode + " WHERE playerID = '" + name + "';", new QueryCallback() {
                        public void result(List<ResultData> result) {
                        }
                    }));
                }
            }
        }));
    }

    /**
     *
     * @param name
     * @param place
     */
    public static void setPlayerLocation(final String name, final Location place) {
        setPlayerLocation(name, place.getX(), place.getY(), place.getZ());
    }

    public static void setPlayer(final String name, final double locX, final double locY, final double locZ, final String inv, final String arm, final int active, GameMode gamemode, final String world) {
        final int gameMode = gamemode.getValue();
        if (activeUsers.get(name) == null) {
            activeUsers.put(name, new PlayerData(name, locX, locY, locZ, gameMode, inv, arm, active == 1, world));
        } else {
            PlayerData playerData = activeUsers.get(name);
            playerData.setLocX(locX);
            playerData.setLocY(locY);
            playerData.setLocZ(locZ);
            playerData.setGameMode(gameMode);
            playerData.setInventory(inv);
            playerData.setArmour(arm);
            playerData.setActive(active == 1);
            playerData.setWorld(world);
            activeUsers.put(name, playerData);
        }
        QueryProcessor.addQuery(new QueryData("SELECT COUNT(*) AS Count FROM " + ConfigHandler.getDefaultBukkitConfig().getString("database.prefix", "blueprint") + "_players WHERE playerID = '" + name + "';", new QueryCallback() {
            public void result(List<ResultData> result) {
                if (Integer.parseInt(result.get(0).getKey("Count")) == 0) {
                    QueryProcessor.addQuery(new QueryData(MessageFormat.format("INSERT INTO {9}_players (playerID, locX, locY, locZ, gameMode, inventory, armour, active, world) VALUES (''{0}'', {1}, {2}, {3}, {4}, ''{5}'', ''{6}'', {7}, ''{8}'');", name, String.valueOf(locX).replaceAll("[^0-9\\.]", ""), String.valueOf(locY).replaceAll("[^0-9\\.]", ""), String.valueOf(locZ).replaceAll("[^0-9\\.]", ""), gameMode, inv, arm, active, world, ConfigHandler.getDefaultBukkitConfig().getString("database.prefix", "blueprint")), new QueryCallback() {
                        public void result(List<ResultData> result) {
                        }
                    }) {
                        @Override
                        public boolean runError(Exception ex) {
                            ex.printStackTrace();
                            return !(ex instanceof SQLException);
                        }
                    });
                } else {
                    QueryProcessor.addQuery(new QueryData(MessageFormat.format("UPDATE {8}_players SET locX =  {0}, locY =  {1}, locZ = {2}, gameMode = {3},  inventory = ''{4}'', armour = ''{5}'', active = {6} WHERE playerID = ''{7}'';", String.valueOf(locX).replaceAll("[^0-9\\.]", ""), String.valueOf(locY).replaceAll("[^0-9\\.]", ""), String.valueOf(locZ).replaceAll("[^0-9\\.]", ""), gameMode, inv, arm, active, name, ConfigHandler.getDefaultBukkitConfig().getString("database.prefix", "blueprint")), new QueryCallback() {
                        public void result(List<ResultData> result) {
                        }
                    }));
                }
            }
        }));
    }

    /**
     *
     * @param name
     * @param locX
     * @param locY
     * @param locZ
     */
    public static void setPlayerLocation(final String name, final double locX, final double locY, final double locZ) {
        activeUsers.get(name).setLocX(locX);
        activeUsers.get(name).setLocY(locY);
        activeUsers.get(name).setLocZ(locZ);

        QueryProcessor.addQuery(new QueryData("SELECT COUNT(*) AS Count FROM " + ConfigHandler.getDefaultBukkitConfig().getString("database.prefix", "blueprint") + "_players WHERE playerID = '" + name + "';", new QueryCallback() {

            public void result(List<ResultData> result) {
                if (Integer.parseInt(result.get(0).getKey("Count")) == 0) {
                    QueryProcessor.addQuery(new QueryData(MessageFormat.format("INSERT INTO {0}_players (playerID, locX, locY, locZ, gameMode) VALUES ('{1}', {2}, {3}, {4}, 0);", ConfigHandler.getDefaultBukkitConfig().getString("database.prefix", "blueprint"), name, String.valueOf(locX).replaceAll("[^0-9\\.]", ""), String.valueOf(locY).replaceAll("[^0-9\\.]", ""), String.valueOf(locZ).replaceAll("[^0-9\\.]", "")), new QueryCallback() {
                        public void result(List<ResultData> result) {
                            QueryProcessor.addQuery(new QueryData(MessageFormat.format("UPDATE {0}_players SET locX =  {1}, locY =  {2}, locZ = {3} WHERE playerID = '{4}';", ConfigHandler.getDefaultBukkitConfig().getString("database.prefix", "blueprint"), String.valueOf(locX).replaceAll("[^0-9\\.]", ""), String.valueOf(locY).replaceAll("[^0-9\\.]", ""), String.valueOf(locZ).replaceAll("[^0-9\\.]", ""), name), new QueryCallback() {
                                public void result(List<ResultData> result) {
                                }
                            }));
                        }
                    }) {
                        @Override
                        public boolean runError(Exception ex) {
                            return !(ex instanceof SQLException);
                        }
                    });
                } else {
                    QueryProcessor.addQuery(new QueryData(MessageFormat.format("UPDATE {0}_players SET locX =  {1}, locY =  {2}, locZ = {3} WHERE playerID = '{4}';", ConfigHandler.getDefaultBukkitConfig().getString("database.prefix", "blueprint"),String.valueOf(locX).replaceAll("[^0-9\\.]", ""), String.valueOf(locY).replaceAll("[^0-9\\.]", ""), String.valueOf(locZ).replaceAll("[^0-9\\.]", ""), name), new QueryCallback() {
                        public void result(List<ResultData> result) {
                        }
                    }));
                }
            }
        }));
    }

    /**
     *
     * @param name
     * @return
     */
    public static Location getPlayerLocation(final String name) {
        return activeUsers.get(name).getLocation();
    }

    /**
     *
     * @param name
     * @param invData
     * @param armData
     */
    public static void activatePlayer(final String name, final String invData, final String armData) {
        activeUsers.get(name).setActive(true);
        QueryProcessor.addQuery(new QueryData("SELECT COUNT(*) AS Count FROM " + ConfigHandler.getDefaultBukkitConfig().getString("database.prefix", "blueprint") + "_players WHERE playerID = '" + name + "';", new QueryCallback() {
            public void result(List<ResultData> result) {
                if (Integer.parseInt(result.get(0).getKey("Count")) == 0) {
                    setOriginalPlayerGameMode(name, GameMode.SURVIVAL);
                }
                setPlayerInventory(name, invData, armData);
                QueryProcessor.addQuery(new QueryData("UPDATE " + ConfigHandler.getDefaultBukkitConfig().getString("database.prefix", "blueprint") + "_players SET active = 1 WHERE playerID = '" + name + "';", new QueryCallback() {
                    public void result(List<ResultData> result) {
                    }
                }));
            }
        }));
    }

    /**
     *
     * @param name
     * @return
     */
    public static PlayerInventory deactivatePlayer(final String name) {
        activeUsers.get(name).setActive(false);
        QueryProcessor.addQuery(new QueryData("SELECT COUNT(*) AS Count FROM " + ConfigHandler.getDefaultBukkitConfig().getString("database.prefix", "blueprint") + "_players WHERE playerID = '" + name + "';", new QueryCallback() {
            public void result(List<ResultData> result) {
                if (Integer.parseInt(result.get(0).getKey("Count")) == 0) {
                    setOriginalPlayerGameMode(name, GameMode.SURVIVAL);
                }
                QueryProcessor.addQuery(new QueryData("UPDATE " + ConfigHandler.getDefaultBukkitConfig().getString("database.prefix", "blueprint") + "_players SET active = 0 WHERE playerID = '" + name + "';", new QueryCallback() {
                    public void result(List<ResultData> result) {
                    }
                }));
            }
        }));
        return getAndDeserializeFullPlayerInventory(name);
    }

    /**
     *
     * @param name
     * @return
     */
    public static boolean isPlayerActive(final String name) {
        return activeUsers.get(name) == null ? false : activeUsers.get(name).isActive();
    }

    /**
     *
     * @param playerID
     * @param worldID
     * @return
     */
    public static List<BlockData> getBlueprint(final String playerID, final String worldID) {
        List<BlockData> blockList = new CopyOnWriteArrayList();
        if (activeUsers.get(playerID) != null) {
            for (BlockData data : activeUsers.get(playerID).getPlayerBlocks().values()) {
                if (data.getBlockWorld().getName().equalsIgnoreCase(worldID)) {
                    blockList.add(data);
                }
            }
        }
        return blockList;
    }

    /**
     *
     * @param playerId
     * @param inv
     * @param arm
     */
    public static void setPlayerInventory(final String playerId, final String inv, final String arm) {
        activeUsers.get(playerId).setArmour(arm);
        activeUsers.get(playerId).setInventory(inv);
        QueryProcessor.addQuery(new QueryData("UPDATE " + ConfigHandler.getDefaultBukkitConfig().getString("database.prefix", "blueprint") + "_players SET inventory = '" + inv + "', armour = '" + arm + "'  WHERE playerID = '" + playerId + "';", new QueryCallback() {
            public void result(List<ResultData> result) {
            }
        }));
    }

    /**
     *
     * @param playerId
     * @return
     */
    public static String getPlayerInventory(String playerId) {
        return activeUsers.get(playerId).getInventory();
    }

    /**
     *
     * @param playerId
     * @return
     */
    public static String getPlayerArmour(String playerId) {
        return activeUsers.get(playerId).getArmour();
    }

    /**
     *
     * @param playerId
     * @return
     */
    public static PlayerInventory getAndDeserializeFullPlayerInventory(final String playerId) {
        String inv = getPlayerInventory(playerId).replaceAll("''", "'");
        String arm = getPlayerArmour(playerId).replaceAll("''", "'");
        Yaml tempSer = new Yaml();
        List<Map<String, Object>> invPre = (List<Map<String, Object>>) tempSer.load(inv);
        List<Map<String, Object>> armPre = (List<Map<String, Object>>) tempSer.load(arm);
        List<ConfigurationSerializable> invMid = ItemSerial.deserializeItemList(invPre);
        List<ConfigurationSerializable> armMid = ItemSerial.deserializeItemList(armPre);
        ItemStack[] invPost = invMid.toArray(new ItemStack[invMid.size()]);
        ItemStack[] armPost = armMid.toArray(new ItemStack[armMid.size()]);
        return new PlayerInventory(invPost, armPost);
    }

    /**
     *
     * @param name
     * @param clickedBlock
     */
    public static void updatePlayerBlock(final String name, final BlockData clickedBlock) {
        final BlockDataCache bdc = blocks.get(clickedBlock.convertToKey());
        bdc.setData(clickedBlock.getData());
        bdc.setType(clickedBlock.getType());
        QueryProcessor.addQuery(new QueryData("SELECT COUNT(*) AS Count FROM " + ConfigHandler.getDefaultBukkitConfig().getString("database.prefix", "blueprint") + "_players WHERE playerID = '" + name + "';", new QueryCallback() {
            public void result(List<ResultData> result) {
                if (Integer.parseInt(result.get(0).getKey("Count")) == 0) {
                    setOriginalPlayerGameMode(name, GameMode.SURVIVAL);
                }
                QueryProcessor.addQuery(new QueryData("UPDATE " + ConfigHandler.getDefaultBukkitConfig().getString("database.prefix", "blueprint") + "_blocks SET blockMeta = " + (int) bdc.getData() + ", blockID = '" + bdc.getType() + "' WHERE "
                        + " playerID = '" + name
                        + "' AND blockX = " + bdc.getX()
                        + " AND blockY = " + bdc.getY()
                        + " AND blockZ = " + bdc.getZ()
                        + " AND world = '" + bdc.getBlockWorld().getName() + "';", new QueryCallback() {
                            public void result(List<ResultData> result) {
                            }
                        }));
            }
        }));
    }

    /**
     *
     * @param name
     * @param clickedBlock
     */
    public static void updatePlayerBlock(final String name, final Block clickedBlock) {
        final BlockDataCache bdc = blocks.get(new BlockData(clickedBlock).convertToKey());
        bdc.setData(clickedBlock.getData());
        bdc.setType(clickedBlock.getTypeId());
        QueryProcessor.addQuery(new QueryData("SELECT COUNT(*) AS Count FROM " + ConfigHandler.getDefaultBukkitConfig().getString("database.prefix", "blueprint") + "_players WHERE playerID = '" + name + "';", new QueryCallback() {
            public void result(List<ResultData> result) {
                if (Integer.parseInt(result.get(0).getKey("Count")) == 0) {
                    setOriginalPlayerGameMode(name, GameMode.SURVIVAL);
                }
                QueryProcessor.addQuery(new QueryData("UPDATE " + ConfigHandler.getDefaultBukkitConfig().getString("database.prefix", "blueprint") + "_blocks SET blockMeta = " + (int) bdc.getData() + ", blockID = '" + bdc.getType() + "' WHERE"
                        + " playerID = '" + name
                        + "' AND blockX = " + bdc.getX()
                        + " AND blockY = " + bdc.getY()
                        + " AND blockZ = " + bdc.getZ()
                        + " AND world = '" + bdc.getBlockWorld().getName() + "';", new QueryCallback() {
                            public void result(List<ResultData> result) {
                            }
                        }));
            }
        }));
    }

    /**
     *
     * @param name
     * @param item
     * @param clickedBlock
     */
    public static void updatePlayerBlockAndItem(final String name, final ItemStack item, final BlockData clickedBlock) {
        final BlockDataCache bdc = blocks.get(clickedBlock.convertToKey());
        bdc.setData(clickedBlock.getData());
        bdc.setType(clickedBlock.getType());
        QueryProcessor.addQuery(new QueryData("SELECT COUNT(*) AS Count FROM " + ConfigHandler.getDefaultBukkitConfig().getString("database.prefix", "blueprint") + "_players WHERE playerID = '" + name + "';", new QueryCallback() {
            public void result(List<ResultData> result) {
                if (Integer.parseInt(result.get(0).getKey("Count")) == 0) {
                    setOriginalPlayerGameMode(name, GameMode.SURVIVAL);
                }
                QueryProcessor.addQuery(new QueryData("UPDATE " + ConfigHandler.getDefaultBukkitConfig().getString("database.prefix", "blueprint") + "_blocks SET blockMeta = " + (int) bdc.getData() + ", blockID = '" + bdc.getType() + "' WHERE "
                        + " playerID = '" + name
                        + "' AND itemID = " + item.getTypeId()
                        + " AND itemMeta = " + item.getData().getData()
                        + " AND blockX = " + bdc.getX()
                        + " AND blockY = " + bdc.getY()
                        + " AND blockZ = " + bdc.getZ()
                        + " AND world = '" + bdc.getBlockWorld().getName() + "';", new QueryCallback() {
                            public void result(List<ResultData> result) {
                            }
                        }));
            }
        }));
    }

    /**
     *
     * @param name
     * @param placedBlcok
     */
    public static void addPlayerChest(final String name, final Block placedBlcok) {
        final BlockDataChest bdc = new BlockDataChest(placedBlcok, name);
        if (!isPlayerChest(bdc)) {
            chests.put(bdc.convertToKey(), bdc);
            if (activeUsers.get(name) == null) {
                setOriginalPlayerGameMode(name, GameMode.SURVIVAL);
            }
            activeUsers.get(name).getPlayerChests().put(bdc.convertToKey(), bdc);
            QueryProcessor.addQuery(new QueryData("SELECT COUNT(*) AS Count FROM " + ConfigHandler.getDefaultBukkitConfig().getString("database.prefix", "blueprint") + "_players WHERE playerID = '" + name + "';", new QueryCallback() {
                public void result(List<ResultData> result) {
                    if (Integer.parseInt(result.get(0).getKey("Count")) == 0) {
                        setOriginalPlayerGameMode(name, GameMode.SURVIVAL);
                    }
                    QueryProcessor.addQuery(new QueryData("INSERT INTO " + ConfigHandler.getDefaultBukkitConfig().getString("database.prefix", "blueprint") + "_chests (playerID, blockX, blockY, blockZ, world) VALUES ('" + name + "', " + bdc.getX() + ", " + bdc.getY() + ", " + bdc.getZ() + ", '" + bdc.getBlockWorld().getName() + "');", new QueryCallback() {
                        public void result(List<ResultData> result) {
                        }
                    }));
                }
            }));
        }
    }

    /**
     *
     * @param name
     * @param placedBlock
     */
    public static void removePlayerChest(final String name, final Block placedBlock) {
        final BlockDataChest bdc = new BlockDataChest(placedBlock, name);

        if (isPlayerChest(bdc)) {
            if (chests.get(bdc.convertToKey()) != null && chests.get(bdc.convertToKey()).getOwner().equalsIgnoreCase(name)) {
                chests.remove(bdc.convertToKey());
                activeUsers.get(name).getPlayerChests().remove(bdc.convertToKey());
            } else {
                return;
            }
            QueryProcessor.addQuery(new QueryData("SELECT COUNT(*) AS Count FROM " + ConfigHandler.getDefaultBukkitConfig().getString("database.prefix", "blueprint") + "_players WHERE playerID = '" + name + "';", new QueryCallback() {
                public void result(List<ResultData> result) {
                    if (Integer.parseInt(result.get(0).getKey("Count")) == 0) {
                        setOriginalPlayerGameMode(name, GameMode.SURVIVAL);
                    }
                    QueryProcessor.addQuery(new QueryData("DELETE FROM " + ConfigHandler.getDefaultBukkitConfig().getString("database.prefix", "blueprint") + "_chests WHERE playerID = '" + name + "' AND blockX = " + bdc.getX() + " AND blockY = " + bdc.getY() + " AND blockZ  = " + bdc.getZ() + " AND world = '" + bdc.getBlockWorld().getName() + "';", new QueryCallback() {
                        public void result(List<ResultData> result) {
                        }
                    }));

                }
            }));
        }
    }

    /**
     *
     * @param placedBlock
     * @return
     */
    public static boolean isPlayerChest(Block placedBlock) {
        return chests.get(new BlockData(placedBlock).convertToKey()) != null;
    }

    /**
     *
     * @param placedBlock
     * @return
     */
    public static boolean isPlayerChest(BlockData placedBlock) {
        return chests.get(placedBlock.convertToKey()) != null;
    }

    /**
     *
     * @param name
     * @return
     */
    public static List<BlockDataChest> getPlayerChestLocations(final String name) {
        return new ArrayList(activeUsers.get(name).getPlayerChests().values());
    }

    /**
     *
     * @return
     */
    public static List<String> getPlayerIds() {
        return new ArrayList(activeUsers.keySet());
    }

    /**
     *
     * @param playerID
     * @return
     */
    public static List<BlockData> getBlueprintAllWorlds(final String playerID) {
        return new ArrayList(activeUsers.get(playerID).getPlayerBlocks().values());
    }

    /**
     *
     * @param playerID
     * @param worldID
     * @return
     */
    public static List<Integer> getBlueprintBlockTypes(final String playerID, final String worldID) {
        List<Integer> blockList = new CopyOnWriteArrayList();
        for (BlockData data : activeUsers.get(playerID).getPlayerBlocks().values()) {
            if (data.getBlockWorld().getName().equalsIgnoreCase(worldID) && !blockList.contains(data.getType())) {
                blockList.add(data.getType());
            }
        }
        return blockList;
    }

    /**
     *
     * @param playerID
     * @param mat
     * @param worldID
     * @return
     */
    public static int getBlueprintBlockOfTypInWorldNeeded(String playerID, int mat, String worldID) {
        int count = 0;
        for (BlockData data : activeUsers.get(playerID).getPlayerBlocks().values()) {
            if (data.getBlockWorld().getName().equalsIgnoreCase(worldID) && data.getType() == mat) {
                count++;
            }
        }
        return count;
    }

    /**
     *
     * @param playerID
     * @param mat
     * @param worldID
     * @return
     */
    public static List<BlockData> getBlueprintBuildBlockOfTypInWorld(String playerID, int mat, String worldID) {
        List<BlockData> blockList = new CopyOnWriteArrayList();
        for (BlockData data : activeUsers.get(playerID).getPlayerBlocks().values()) {
            if (data.getBlockWorld().getName().equalsIgnoreCase(worldID) && data.getType() == mat) {
                blockList.add(data);
            }
        }
        return blockList;
    }

    /**
     *
     * @param blockType
     * @param placedBlock
     * @return
     */
    public static boolean isBlueprintBlock(final Material blockType, final Location placedBlock) {
        return isBlueprintBlock(blockType.getId(), placedBlock);
    }

    /**
     *
     * @param blockTypeID
     * @param placedBlock
     * @return
     */
    public static boolean isBlueprintBlock(final int blockTypeID, final Location placedBlock) {
        return isBlueprintBlock(blockTypeID, (int) (placedBlock.getX() + 0.5D), (int) (placedBlock.getY() + 0.5D), (int) (placedBlock.getZ() + 0.5D), placedBlock.getWorld().getName());
    }

    /**
     *
     * @param placedBlock
     * @return
     */
    public static boolean isBlueprintBlock(final Block placedBlock) {
        return isBlueprintBlock(placedBlock.getType().getId(), placedBlock.getX(), placedBlock.getY(), placedBlock.getZ(), placedBlock.getWorld().getName());
    }

    /**
     *
     * @param blockTypeID
     * @param locX
     * @param locY
     * @param locZ
     * @param worldID
     * @return
     */
    public static boolean isBlueprintBlock(int blockTypeID, final int locX, final int locY, final int locZ, final String worldID) {
        BlockDataCache get = blocks.get(locX + "" + locY + "" + locZ + worldID);
        if (get != null) {
            return get.getType() == blockTypeID;
        } else {
            return false;
        }
    }

    /**
     *
     * @param clickedBlock
     */
    public static void updateBlock(final Block clickedBlock) {
        BlockDataCache bdc = blocks.get(new BlockData(clickedBlock).convertToKey());
        bdc.setData(clickedBlock.getData());
        bdc.setType(clickedBlock.getTypeId());
        blocks.put(bdc.convertToKey(), bdc);
        bdc = activeUsers.get(blocks.get(bdc.convertToKey()).getPlayerID()).getPlayerBlocks().get(bdc.convertToKey());
        bdc.setData(clickedBlock.getData());
        bdc.setType(clickedBlock.getTypeId());
        activeUsers.get(blocks.get(bdc.convertToKey()).getPlayerID()).getPlayerBlocks().put(bdc.convertToKey(), bdc);
        QueryProcessor.addQuery(new QueryData("UPDATE " + ConfigHandler.getDefaultBukkitConfig().getString("database.prefix", "blueprint") + "_blocks SET blockMeta = " + bdc.getData() + ",  blockID = " + bdc.getType() + " WHERE "
                + "  blockX = " + bdc.getX()
                + " AND blockY = " + bdc.getY()
                + " AND blockZ = " + bdc.getZ()
                + " AND world = '" + bdc.getBlockWorld().getName() + "';", new QueryCallback() {
                    public void result(List<ResultData> result) {
                    }
                }));
    }

    /**
     *
     * @param location
     * @return
     */
    public static boolean isBlueprintBlockAtLocation(final Location location) {
        return isBlueprintBlockAtLocation(location.getBlockX(), location.getBlockY(), location.getBlockZ(), location.getWorld().getName());
    }

    /**
     *
     * @param locX
     * @param locY
     * @param locZ
     * @param worldID
     * @return
     */
    public static boolean isBlueprintBlockAtLocation(final int locX, final int locY, final int locZ, final String worldID) {
        return blocks.get(locX + "" + locY + "" + locZ + worldID) != null;
    }

    /**
     *
     * @param playerID
     * @return
     */
    public static List<ItemStack> getBlueprintItemTypes(final String playerID) {
        List<ItemStack> blockList = new CopyOnWriteArrayList();
        if (activeUsers.get(playerID) != null) {
            for (BlockDataCache data : activeUsers.get(playerID).getPlayerBlocks().values()) {
                blockList.add(data.getItemStack());
            }
        }
        return blockList;
    }

    /**
     *
     * @param playerID
     * @param worldID
     * @return
     */
    public static List<ItemStack> getBlueprintItemTypes(final String playerID, final String worldID) {
        List<ItemStack> blockList = new CopyOnWriteArrayList();
        for (BlockDataCache data : activeUsers.get(playerID).getPlayerBlocks().values()) {
            if (data.getBlockWorld().getName().equals(worldID)) {
                blockList.add(data.getItemStack());
            }
        }
        return blockList;
    }

    /**
     *
     * @param playerID
     * @param matID
     * @param matData
     * @param worldID
     * @return
     */
    public static int getBlueprintBlockOfTypInWorldNeededFromItem(final String playerID, final int matID, final int matData, final String worldID) {
        ItemStack checkStack = new ItemStack(matID);
        checkStack.getData().setData((byte) matData);
        return getBlueprintBlockOfTypInWorldNeededFromItem(playerID, checkStack, worldID);
    }

    /**
     *
     * @param playerID
     * @param matID
     * @param matData
     * @param worldID
     * @return
     */
    public static List<BlockData> getBlueprintBuildBlockOfTypInWorldFromItem(final String playerID, final int matID, final int matData, final String worldID) {
        ItemStack checkStack = new ItemStack(matID);
        checkStack.getData().setData((byte) matData);
        return getBlueprintBuildBlockOfTypInWorldFromItem(playerID, checkStack, worldID);
    }

    /**
     *
     * @param loc
     * @return
     */
    public static String getBlockOwnerAtLocation(final Location loc) {
        return blocks.get(loc.getBlockX() + "" + loc.getBlockY() + "" + loc.getBlockZ() + loc.getWorld().getName()).getPlayerID();
    }

    /**
     *
     * @param playerID
     * @param mat
     * @param worldID
     * @return
     */
    public static int getBlueprintBlockOfTypInWorldNeededFromItem(final String playerID, final ItemStack mat, final String worldID) {
        int count = 0;
        for (BlockDataCache data : activeUsers.get(playerID).getPlayerBlocks().values()) {
            if (data.getItemID() == mat.getTypeId() && data.getItemMeta() == mat.getData().getData()) {
                count++;
            }
        }
        return count;
    }

    /**
     *
     * @param playerID
     * @param mat
     * @param worldID
     * @return
     */
    public static List<BlockData> getBlueprintBuildBlockOfTypInWorldFromItem(final String playerID, final ItemStack mat, final String worldID) {
        List<BlockData> blockList = new CopyOnWriteArrayList();
        for (BlockDataCache data : activeUsers.get(playerID).getPlayerBlocks().values()) {
            if (data.getItemID() == mat.getTypeId() && data.getItemMeta() == mat.getData().getData()) {
                blockList.add(data);
            }
        }
        return blockList;
    }

    /**
     *
     * @param placedBlock
     * @param world
     */
    public static void removeBlueprintBlock(final BlockData placedBlock, final String world) {
        blocks.remove(placedBlock.getX() + "" + placedBlock.getY() + "" + placedBlock.getZ() + world);
        QueryProcessor.addQuery(new QueryData("DELETE FROM " + ConfigHandler.getDefaultBukkitConfig().getString("database.prefix", "blueprint") + "_blocks WHERE blockID = '" + placedBlock.getType()
                + "' AND blockX = " + placedBlock.getX()
                + " AND blockY = " + placedBlock.getY()
                + " AND blockZ = " + placedBlock.getZ()
                + " AND blockMeta = " + (int) placedBlock.getData()
                + " AND world = '" + world + "';", new QueryCallback() {
                    public void result(List<ResultData> result) {
                    }
                }));
    }

    /**
     *
     */
    public static void setupCache() {
        QueryProcessor.addQuery(new QueryData("SELECT playerID, locX, locY, locZ, gameMode, inventory, armour, active, world  FROM " + ConfigHandler.getDefaultBukkitConfig().getString("database.prefix", "blueprint") + "_players;", new QueryCallback() {
            public void result(List<ResultData> result) {
                for (ResultData data : result) {
                    activeUsers.put(data.getKey("playerID"), new PlayerData(data.getKey("playerID"), Double.parseDouble(data.getKey("locX")), Double.parseDouble(data.getKey("locY")), Double.parseDouble(data.getKey("locZ")), Integer.parseInt(data.getKey("gameMode")), data.getKey("inventory"), data.getKey("armour"), data.getKey("active").equals("1"), data.getKey("world")));
                }
                QueryProcessor.addQuery(new QueryData("SELECT playerID, itemID, itemMeta, blockID, blockX, blockY,blockZ, blockMeta, world  FROM " + ConfigHandler.getDefaultBukkitConfig().getString("database.prefix", "blueprint") + "_blocks;", new QueryCallback() {
                    public void result(List<ResultData> result) {
                        for (ResultData data : result) {
                            activeUsers.get(data.getKey("playerID")).getPlayerBlocks().put(data.getKey("blockX") + data.getKey("blockY") + data.getKey("blockZ") + data.getKey("world"),
                                    new BlockDataCache(Integer.parseInt(data.getKey("blockID")),
                                            Integer.parseInt(data.getKey("blockX")),
                                            Integer.parseInt(data.getKey("blockY")),
                                            Integer.parseInt(data.getKey("blockZ")),
                                            Byte.parseByte(data.getKey("blockMeta")),
                                            Bukkit.getWorld(data.getKey("world")),
                                            data.getKey("playerID"),
                                            Integer.parseInt(data.getKey("itemID")),
                                            Integer.parseInt(data.getKey("itemMeta"))
                                    ));
                            blocks.put(data.getKey("blockX") + data.getKey("blockY") + data.getKey("blockZ") + data.getKey("world"),
                                    new BlockDataCache(Integer.parseInt(data.getKey("blockID")),
                                            Integer.parseInt(data.getKey("blockX")),
                                            Integer.parseInt(data.getKey("blockY")),
                                            Integer.parseInt(data.getKey("blockZ")),
                                            Byte.parseByte(data.getKey("blockMeta")),
                                            Bukkit.getWorld(data.getKey("world")),
                                            data.getKey("playerID"),
                                            Integer.parseInt(data.getKey("itemID")),
                                            Integer.parseInt(data.getKey("itemMeta"))
                                    ));
                        }
                        QueryProcessor.addQuery(new QueryData("SELECT playerID, world, blockX, blockY, blockZ  FROM " + ConfigHandler.getDefaultBukkitConfig().getString("database.prefix", "blueprint") + "_chests;", new QueryCallback() {
                            public void result(List<ResultData> result) {
                                for (ResultData data : result) {
                                    BlockDataChest bdc = new BlockDataChest(54, Integer.parseInt(data.getKey("blockX")), Integer.parseInt(data.getKey("blockY")), Integer.parseInt(data.getKey("blockZ")), (byte) 0, Bukkit.getWorld(data.getKey("world")), data.getKey("playerID"));
                                    activeUsers.get(data.getKey("playerID")).getPlayerChests().put(bdc.convertToKey(), bdc);
                                    chests.put(bdc.convertToKey(), bdc);
                                }
                            }
                        }));
                    }
                }));
            }
        }));
    }

    /**
     *
     */
    public static void setupDB() {
        switch (databaseType) {
            case 0:
                QueryProcessor.addQuery(new QueryData("CREATE TABLE IF NOT EXISTS " + ConfigHandler.getDefaultBukkitConfig().getString("database.prefix", "blueprint") + "_players (\n"
                        + " id INTEGER PRIMARY KEY AUTOINCREMENT,\n"
                        + " playerID TEXT NOT NULL ,\n"
                        + " locX REAL NOT NULL DEFAULT 0,\n"
                        + " locY REAL NOT NULL DEFAULT 0,\n"
                        + " locZ REAL NOT NULL DEFAULT 0,\n"
                        + " gameMode INTEGER NOT NULL,\n"
                        + " inventory MEDIUMTEXT NOT NULL DEFAULT '',\n"
                        + " armour MEDIUMTEXT NOT NULL DEFAULT '',\n"
                        + " active INTEGER NOT NULL DEFAULT 0,\n"
                        + " world MEDIUMTEXT NOT NULL,\n"
                        + " CONSTRAINT Play UNIQUE(playerID));", new QueryCallback() {
                            public void result(List<ResultData> result) {
                            }
                        }));

                QueryProcessor.addQuery(new QueryData("SELECT world FROM " + ConfigHandler.getDefaultBukkitConfig().getString("database.prefix", "blueprint") + "_players;", new QueryCallback() {
                    public void result(List<ResultData> result) {
                    }
                }) {
                    @Override
                    public boolean runError(Exception er) {
                        QueryProcessor.addQuery(new QueryData("ALTER TABLE " + ConfigHandler.getDefaultBukkitConfig().getString("database.prefix", "blueprint") + "_players ADD COLUMN world MEDIUMTEXT NOT NULL AFTER active;", new QueryCallback() {
                            public void result(List<ResultData> result) {
                            }
                        }) {
                            @Override
                            public boolean runError(Exception er) {
                                Blueprint.info("Couldn't alter table, if the plugin misbehaves you might have to delete the database (or at least the player table)");
                                return false;
                            }
                        });
                        return false;
                    }
                });

                QueryProcessor.addQuery(new QueryData("CREATE TABLE IF NOT EXISTS " + ConfigHandler.getDefaultBukkitConfig().getString("database.prefix", "blueprint") + "_blocks (\n"
                        + " id INTEGER PRIMARY KEY AUTOINCREMENT,\n"
                        + " playerID TEXT NOT NULL,\n"
                        + " itemID INTEGER NOT NULL,\n"
                        + " itemMeta MEDIUMTEXT NOT NULL,\n"
                        + " blockID TEXT NOT NULL,\n"
                        + " blockX INTEGER NOT NULL,\n"
                        + " blockY INTEGER NOT NULL,\n"
                        + " blockZ INTEGER NOT NULL,\n"
                        + " blockMeta INTEGER NOT NULL,\n"
                        + " world TEXT NOT NULL, "
                        + " CONSTRAINT CON UNIQUE(blockX, blockY, blockZ, world));", new QueryCallback() {

                            public void result(List<ResultData> result) {
                            }
                        }
                ));

                QueryProcessor.addQuery(new QueryData("CREATE TABLE IF NOT EXISTS " + ConfigHandler.getDefaultBukkitConfig().getString("database.prefix", "blueprint") + "_chests (\n"
                        + " id INTEGER PRIMARY KEY AUTOINCREMENT,\n"
                        + " playerID TEXT NOT NULL,\n"
                        + " world TEXT NOT NULL,\n"
                        + " blockX INTEGER NOT NULL,\n"
                        + " blockY INTEGER NOT NULL,\n"
                        + " blockZ INTEGER NOT NULL, "
                        + " CONSTRAINT CON UNIQUE(blockX, blockY, blockZ, world));", new QueryCallback() {
                            public void result(List<ResultData> result) {
                            }
                        }));
                break;

            case 9:
            default:
                QueryProcessor.addQuery(new QueryData("CREATE TABLE IF NOT EXISTS " + ConfigHandler.getDefaultBukkitConfig().getString("database.prefix", "blueprint") + "_players (\n"
                        + " id INT NOT NULL AUTO_INCREMENT,\n"
                        + " playerID VARCHAR(45) NOT NULL,\n"
                        + " locX DECIMAL NOT NULL DEFAULT 0,\n"
                        + " locY DECIMAL NOT NULL DEFAULT 0,\n"
                        + " locZ DECIMAL NOT NULL DEFAULT 0,\n"
                        + " gameMode INT NOT NULL,\n"
                        + " inventory MEDIUMTEXT,\n"
                        + " armour MEDIUMTEXT,\n"
                        + " active INT NOT NULL DEFAULT 0,\n"
                        + " world MEDIUMTEXT NOT NULL,\n"
                        + " PRIMARY KEY (id),\n"
                        + " UNIQUE INDEX id_UNIQUE (id ASC),\n"
                        + " UNIQUE INDEX playerID_UNIQUE (playerID ASC));", new QueryCallback() {
                            public void result(List<ResultData> result) {
                            }
                        }));

                QueryProcessor.addQuery(new QueryData("SELECT world FROM " + ConfigHandler.getDefaultBukkitConfig().getString("database.prefix", "blueprint") + "_players;", new QueryCallback() {
                    public void result(List<ResultData> result) {
                    }
                }) {
                    @Override
                    public boolean runError(Exception er) {
                        QueryProcessor.addQuery(new QueryData("ALTER TABLE " + ConfigHandler.getDefaultBukkitConfig().getString("database.prefix", "blueprint") + "_players ADD COLUMN world MEDIUMTEXT NOT NULL AFTER active;", new QueryCallback() {
                            public void result(List<ResultData> result) {
                            }
                        }) {
                            @Override
                            public boolean runError(Exception er) {
                                Blueprint.info("Couldn't alter table, if the plugin misbehaves you might have to delete the database (or at least the player table)");
                                return false;
                            }
                        });
                        return false;
                    }
                });

                QueryProcessor.addQuery(new QueryData("CREATE TABLE IF NOT EXISTS " + ConfigHandler.getDefaultBukkitConfig().getString("database.prefix", "blueprint") + "_blocks (\n"
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
                        + " CONSTRAINT CON UNIQUE(blockX, blockY, blockZ, world),\n"
                        + " PRIMARY KEY (id));", new QueryCallback() {

                            public void result(List<ResultData> result) {
                            }
                        })
                );

                QueryProcessor.addQuery(new QueryData("CREATE TABLE IF NOT EXISTS " + ConfigHandler.getDefaultBukkitConfig().getString("database.prefix", "blueprint") + "_chests (\n"
                        + " id INT NOT NULL AUTO_INCREMENT,\n"
                        + " playerID VARCHAR(45) NOT NULL,\n"
                        + " world VARCHAR(45) NOT NULL,\n"
                        + " blockX INT NOT NULL,\n"
                        + " blockY INT NOT NULL,\n"
                        + " blockZ INT NOT NULL,\n"
                        + " CONSTRAINT CON UNIQUE(blockX, blockY, blockZ, world),\n"
                        + " PRIMARY KEY (id));", new QueryCallback() {
                            public void result(List<ResultData> result) {
                            }
                        }));
        }
    }

    public static boolean playerExists(final String playerId) {
        return activeUsers.get(playerId) != null;
    }
}
