package com.emberringstudios.blueprint;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

/**
 *
 * @author Benjamin
 */
class BlueprintBuild implements Runnable {

    private Plugin plugin;

    public BlueprintBuild(Plugin plugin) {
        this.plugin = plugin;
    }

    public void run() {
//        ConcurrentHashMap<String, List<Location>> chestLocations = new ConcurrentHashMap();
//        for (String name : names) {
//            chestLocations.put(name, DataHandler.getPlayerChestLocation(name));
//        }

        for (String name : DataHandler.getPlayerIds()) {
            for (Location loc : DataHandler.getPlayerChestLocation(name)) {
                if (loc.getBlock().getType() == Material.CHEST) {
                    Inventory inv;
                    if (loc.getBlock().getState() instanceof Chest) {
                        inv = ((Chest) loc.getBlock().getState()).getInventory();
                    } else if (loc.getBlock().getState() instanceof DoubleChest) {
                        inv = ((DoubleChest) loc.getBlock().getState()).getInventory();
                    } else {
                        break;
                    }
                    List<Integer> blueprint = DataHandler.getBlueprintBlockTypes(name, loc.getBlock().getWorld().getName());
                    for (int mat : blueprint) {
                        if (inv.contains(mat)) {
                            HashMap<Integer, ? extends ItemStack> all = inv.all(mat);
                            Iterator it = all.entrySet().iterator();
                            while (it.hasNext()) {
                                Map.Entry pairs = (Map.Entry) it.next();
                                System.out.println(pairs.getKey() + " = " + pairs.getValue());
                                ItemStack temp = (ItemStack) pairs.getValue();
                                int inChest = temp.getAmount();
                                int needed = DataHandler.getBlueprintBlockOfTypInWorldNeeded(name, mat, loc.getBlock().getWorld().getName());
                                int remaining = (needed - inChest) > 0 ? needed - inChest : 0;
                                List<BlockData> blocks = DataHandler.getBlueprintBuildBlockOfTypInWorld(name, mat, loc.getBlock().getWorld().getName());
                                for (int counter = 0; counter < needed - remaining; counter++) {
                                    inv.removeItem(new ItemStack(mat, 1));
                                    loc.getBlock().getState().update(true);
                                    try {
                                        blocks.get(counter).loadBlockIntoWorld();
                                    } catch (NoWorldGivenException ex) {
                                        blocks.get(counter).loadBlockIntoWorld(loc.getBlock().getWorld());
                                    }
                                    DataHandler.removePlayerBlock(name, blocks.get(counter), loc.getBlock().getWorld().getName());
                                }

                                it.remove(); // avoids a ConcurrentModificationException
                            }
                        }
                    }
                } else {

                }
            }
        }
    }
}
