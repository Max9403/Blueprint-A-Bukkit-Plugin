package com.emberringstudios.blueprint;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import lib.PatPeter.SQLibrary.Database;
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
    private static volatile ConcurrentHashMap<String, PlayerData> activeUsers = new ConcurrentHashMap();
    private static volatile ConcurrentHashMap<String, BlockDataCache> blocks = new ConcurrentHashMap();
    private static volatile ConcurrentHashMap<String, BlockDataChest> chests = new ConcurrentHashMap();

    public static int getDatabaseType() {
        return databaseType;
    }

    public static void setDatabaseType(final int aDatabaseType) {
        databaseType = aDatabaseType;
    }

    public static void addPlayerBlock(final String name, final ItemStack item, final BlockData placedBlock) {
        addPlayerBlock(name, item.getTypeId(), item.getData().getData(), placedBlock);
    }

    public static void addPlayerBlock(final String name, final int itemID, final int itemData, final BlockData placedBlock) {
        blocks.put(placedBlock.convertToKey(), new BlockDataCache(placedBlock, name, itemID, (byte) itemData));
        activeUsers.get(name).getPlayerBlocks().put(placedBlock.convertToKey(), new BlockDataCache(placedBlock, name, itemID, (byte) itemData));
        Bukkit.getScheduler().runTaskAsynchronously(Blueprint.getPlugin(), new Runnable() {
            public void run() {
                try {
                    if (Integer.parseInt(query("SELECT COUNT(*) AS Count FROM players WHERE playerID = '" + name + "';").get(0).getKey("Count")) == 0) {
                        setOriginalPlayerGameMode(name, GameMode.SURVIVAL);
                    }
                    query("INSERT INTO blocks (playerID,  itemID, itemMeta, blockID, blockX, blockY, blockZ, blockMeta, world) VALUES ('" + name + "', " + itemID + ", '" + itemData + "', '" + placedBlock.getType() + "', " + placedBlock.getX() + ", " + placedBlock.getY() + ", " + placedBlock.getZ() + ", " + (int) placedBlock.getData() + ", '" + placedBlock.getBlockWorld().getName() + "');");
                } catch (SQLException ex) {
                    Blueprint.error("Couldn't add block to player", ex);
                }
            }
        });
    }

    public static void removePlayerBlock(final String name, final BlockData placedBlock, final String world) {
        blocks.remove(placedBlock.convertToKey());
        activeUsers.get(name).getPlayerBlocks().remove(placedBlock.convertToKey());
        Bukkit.getScheduler().runTaskAsynchronously(Blueprint.getPlugin(), new Runnable() {
            public void run() {
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
        });
    }

    public static void removePlayerBlock(final String name, final Block placedBlock, final String world) {
        blocks.remove(new BlockData(placedBlock).convertToKey());
        activeUsers.get(name).getPlayerBlocks().remove(new BlockData(placedBlock).convertToKey());
        Bukkit.getScheduler().runTaskAsynchronously(Blueprint.getPlugin(), new Runnable() {
            public void run() {
                try {
                    if (Integer.parseInt(query("SELECT COUNT(*) AS Count FROM players WHERE playerID = '" + name + "';").get(0).getKey("Count")) == 0) {
                        setOriginalPlayerGameMode(name, GameMode.SURVIVAL);
                    }
                    query("DELETE FROM blocks WHERE playerID = '" + name
                            + "' AND blockID = '" + placedBlock.getType().getId()
                            + "' AND blockX = " + placedBlock.getX()
                            + " AND blockY = " + placedBlock.getY()
                            + " AND blockZ = " + placedBlock.getZ()
                            + " AND world = '" + world + "';");
                } catch (SQLException ex) {
                    Blueprint.error("Couldn't activate player", ex);
                }
            }
        });
    }

    public static boolean checkPlayerBlock(final String name, final Block placedBlock) {
        BlockDataCache get = blocks.get(new BlockData(placedBlock).convertToKey());
        if (get != null) {
            return get.getPlayerID().equalsIgnoreCase(name) && get.equalToBlock(placedBlock);
        } else {
            return false;
        }
    }

    public static boolean checkPlayerBlockNoData(final String name, final int locX, final int locY, final int locZ, final String worldID) {
        BlockDataCache get = blocks.get(locX + "" + locY + "" + locZ + worldID);
        if (get != null) {
            return get.getPlayerID().equalsIgnoreCase(name);
        } else {
            return false;
        }
    }

    public static boolean checkPlayerBlockNoData(final String name, final Block placedBlock) {
        BlockDataCache get = blocks.get(new BlockData(placedBlock).convertToKey());
        if (get != null) {
            return get.getPlayerID().equalsIgnoreCase(name);
        } else {
            return false;
        }
    }

    public static GameMode getOriginalPlayerGameMode(final String name) {
        return activeUsers.get(name).getGameMode() == 0 ? GameMode.CREATIVE : GameMode.ADVENTURE;
    }

    public static void setOriginalPlayerGameMode(final String name, final GameMode originalGameMode) {
        final int gameMode = originalGameMode == GameMode.ADVENTURE ? 1 : 0;
        if (activeUsers.get(name) == null) {
            activeUsers.put(name, new PlayerData(name));
        }
        activeUsers.get(name).setGameMode(gameMode);
        Bukkit.getScheduler().runTaskAsynchronously(Blueprint.getPlugin(), new Runnable() {
            public void run() {
                try {
                    if (Integer.parseInt(query("SELECT COUNT(*) AS Count FROM players WHERE playerID = '" + name + "';").get(0).getKey("Count")) == 0) {
                        query("INSERT INTO players (playerID, gameMode) VALUES ('" + name + "', " + gameMode + ");");
                    } else {
                        query("UPDATE players SET gameMode = " + gameMode + " WHERE playerID = '" + name + "';");
                    }
                } catch (SQLException ex) {
                    Blueprint.error("Could not alter table data", ex);
                }
            }
        });
    }

    public static void setPlayerLocation(final String name, final Location place) {
        setPlayerLocation(name, place.getX(), place.getY(), place.getZ());
    }

    public static void setPlayerLocation(final String name, final double locX, final double locY, final double locZ) {
        activeUsers.get(name).setLocX(locX);
        activeUsers.get(name).setLocY(locY);
        activeUsers.get(name).setLocZ(locZ);
        Bukkit.getScheduler().runTaskAsynchronously(Blueprint.getPlugin(), new Runnable() {
            public void run() {
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
        });
    }

    public static BasicLocation getPlayerLocation(final String name) {
        return activeUsers.get(name).getLocation();
    }

    public static void activatePlayer(final String name, final String invData, final String armData) {
        activeUsers.get(name).setActive(true);
        Bukkit.getScheduler().runTaskAsynchronously(Blueprint.getPlugin(), new Runnable() {
            public void run() {
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
        });
    }

    public static PlayerInventory deactivatePlayer(final String name) {
        activeUsers.get(name).setActive(false);
        Bukkit.getScheduler().runTaskAsynchronously(Blueprint.getPlugin(), new Runnable() {
            public void run() {
                try {
                    if (Integer.parseInt(query("SELECT COUNT(*) AS Count FROM players WHERE playerID = '" + name + "';").get(0).getKey("Count")) == 0) {
                        setOriginalPlayerGameMode(name, GameMode.SURVIVAL);
                    }
                    query("UPDATE players SET active = 0 WHERE playerID = '" + name + "';");
                } catch (SQLException ex) {

                    Blueprint.error("Couldn't deactivate player", ex);
                }
            }
        }
        );
        return getAndDeserializeFullPlayerInventory(name);
    }

    public static boolean isPlayerActive(final String name) {
        return activeUsers.get(name) == null ? false : activeUsers.get(name).isActive();
    }

    public static List<BlockData> getBlueprint(final String playerID, final String worldID) {
        List<BlockData> blockList = new CopyOnWriteArrayList();
        for (BlockData data : activeUsers.get(playerID).getPlayerBlocks().values()) {
            if (data.getBlockWorld().getName().equalsIgnoreCase(worldID)) {
                blockList.add(data);
            }
        }
        return blockList;
    }

    public static void setPlayerInventory(final String playerId, final String inv, final String arm) {
        activeUsers.get(playerId).setArmour(arm);
        activeUsers.get(playerId).setInventory(inv);
        Bukkit.getScheduler().runTaskAsynchronously(Blueprint.getPlugin(), new Runnable() {
            public void run() {
                try {
                    query("UPDATE players SET inventory = '" + inv + "', armour = '" + arm + "'  WHERE playerID = '" + playerId + "';");
                } catch (SQLException ex) {
                    Blueprint.error("Couldn't deactivate player", ex);
                }
            }
        });

    }

    public static String getPlayerInventory(String playerId) {
        return activeUsers.get(playerId).getInventory();
    }

    public static String getPlayerArmour(String playerId) {
        return activeUsers.get(playerId).getArmour();
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

    public static void updatePlayerBlock(final String name, final BlockData clickedBlock) {
        final BlockDataCache bdc = blocks.get(clickedBlock.convertToKey());
        bdc.setData(clickedBlock.getData());
        bdc.setType(clickedBlock.getType());
        Bukkit.getScheduler().runTaskAsynchronously(Blueprint.getPlugin(), new Runnable() {
            public void run() {
                try {
                    if (Integer.parseInt(query("SELECT COUNT(*) AS Count FROM players WHERE playerID = '" + name + "';").get(0).getKey("Count")) == 0) {
                        setOriginalPlayerGameMode(name, GameMode.SURVIVAL);
                    }
                    query("UPDATE blocks SET blockMeta = " + (int) bdc.getData() + ", blockID = '" + bdc.getType() + "' WHERE "
                            + " playerID = '" + name
                            + "' AND blockX = " + bdc.getX()
                            + " AND blockY = " + bdc.getY()
                            + " AND blockZ = " + bdc.getZ()
                            + " AND world = '" + bdc.getBlockWorld().getName() + "';");
                } catch (SQLException ex) {
                    Blueprint.error("Couldn't activate player", ex);
                }
            }
        });
    }

    public static void updatePlayerBlock(final String name, final Block clickedBlock) {
        final BlockDataCache bdc = blocks.get(new BlockData(clickedBlock).convertToKey());
        bdc.setData(clickedBlock.getData());
        bdc.setType(clickedBlock.getTypeId());
        Bukkit.getScheduler().runTaskAsynchronously(Blueprint.getPlugin(), new Runnable() {
            public void run() {
                try {
                    if (Integer.parseInt(query("SELECT COUNT(*) AS Count FROM players WHERE playerID = '" + name + "';").get(0).getKey("Count")) == 0) {
                        setOriginalPlayerGameMode(name, GameMode.SURVIVAL);
                    }
                    query("UPDATE blocks SET blockMeta = " + (int) bdc.getData() + ", blockID = '" + bdc.getType() + "' WHERE"
                            + " playerID = '" + name
                            + "' AND blockX = " + bdc.getX()
                            + " AND blockY = " + bdc.getY()
                            + " AND blockZ = " + bdc.getZ()
                            + " AND world = '" + bdc.getBlockWorld().getName() + "';");
                } catch (SQLException ex) {
                    Blueprint.error("Couldn't activate player", ex);
                }
            }
        });
    }

    public static void addPlayerChest(final String name, final Block placedBlcok) {
        final BlockDataChest bdc = new BlockDataChest(placedBlcok, name);
        if (!isPlayerChest(bdc)) {
            chests.put(bdc.convertToKey(), bdc);
            activeUsers.get(name).getPlayerChests().put(bdc.convertToKey(), bdc);
            Bukkit.getScheduler().runTaskAsynchronously(Blueprint.getPlugin(), new Runnable() {
                public void run() {
                    try {
                        if (Integer.parseInt(query("SELECT COUNT(*) AS Count FROM players WHERE playerID = '" + name + "';").get(0).getKey("Count")) == 0) {
                            setOriginalPlayerGameMode(name, GameMode.SURVIVAL);
                        }
                        query("INSERT INTO chests (playerID, blockX, blockY, blockZ, world) VALUES ('" + name + "', " + bdc.getX() + ", " + bdc.getY() + ", " + bdc.getZ() + ", '" + bdc.getBlockWorld().getName() + "');");

                    } catch (SQLException ex) {
                        Blueprint.error("Couldn't activate player", ex);
                    }
                }
            });
        }
    }

    public static void removePlayerChest(final String name, final Block placedBlock) {
        final BlockDataChest bdc = new BlockDataChest(placedBlock, name);

        if (isPlayerChest(bdc)) {
            if (chests.get(bdc.convertToKey()) != null && chests.get(bdc.convertToKey()).getOwner().equalsIgnoreCase(name)) {
                chests.remove(bdc.convertToKey());
                activeUsers.get(name).getPlayerChests().remove(bdc.convertToKey());
            } else {
                return;
            }
            Bukkit.getScheduler().runTaskAsynchronously(Blueprint.getPlugin(), new Runnable() {
                public void run() {
                    try {
                        if (Integer.parseInt(query("SELECT COUNT(*) AS Count FROM players WHERE playerID = '" + name + "';").get(0).getKey("Count")) == 0) {
                            setOriginalPlayerGameMode(name, GameMode.SURVIVAL);
                        }
                        query("DELETE FROM chests WHERE playerID = '" + name + "' AND blockX = " + bdc.getX() + " AND blockY = " + bdc.getY() + " AND blockZ  = " + bdc.getZ() + " AND world = '" + bdc.getBlockWorld().getName() + "';");
                    } catch (SQLException ex) {
                        Blueprint.error("Couldn't activate player", ex);
                    }
                }
            });
        }
    }

    public static boolean isPlayerChest(Block placedBlock) {
        return chests.get(new BlockData(placedBlock).convertToKey()) != null;
    }

    public static boolean isPlayerChest(BlockData placedBlock) {
        return chests.get(placedBlock.convertToKey()) != null;
    }

    public static List<BlockData> getPlayerChestLocations(final String name) {
        return new ArrayList(chests.values());
    }

    public static List<String> getPlayerIds() {
        return new ArrayList(activeUsers.keySet());
    }

    public static List<BlockData> getBlueprintAllWorlds(final String playerID) {
        return new ArrayList(activeUsers.get(playerID).getPlayerBlocks().values());
    }

    public static List<Integer> getBlueprintBlockTypes(final String playerID, final String worldID) {
        List<Integer> blockList = new CopyOnWriteArrayList();
        for (BlockData data : activeUsers.get(playerID).getPlayerBlocks().values()) {
            if (data.getBlockWorld().getName().equalsIgnoreCase(worldID) && !blockList.contains(data.getType())) {
                blockList.add(data.getType());
            }
        }
        return blockList;
    }

    public static int getBlueprintBlockOfTypInWorldNeeded(String playerID, int mat, String worldID) {
        int count = 0;
        for (BlockData data : activeUsers.get(playerID).getPlayerBlocks().values()) {
            if (data.getBlockWorld().getName().equalsIgnoreCase(worldID) && data.getType() == mat) {
                count++;
            }
        }
        return count;
    }

    public static List<BlockData> getBlueprintBuildBlockOfTypInWorld(String playerID, int mat, String worldID) {
        List<BlockData> blockList = new CopyOnWriteArrayList();
        for (BlockData data : activeUsers.get(playerID).getPlayerBlocks().values()) {
            if (data.getBlockWorld().getName().equalsIgnoreCase(worldID) && data.getType() == mat) {
                blockList.add(data);
            }
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
        BlockDataCache get = blocks.get(locX + "" + locY + "" + locZ + worldID);
        if (get != null) {
            return get.getType() == blockTypeID;
        } else {
            return false;
        }
    }

    public static void updateBlock(final Block clickedBlock) {
        final BlockDataCache bdc = blocks.get(new BlockData(clickedBlock).convertToKey());
        bdc.setData(clickedBlock.getData());
        bdc.setType(clickedBlock.getTypeId());
        Bukkit.getScheduler().runTaskAsynchronously(Blueprint.getPlugin(), new Runnable() {
            public void run() {
                try {
                    query("UPDATE blocks SET blockMeta = " + bdc.getData() + ",  blockID = " + bdc.getType() + " WHERE "
                            + "  blockX = " + bdc.getX()
                            + " AND blockY = " + bdc.getY()
                            + " AND blockZ = " + bdc.getZ()
                            + " AND world = '" + bdc.getBlockWorld().getName() + "';");
                } catch (SQLException ex) {
                    Blueprint.error("Couldn't activate player", ex);
                }
            }
        });
    }

    public static boolean isBlueprintBlockAtLocation(final Location location) {
        return isBlueprintBlockAtLocation(location.getBlockX(), location.getBlockY(), location.getBlockZ(), location.getWorld().getName());
    }

    public static boolean isBlueprintBlockAtLocation(final int locX, final int locY, final int locZ, final String worldID) {
        return blocks.get(locX + "" + locY + "" + locZ + worldID) != null;
    }

    public static List<ItemStack> getBlueprintItemTypes(final String playerID, final String worldID) {
        List<ItemStack> blockList = new CopyOnWriteArrayList();
        for (BlockDataCache data : activeUsers.get(playerID).getPlayerBlocks().values()) {
            blockList.add(data.getItemStack());
        }
        return blockList;
    }

    public static int getBlueprintBlockOfTypInWorldNeededFromItem(final String playerID, final int matID, final int matData, final String worldID) {
        ItemStack checkStack = new ItemStack(matID);
        checkStack.getData().setData((byte) matData);
        return getBlueprintBlockOfTypInWorldNeededFromItem(playerID, checkStack, worldID);
    }

    public static List<BlockData> getBlueprintBuildBlockOfTypInWorldFromItem(final String playerID, final int matID, final int matData, final String worldID) {
        ItemStack checkStack = new ItemStack(matID);
        checkStack.getData().setData((byte) matData);
        return getBlueprintBuildBlockOfTypInWorldFromItem(playerID, checkStack, worldID);
    }

    public static String getBlockOwnerAtLocation(final Location loc) {
        return blocks.get(loc.getBlockX() + "" + loc.getBlockY() + "" + loc.getBlockZ() + loc.getWorld().getName()).getPlayerID();
    }

    public static int getBlueprintBlockOfTypInWorldNeededFromItem(final String playerID, final ItemStack mat, final String worldID) {
        int count = 0;
        for (BlockDataCache data : activeUsers.get(playerID).getPlayerBlocks().values()) {
            if (data.getItemID() == mat.getTypeId() && data.getItemMeta() == mat.getData().getData()) {
                count++;
            }
        }
        return count;
    }

    public static List<BlockData> getBlueprintBuildBlockOfTypInWorldFromItem(final String playerID, final ItemStack mat, final String worldID) {
        List<BlockData> blockList = new CopyOnWriteArrayList();
        for (BlockDataCache data : activeUsers.get(playerID).getPlayerBlocks().values()) {
            if (data.getItemID() == mat.getTypeId() && data.getItemMeta() == mat.getData().getData()) {
                blockList.add(data);
            }
        }
        return blockList;
    }

    public static void removeBlueprintBlock(final BlockData placedBlock, final String world) {
        blocks.remove(placedBlock.getX() + "" + placedBlock.getY() + "" + placedBlock.getZ() + world);
        Bukkit.getScheduler().runTaskAsynchronously(Blueprint.getPlugin(), new Runnable() {
            public void run() {
                try {
                    query("DELETE FROM blocks WHERE blockID = '" + placedBlock.getType()
                            + "' AND blockX = " + placedBlock.getX()
                            + " AND blockY = " + placedBlock.getY()
                            + " AND blockZ = " + placedBlock.getZ()
                            + " AND blockMeta = " + (int) placedBlock.getData()
                            + " AND world = '" + world + "';");
                } catch (SQLException ex) {
                    Blueprint.error("Couldn't activate player", ex);
                }
            }
        });
    }

    public static void setupCache() {
        try {
            List<ResultData> result = query("SELECT playerID, locX, locY, locZ, gameMode, inventory, armour, active  FROM players;");
            for (ResultData data : result) {
                activeUsers.put(data.getKey("playerID"), new PlayerData(data.getKey("playerID"), Double.parseDouble(data.getKey("locX")), Double.parseDouble(data.getKey("locY")), Double.parseDouble(data.getKey("locZ")), Integer.parseInt(data.getKey("gameMode")), data.getKey("inventory"), data.getKey("armour"), data.getKey("active").equals("1")));
            }
            result = query("SELECT playerID, itemID, itemMeta, blockID, blockX, blockY,blockZ, blockMeta, world  FROM blocks;");
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
            result = query("SELECT playerID, world, blockX, blockY, blockZ  FROM chests;");
            for (ResultData data : result) {
                BlockDataChest bdc = new BlockDataChest(54, Integer.parseInt(data.getKey("blockX")), Integer.parseInt(data.getKey("blockY")), Integer.parseInt(data.getKey("blockZ")), (byte) 0, Bukkit.getWorld(data.getKey("world")), data.getKey("playerID"));
                activeUsers.get(data.getKey("playerID")).getPlayerChests().put(bdc.convertToKey(), bdc);
                chests.put(bdc.convertToKey(), bdc);
            }
        } catch (SQLException ex) {
            Logger.getLogger(DataHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void setupDB() {
        try {
            switch (databaseType) {
                case 0:
                    query("CREATE TABLE IF NOT EXISTS players (\n"
                            + " id INTEGER PRIMARY KEY AUTOINCREMENT,\n"
                            + " playerID TEXT NOT NULL ,\n"
                            + " locX REAL NOT NULL DEFAULT 0,\n"
                            + " locY REAL NOT NULL DEFAULT 0,\n"
                            + " locZ REAL NOT NULL DEFAULT 0,\n"
                            + " gameMode INTEGER NOT NULL,\n"
                            + " inventory MEDIUMTEXT NOT NULL DEFAULT '',\n"
                            + " armour MEDIUMTEXT NOT NULL DEFAULT '',\n"
                            + " active INTEGER NOT NULL DEFAULT 0,"
                            + " CONSTRAINT Play UNIQUE(playerID));");

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
                            + " world TEXT NOT NULL, "
                            + " CONSTRAINT CON UNIQUE(blockX, blockY, blockZ, world));");

                    query("CREATE TABLE IF NOT EXISTS chests (\n"
                            + " id INTEGER PRIMARY KEY AUTOINCREMENT,\n"
                            + " playerID TEXT NOT NULL,\n"
                            + " world TEXT NOT NULL,\n"
                            + " blockX INTEGER NOT NULL,\n"
                            + " blockY INTEGER NOT NULL,\n"
                            + " blockZ INTEGER NOT NULL, "
                            + " CONSTRAINT CON UNIQUE(blockX, blockY, blockZ, world));");
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
                            + " CONSTRAINT CON UNIQUE(blockX, blockY, blockZ, world),\n"
                            + " PRIMARY KEY (id));");

                    query("CREATE TABLE IF NOT EXISTS chests (\n"
                            + " id INT NOT NULL AUTO_INCREMENT,\n"
                            + " playerID VARCHAR(45) NOT NULL,\n"
                            + " world VARCHAR(45) NOT NULL,\n"
                            + " blockX INT NOT NULL,\n"
                            + " blockY INT NOT NULL,\n"
                            + " blockZ INT NOT NULL,\n"
                            + " CONSTRAINT CON UNIQUE(blockX, blockY, blockZ, world),\n"
                            + " PRIMARY KEY (id));");
            }
        } catch (SQLException ex) {
            Blueprint.error("Could not create needed table", ex);
        }
    }

    private static List<ResultData> query(final String query) throws SQLException {
        List<ResultData> data = new CopyOnWriteArrayList();
        synchronized (ConfigHandler.getTheDataHub()) {
            final Database tempDB = ConfigHandler.getTheDataHub();
            if (tempDB.getConnection().isClosed()) {
                if (!tempDB.open()) {
                    Blueprint.error("Could not work with database");
                }
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
        }
        return data;
    }

}
