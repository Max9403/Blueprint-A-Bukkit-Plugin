/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.emberringstudios.blueprint;

import org.bukkit.World;
import org.bukkit.block.Block;

/**
 *
 * @author Max9403 <Max9403@live.com>
 */
public class BlockDataChest extends BlockData{
    private String owner;

    public BlockDataChest(int blockType, int locX, int locY, int locZ, byte data, World blockWorld, String owner) {
        super(blockType, locX, locY, locZ, data, blockWorld);
        this.owner = owner;
    }

    BlockDataChest(Block placedBlock, String name) {
        super(placedBlock);
        this.owner = name;
    }

    /**
     * @return the owner
     */
    public String getOwner() {
        return owner;
    }

    /**
     * @param owner the owner to set
     */
    public void setOwner(String owner) {
        this.owner = owner;
    }
    
}
