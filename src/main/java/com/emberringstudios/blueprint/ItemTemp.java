/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.emberringstudios.blueprint;

import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Max9403 <Max9403@live.com>
 */
class ItemTemp {
    public int type;
    public byte data = (byte) 0;

    public ItemTemp() {
    }

    public ItemTemp(ItemStack data) {
        this.type = data.getTypeId();
        this.data = data.getData().getData();
    }

    public ItemTemp(int type) {
        this.type = type;
    }

    public ItemTemp(int type, byte data) {
        this.type = type;
        this.data = data;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + this.type;
        hash = 53 * hash + this.data;
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
        final ItemTemp other = (ItemTemp) obj;
        if (this.type != other.type) {
            return false;
        }
        if (this.data != other.data) {
            return false;
        }
        return true;
    }

}
