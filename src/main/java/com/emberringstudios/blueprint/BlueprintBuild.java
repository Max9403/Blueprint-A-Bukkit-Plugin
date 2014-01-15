package com.emberringstudios.blueprint;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

/**
 *
 * @author Max9403 <Max9403@live.com>
 */
class BlueprintBuild implements Runnable {

    private final Plugin plugin;

    public BlueprintBuild(Plugin plugin) {
        this.plugin = plugin;
    }

    public void run() {
        int maxBlocks =  ConfigHandler.getDefaultBukkitConfig().getInt("limits.blocks at a time", 20);
        int placedBlocks = 0;
//        ConcurrentHashMap<String, List<Location>> chestLocations = new ConcurrentHashMap();
//        for (String name : names) {
//            chestLocations.put(name, DataHandler.getPlayerChestLocations(name));
//        }

        for (String name : DataHandler.getPlayerIds()) {
            for (Location loc : DataHandler.getPlayerChestLocations(name)) {
                Inventory inv;
                if (loc.getBlock().getState() instanceof InventoryHolder) {
                    inv = ((InventoryHolder) loc.getBlock().getState()).getInventory();
                } else {
                    continue;
                }
                List<Integer> blueprint = DataHandler.getBlueprintBlockTypes(name, loc.getBlock().getWorld().getName());
                for (int mat : blueprint) {
                    if (inv.contains(mat)) {
                        HashMap<Integer, ? extends ItemStack> all = inv.all(mat);
                        Iterator it = all.entrySet().iterator();
                        while (it.hasNext()) {
                            Map.Entry pairs = (Map.Entry) it.next();
                            ItemStack temp = (ItemStack) pairs.getValue();
                            int inChest = temp.getAmount();
                            int needed = DataHandler.getBlueprintBlockOfTypInWorldNeeded(name, mat, loc.getBlock().getWorld().getName());
                            int remaining = (needed - inChest) > 0 ? needed - inChest : 0;
                            List<BlockData> blocks = DataHandler.getBlueprintBuildBlockOfTypInWorld(name, mat, loc.getBlock().getWorld().getName());
                            for (int counter = 0; counter < needed - remaining; counter++) {
                                if(placedBlocks >= maxBlocks){
                                    return;
                                }
                                inv.removeItem(new ItemStack(mat, 1));
                                loc.getBlock().getState().update(true);
                                DataHandler.removePlayerBlock(name, blocks.get(counter), loc.getBlock().getWorld().getName());
                                if (blocks.get(counter).getType() == Material.REDSTONE_TORCH_ON.getId()) {
                                    blocks.get(counter).setType(Material.REDSTONE_TORCH_OFF.getId());
                                }
                                try {
                                    blocks.get(counter).loadBlockIntoWorld();
                                } catch (NoWorldGivenException ex) {
                                    blocks.get(counter).loadBlockIntoWorld(loc.getBlock().getWorld());
                                }
                                 placedBlocks++;
                            }

                            it.remove(); // avoids a ConcurrentModificationException
                        }
                    }
                }
            }
        }
    }
}
