package com.emberringstudios.blueprint;

import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Max9403 <Max9403@live.com>
 */
public class PlayerInventory {
    private final ItemStack[] items;
    private final ItemStack[] armour;
    
    public PlayerInventory(final ItemStack[] items, final ItemStack[] armour){
        this.items = items;
        this.armour = armour;
    }

    /**
     * @return the items
     */
    public ItemStack[] getItems() {
        return items;
    }

    /**
     * @return the armour
     */
    public ItemStack[] getArmour() {
        return armour;
    }
}
