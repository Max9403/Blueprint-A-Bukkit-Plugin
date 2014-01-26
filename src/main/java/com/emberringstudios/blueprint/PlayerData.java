/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.emberringstudios.blueprint;

import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author Max9403 <Max9403@live.com>
 */
public class PlayerData {

    private final String playerID;
    private int gameMode;
    private String inv;
    private String armour;
    private boolean active;
    private ConcurrentHashMap<String, BlockDataCache> playerBlocks = new ConcurrentHashMap();
    private ConcurrentHashMap<String, BlockDataChest> playerChests = new ConcurrentHashMap();
    private double locX;
    private double locY;
    private double locZ;

    /**
     *
     * @param playerID
     */
    public PlayerData(final String playerID) {
        this.playerID = playerID;
    }

    /**
     *
     * @param playerID
     * @param locX
     * @param locY
     * @param locZ
     * @param gameMode
     * @param inv
     * @param armour
     * @param active
     */
    public PlayerData(final String playerID, final double locX, final double locY, final double locZ, final int gameMode, final String inv, final String armour, final boolean active) {
        this.playerID = playerID;
        this.locX = locX;
        this.locY = locY;
        this.locZ = locZ;
        this.gameMode = gameMode;
        this.inv = inv;
        this.armour = armour;
        this.active = active;
    }

    /**
     * @return the playerChests
     */
    public ConcurrentHashMap<String, BlockDataChest> getPlayerChests() {
        return playerChests;
    }

    /**
     * @return the playerID
     */
    public String getPlayerID() {
        return playerID;
    }

    /**
     * @return the gameMode
     */
    public int getGameMode() {
        return gameMode;
    }

    /**
     * @return the inv
     */
    public String getInventory() {
        return inv;
    }

    /**
     * @return the armour
     */
    public String getArmour() {
        return armour;
    }

    /**
     * @return the active
     */
    public boolean isActive() {
        return active;
    }

    /**
     * @return the playerBlocks
     */
    public ConcurrentHashMap<String, BlockDataCache> getPlayerBlocks() {
        return playerBlocks;
    }

    /**
     * @param gameMode the gameMode to set
     */
    public void setGameMode(int gameMode) {
        this.gameMode = gameMode;
    }

    /**
     * @param inv the inv to set
     */
    public void setInventory(String inv) {
        this.inv = inv;
    }

    /**
     * @param armour the armour to set
     */
    public void setArmour(String armour) {
        this.armour = armour;
    }

    /**
     * @param active the active to set
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * @param playerBlocks the playerBlocks to set
     */
    public void setPlayerBlocks(ConcurrentHashMap<String, BlockDataCache> playerBlocks) {
        this.playerBlocks = playerBlocks;
    }

    /**
     *
     * @return
     */
    public BasicLocation getLocation() {
        return new BasicLocation(locX, locY, locZ);
    }

    /**
     * @return the locX
     */
    public double getLocX() {
        return locX;
    }

    /**
     * @return the locY
     */
    public double getLocY() {
        return locY;
    }

    /**
     * @return the locZ
     */
    public double getLocZ() {
        return locZ;
    }

    /**
     * @param locX the locX to set
     */
    public void setLocX(double locX) {
        this.locX = locX;
    }

    /**
     * @param locY the locY to set
     */
    public void setLocY(double locY) {
        this.locY = locY;
    }

    /**
     * @param locZ the locZ to set
     */
    public void setLocZ(double locZ) {
        this.locZ = locZ;
    }

    /**
     * @param playerChests the playerChests to set
     */
    public void setPlayerChests(ConcurrentHashMap<String, BlockDataChest> playerChests) {
        this.playerChests = playerChests;
    }
}
