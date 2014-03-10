package com.emberringstudios.blueprint.background;

import com.emberringstudios.blueprint.ConfigHandler;
import com.emberringstudios.blueprint.DataHandler;
import com.emberringstudios.blueprint.NoWorldGivenException;
import com.emberringstudios.blueprint.ScoreBoardSystem;
import com.emberringstudios.blueprint.blockdata.BlockData;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

/**
 *
 * @author Max9403 <Max9403@live.com>
 */
public class BlueprintBuild implements Runnable {

    private final Plugin plugin;

    public BlueprintBuild(Plugin plugin) {
        this.plugin = plugin;
    }

    public void run() {
        int maxBlocks = ConfigHandler.getDefaultBukkitConfig().getInt("limits.blocks at a time", 20);
        int placedBlocks = 0;
//        ConcurrentHashMap<String, List<Location>> chestLocations = new ConcurrentHashMap();
//        for (String name : names) {
//            chestLocations.put(name, DataHandler.getPlayerChestLocations(name));
//        }

        for (String name : DataHandler.getPlayerIds()) {
            boolean obstruction = false;
            for (BlockData loc : DataHandler.getPlayerChestLocations(name)) {
                Inventory inv;
                if (loc.getLocation().getBlock().getState() instanceof InventoryHolder) {
                    inv = ((InventoryHolder) loc.getLocation().getBlock().getState()).getInventory();
                } else {
                    continue;
                }
                List<ItemStack> blueprint = DataHandler.getBlueprintItemTypes(name, loc.getLocation().getBlock().getWorld().getName());
                for (ItemStack mat : blueprint) {
                    if (inv.contains(mat.getTypeId())) {
                        HashMap<Integer, ? extends ItemStack> all = inv.all(mat.getTypeId());
                        Iterator it = all.entrySet().iterator();
                        while (it.hasNext()) {
                            Map.Entry pairs = (Map.Entry) it.next();
                            ItemStack temp = (ItemStack) pairs.getValue();
                            if (temp.getData().getData() == mat.getData().getData()) {
                                int inChest = temp.getAmount();
                                int needed = DataHandler.getBlueprintBlockOfTypInWorldNeededFromItem(name, mat, loc.getLocation().getBlock().getWorld().getName());
                                int remaining = (needed - inChest) > 0 ? needed - inChest : 0;
                                List<BlockData> blocks = DataHandler.getBlueprintBuildBlockOfTypInWorldFromItem(name, mat, loc.getLocation().getBlock().getWorld().getName());
                                for (int counter = 0; counter < needed - remaining; counter++) {
                                    if (placedBlocks >= maxBlocks) {
                                        return;
                                    }
                                    if (blocks.get(counter).equalToBlock(blocks.get(counter).getLocation().getBlock()) || blocks.get(counter).getLocation().getBlock().isEmpty()) {

                                        BlockState tempBlock = blocks.get(counter).updateBlockState(blocks.get(counter).getLocation().getBlock().getState());

                                        if (blocks.get(counter).getType() == Material.REDSTONE_TORCH_ON.getId()) {
                                            blocks.get(counter).setType(Material.REDSTONE_TORCH_OFF.getId());
                                        }
                                        try {
                                            blocks.get(counter).loadBlockIntoWorld();
                                        } catch (NoWorldGivenException ex) {
                                            blocks.get(counter).loadBlockIntoWorld(loc.getLocation().getBlock().getWorld());
                                        }
                                        if (!ConfigHandler.getDefaultBukkitConfig().getBoolean("use.UUIDs", true)) {

                                            BlockPlaceEvent event = new BlockPlaceEvent(blocks.get(counter).getLocation().getBlock(), tempBlock, blocks.get(counter).getLocation().getBlock(), new ItemStack(blocks.get(counter).getLocation().getBlock().getType()), Bukkit.getPlayerExact(name), true);

                                            Bukkit.getServer().getPluginManager().callEvent(event);
                                            if (!event.isCancelled()) {
                                                tempBlock.update();
                                            }
                                        } else {
                                            for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                                                if (player.getUniqueId().toString().equalsIgnoreCase(name)) {
                                                    ScoreBoardSystem.updatePlayer(player);
                                                    BlockPlaceEvent event;
                                                    event = new BlockPlaceEvent(blocks.get(counter).getLocation().getBlock(), blocks.get(counter).getLocation().getBlock().getState(), blocks.get(counter).getLocation().getBlock(), new ItemStack(blocks.get(counter).getLocation().getBlock().getType()), player, true);

                                                    Bukkit.getServer().getPluginManager().callEvent(event);
                                                    if (!event.isCancelled()) {
                                                        tempBlock.update();
                                                    }
                                                }
                                            }
                                        }

                                        if (temp.getAmount() > 1) {
                                            temp.setAmount(temp.getAmount() - 1);
                                        } else {
                                            inv.remove(temp);
                                        }
                                        loc.getLocation().getBlock().getState().update(true);
                                        DataHandler.removePlayerBlock(name, blocks.get(counter), loc.getLocation().getBlock().getWorld().getName());
                                        placedBlocks++;
                                        if (!ConfigHandler.getDefaultBukkitConfig().getBoolean("use.UUIDs", true)) {
                                            ScoreBoardSystem.updatePlayer(Bukkit.getPlayerExact(name));
                                        } else {
                                            for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                                                if (player.getUniqueId().toString().equalsIgnoreCase(name)) {
                                                    ScoreBoardSystem.updatePlayer(player);
                                                }
                                            }
                                        }
                                    } else {
                                        obstruction = true;
                                    }
                                }
                            }
                            it.remove(); // avoids a ConcurrentModificationException
                        }
                    }
                }
            }
            if (obstruction) {
                if (!ConfigHandler.getDefaultBukkitConfig().getBoolean("use.UUIDs", true)) {
                    Bukkit.getPlayerExact(name).sendMessage("Something is obstruction your blueprint");
                } else {
                    for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                        if (player.getUniqueId().toString().equalsIgnoreCase(name)) {
                            player.sendMessage("Something is obstruction your blueprint");
                        }
                    }
                }
            }
        }
    }
}
