/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.emberringstudios.blueprint;

import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Benjamin
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
