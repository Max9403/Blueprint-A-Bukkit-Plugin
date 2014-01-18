/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.emberringstudios.blueprint;

import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;
import org.bukkit.block.Block;

/**
 *
 * @author Max9403 <Max9403@live.com>
 * @param <T>
 */
public class BlockDataList<T extends BlockData> extends CopyOnWriteArrayList<T> {

    public BlockDataList() {
        super();
    }

    public BlockDataList(Collection<? extends T> collection) {
        super(collection);
    }

    public int getBlockAtLocation(final int x, final int y, final int z) {
        int count = 0;
        for (BlockData block : this) {
            if (block.getX() == x && block.getY() == y && block.getZ() == z) {
                return count;
            }
            count++;
        }
        return -1;
    }

    @Override
    public boolean add(T e) {
        if (this.contains(e)) {
            return false;
        } else {
            int temp = getBlockAtLocation(e.getX(), e.getY(), e.getZ());
            if (temp >= 0) {
                T get = this.get(temp);
                get.setType(e.getType());
                get.setData(e.getData());
                this.set(temp, get);
                return true;
            } else {
                return super.add(e);
            }
        }
    }

    @Override
    public void add(int index, T e) {
        if (!this.contains(e)) {
            int temp = getBlockAtLocation(e.getX(), e.getY(), e.getZ());
            if (temp >= 0) {
                T get = this.get(temp);
                get.setType(e.getType());
                get.setData(e.getData());
                this.set(temp, get);
            } else {
                super.add(index, e);
            }
        }
    }

    @Override
    public boolean addAll(Collection<? extends T> collection) {
        BlockDataList<T> copy = new BlockDataList<T>(collection);
        copy.removeAll(this);
        for (T data : copy) {
            add(data);
        }
        return true;
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> collection) {
        BlockDataList<T> copy = new BlockDataList<T>(collection);
        copy.removeAll(this);
        for (T data : copy) {
            add(index++, data);
        }
        return true;
    }

    public void airAll(Collection<? extends T> collection) {
        for (T data : collection) {
            BlockData blockData = new BlockData(data);
            blockData.setType(0);
            add((T) blockData);
        }
    }
}
