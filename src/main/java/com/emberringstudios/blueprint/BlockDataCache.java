/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.emberringstudios.blueprint;

import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Max9403 <Max9403@live.com>
 */
public class BlockDataCache extends BlockData {

    private String playerID;
    private int itemID;
    private int itemMeta;

    public BlockDataCache(int blockType, int locX, int locY, int locZ, byte data, World world, String playerID, int itemID, int itemMeta) {
        super(blockType, locX, locY, locZ, data, world);
        this.playerID = playerID;
        this.itemID = itemID;
        this.itemMeta = itemMeta;
    }

    public BlockDataCache(final Block blockData, String playerID, int itemID, int itemMeta) {
        super(blockData);
        this.playerID = playerID;
        this.itemID = itemID;
        this.itemMeta = itemMeta;
    }

    public BlockDataCache(final BlockData blockData, String playerID, int itemID, int itemMeta) {
        super(blockData.getType(), blockData.getX(), blockData.getY(), blockData.getZ(), blockData.getData(), blockData.getBlockWorld());
        this.playerID = playerID;
        this.itemID = itemID;
        this.itemMeta = itemMeta;
    }

    /**
     * @return the playerID
     */
    public String getPlayerID() {
        return playerID;
    }

    /**
     * @return the itemID
     */
    public int getItemID() {
        return itemID;
    }

    /**
     * @return the itemMeta
     */
    public int getItemMeta() {
        return itemMeta;
    }

    public ItemStack getItemStack() {
        ItemStack temp = new ItemStack(itemID);
        temp.getData().setData((byte) itemMeta);
        return temp;
    }
}
