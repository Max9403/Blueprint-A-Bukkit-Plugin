/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.emberringstudios.blueprint.blockdata;

import org.bukkit.World;
import org.bukkit.block.Block;

/**
 *
 * @author Max9403 <Max9403@live.com>
 */
public class BlockDataChest extends BlockData {

    private String owner;

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 43 * hash + (this.owner != null ? this.owner.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final BlockDataChest other = (BlockDataChest) obj;
        if ((this.owner == null) ? (other.owner != null) : !this.owner.equals(other.owner)) {
            return false;
        }
        return true;
    }

    /**
     *
     * @param blockType
     * @param locX
     * @param locY
     * @param locZ
     * @param data
     * @param blockWorld
     * @param owner
     */
    public BlockDataChest(int blockType, int locX, int locY, int locZ, byte data, World blockWorld, String owner) {
        super(blockType, locX, locY, locZ, data, blockWorld);
        this.owner = owner;
    }

    public BlockDataChest(Block placedBlock, String name) {
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
