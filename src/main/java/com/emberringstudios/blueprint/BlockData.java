package com.emberringstudios.blueprint;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

/**
 *
 * @author Max9403 <Max9403@live.com>
 */
public class BlockData {

    private int type;
    private int locX;
    private int locY;
    private int locZ;
    private byte data;
    private World blockWorld;

    public BlockData(final int blockType, final int locX, final int locY, final int locZ, final byte data) {
        this.type = blockType;
        this.locX = locX;
        this.locY = locY;
        this.locZ = locZ;
        this.data = data;
    }

    @Override
    public String toString() {
        return "BlockData{" + "type=" + type + ", locX=" + locX + ", locY=" + locY + ", locZ=" + locZ + ", data=" + data + ", blockWorld=" + blockWorld + '}';
    }

    public BlockData(final Material blockType, final int locX, final int locY, final int locZ, final byte data) {
        this.type = blockType.getId();
        this.locX = locX;
        this.locY = locY;
        this.locZ = locZ;
        this.data = data;
    }

    public BlockData(final Block blockData) {
        this.type = blockData.getTypeId();
        this.locX = blockData.getX();
        this.locY = blockData.getY();
        this.locZ = blockData.getZ();
        this.data = blockData.getData();
    }

    public BlockData(final int blockType, final int locX, final int locY, final int locZ, final byte data, final World blockWorld) {
        this.type = blockType;
        this.locX = locX;
        this.locY = locY;
        this.locZ = locZ;
        this.data = data;
        this.blockWorld = blockWorld;
    }

    public BlockData(final Material blockType, final int locX, final int locY, final int locZ, final byte data, final World blockWorld) {
        this.type = blockType.getId();
        this.locX = locX;
        this.locY = locY;
        this.locZ = locZ;
        this.data = data;
        this.blockWorld = blockWorld;
    }

    public BlockData(final Block blockData, final World blockWorld) {
        this.type = blockData.getTypeId();
        this.locX = blockData.getX();
        this.locY = blockData.getY();
        this.locZ = blockData.getZ();
        this.data = blockData.getData();
        this.blockWorld = blockWorld;
    }

    /**
     * @return the type
     */
    public int getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(int type) {
        this.type = type;
    }

    /**
     * @return the locX
     */
    public int getX() {
        return locX;
    }

    /**
     * @param locX the locX to set
     */
    public void setX(int locX) {
        this.locX = locX;
    }

    /**
     * @return the locY
     */
    public int getY() {
        return locY;
    }

    /**
     * @param locY the locY to set
     */
    public void setY(int locY) {
        this.locY = locY;
    }

    /**
     * @return the locZ
     */
    public int getZ() {
        return locZ;
    }

    /**
     * @param locZ the locZ to set
     */
    public void setZ(int locZ) {
        this.locZ = locZ;
    }

    /**
     * @return the data
     */
    public byte getData() {
        return data;
    }

    /**
     * @param data the data to set
     */
    public void setData(byte data) {
        this.data = data;
    }

    /**
     * @return the blockWorld
     */
    public World getBlockWorld() {
        return blockWorld;
    }

    /**
     * @param blockWorld the blockWorld to set
     */
    public void setBlockWorld(World blockWorld) {
        this.blockWorld = blockWorld;
    }

    public void loadBlockIntoWorld(World blockWorld) {
        setBlockWorld(blockWorld);
        try {
            loadBlockIntoWorld();
        } catch (NoWorldGivenException ex) {
            Logger.getLogger("Minecraft").log(Level.SEVERE, "Something went horribly wrong, this method should not give this error", ex);
        }
    }

    public void loadBlockIntoWorld() throws NoWorldGivenException {
        if (blockWorld == null) {
            throw new NoWorldGivenException("Developer forgot to set a world for this block");
        }
        Block modBlock = this.blockWorld.getBlockAt(locX, locY, locZ);
        modBlock.setTypeIdAndData(type, data, true);
    }

    public boolean equalToBlock(Block tempBlock) {
        return tempBlock.getTypeId() == type && tempBlock.getX() == locX && tempBlock.getY() == locY && tempBlock.getZ() == locZ && tempBlock.getData() == data;
    }
}
