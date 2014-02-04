/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.emberringstudios.blueprint;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 *
 * @author Max9403 <Max9403@live.com>
 */
public class PermissionChecker {

    private static WorldGuardPlugin worldGuardPlugin = Bukkit.getServer().getPluginManager().getPlugin("WorldGuard") instanceof WorldGuardPlugin ? (WorldGuardPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldGuard") : null;

    public static boolean canBuild(final Player player, final Location location) {
        if (worldGuardPlugin == null) {
            return true;
        }
        return worldGuardPlugin.canBuild(player, location);
    }
}
