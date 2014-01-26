/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.emberringstudios.blueprint;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Max9403 <Max9403@live.com>
 */
public class BlockSetter implements Runnable {

    private static BlockDataList<BlockData> blocks = new BlockDataList<BlockData>();

    /**
     * @return the blocks
     */
    public static BlockDataList<BlockData> getBlocks() {
        return blocks;
    }

    /**
     * @param aBlocks the blocks to set
     */
    public static void setBlocks(BlockDataList<BlockData> aBlocks) {
        blocks = aBlocks;
    }

    /**
     *
     */
    public void run() {
        for (BlockData thatData : blocks) {
            if (thatData.getType() == 46) {
                continue;
            }
            try {
                if (thatData.equalToBlock(thatData.getBlockWorld().getBlockAt(thatData.getX(), thatData.getY(), thatData.getZ()))) {
                    DataHandler.removeBlueprintBlock(thatData, thatData.getBlockWorld().getName());
                }
                thatData.loadBlockIntoWorld();
                blocks.remove(thatData);
            } catch (NoWorldGivenException ex) {
                Logger.getLogger(Commands.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

}
