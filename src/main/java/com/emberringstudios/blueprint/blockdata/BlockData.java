package com.emberringstudios.blueprint.blockdata;

import com.emberringstudios.blueprint.NoWorldGivenException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;

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

    /**
     *
     * @return String value of the BlockData
     */
    @Override
    public String toString() {
        return "BlockData{" + "type=" + type + ", locX=" + locX + ", locY=" + locY + ", locZ=" + locZ + ", data=" + data + ", blockWorld=" + blockWorld.getName() + '}';
    }

    /**
     *
     * @param blockType
     * @param locX
     * @param locY
     * @param locZ
     * @param data
     */
    public BlockData(final int blockType, final int locX, final int locY, final int locZ, final byte data) {
        this.type = blockType;
        this.locX = locX;
        this.locY = locY;
        this.locZ = locZ;
        this.data = data;
    }

    /**
     *
     * @param blockType
     * @param locX
     * @param locY
     * @param locZ
     * @param data
     */
    public BlockData(final Material blockType, final int locX, final int locY, final int locZ, final byte data) {
        this.type = blockType.getId();
        this.locX = locX;
        this.locY = locY;
        this.locZ = locZ;
        this.data = data;
    }

    /**
     *
     * @param blockData
     */
    public BlockData(final BlockData blockData) {
        this.type = blockData.getType();
        this.locX = blockData.getX();
        this.locY = blockData.getY();
        this.locZ = blockData.getZ();
        this.data = blockData.getData();
        this.blockWorld = blockData.getBlockWorld();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 47 * hash + this.type;
        hash = 47 * hash + this.locX;
        hash = 47 * hash + this.locY;
        hash = 47 * hash + this.locZ;
        hash = 47 * hash + this.data;
        hash = 47 * hash + (this.blockWorld != null ? this.blockWorld.hashCode() : 0);
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
        final BlockData other = (BlockData) obj;
        if (this.type != other.type) {
            return false;
        }
        if (this.locX != other.locX) {
            return false;
        }
        if (this.locY != other.locY) {
            return false;
        }
        if (this.locZ != other.locZ) {
            return false;
        }
        if (this.data != other.data) {
            return false;
        }
        if (this.blockWorld != other.blockWorld && (this.blockWorld == null || !this.blockWorld.equals(other.blockWorld))) {
            return false;
        }
        return true;
    }

    /**
     *
     * @param blockData
     */
    public BlockData(final Block blockData) {
        this.type = blockData.getTypeId();
        this.locX = blockData.getX();
        this.locY = blockData.getY();
        this.locZ = blockData.getZ();
        this.data = blockData.getData();
        this.blockWorld = blockData.getWorld();
    }

    /**
     *
     * @param blockType
     * @param locX
     * @param locY
     * @param locZ
     * @param data
     * @param blockWorld
     */
    public BlockData(final int blockType, final int locX, final int locY, final int locZ, final byte data, final World blockWorld) {
        this.type = blockType;
        this.locX = locX;
        this.locY = locY;
        this.locZ = locZ;
        this.data = data;
        this.blockWorld = blockWorld;
    }

    /**
     *
     * @param blockType
     * @param locX
     * @param locY
     * @param locZ
     * @param data
     * @param blockWorld
     */
    public BlockData(final Material blockType, final int locX, final int locY, final int locZ, final byte data, final World blockWorld) {
        this.type = blockType.getId();
        this.locX = locX;
        this.locY = locY;
        this.locZ = locZ;
        this.data = data;
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
     *
     * @return
     */
    public Location getLocation() {
        return new Location(blockWorld, locX, locY, locZ);
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

    /**
     *
     * @param blockWorld
     */
    public void loadBlockIntoWorld(World blockWorld) {
        setBlockWorld(blockWorld);
        try {
            loadBlockIntoWorld();
        } catch (NoWorldGivenException ex) {
            Logger.getLogger("Minecraft").log(Level.SEVERE, "Something went horribly wrong, this method should not give this error", ex);
        }
    }

    /**
     *
     * @throws NoWorldGivenException
     */
    public void loadBlockIntoWorld() throws NoWorldGivenException {
        if (blockWorld == null) {
            throw new NoWorldGivenException("Developer forgot to set a world for this block");
        }
        Block modBlock = this.blockWorld.getBlockAt(locX, locY, locZ);
        modBlock.setTypeIdAndData(type, data, false);
    }

    /**
     *
     * @param tempBlock
     * @return
     */
    public boolean equalToBlock(Block tempBlock) {
        return tempBlock.getTypeId() == type && tempBlock.getX() == locX && tempBlock.getY() == locY && tempBlock.getZ() == locZ && tempBlock.getData() == data && tempBlock.getWorld() == blockWorld;
    }

    /**
     *
     * @return
     */
    public String convertToKey() {
        return locX + "" + locY + "" + locZ + blockWorld.getName();
    }

    public BlockState updateBlockState(final BlockState state) {
        state.setTypeId(type);
        state.setRawData(data);
        return state;
    }
}
