/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.emberringstudios.blueprint;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

/**
 *
 * @author Max9403 <Max9403@live.com>
 */
public class ScoreBoardSystem {

    private static ConcurrentHashMap<Player, Player> players = new ConcurrentHashMap();

    public static void addPlayer(Player player) {
        addPlayer(player, player);
    }

    public static void addPlayer(Player player, Player player2) {
        if (player2 == null) {
            player2 = players.get(player) == null ? player : players.get(player);
        }
        players.put(player, player2);
        final String playerId = ConfigHandler.getDefaultBukkitConfig().getBoolean("use.UUIDs", true) ? player2.getUniqueId().toString() : player2.getPlayer().getName();
        List<ItemStack> blueprint = Commands.sortItemStack(DataHandler.getBlueprintItemTypes(playerId));
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        Scoreboard board = manager.getNewScoreboard();

        Objective objective = board.registerNewObjective(("materialsneeded" + playerId).substring(0, 16), "dummy");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        objective.setDisplayName("Materials Needed:");
        int count = 1;
        for (ItemStack data : blueprint) {
            Score score;
            if (count++ < 16) {
                if (ItemResolver.getName(new ItemTemp(data)).length() > 16) {
                    score = objective.getScore(Bukkit.getOfflinePlayer(ItemResolver.getName(new ItemTemp(data)).substring(0, 12) + "..."));
                } else {
                    score = objective.getScore(Bukkit.getOfflinePlayer(ItemResolver.getName(new ItemTemp(data))));
                }
                score.setScore(data.getAmount());
            }
        }
        player.setScoreboard(board);
        if (count > 15) {
            player.sendMessage("To many blocks to list in scoreboard");
        }
        if (blueprint.size() <= 0) {
            player.sendMessage((player == player2) ? "You don't need any matterials" : player2.getDisplayName() + " doesn't need any matterials");
        }
    }

    public static void updatePlayer(Player player) {
        final String playerId = ConfigHandler.getDefaultBukkitConfig().getBoolean("use.UUIDs", true) ? player.getUniqueId().toString() : player.getPlayer().getName();
        if (player.getScoreboard().getObjective(("materialsneeded" + playerId).substring(0, 16)) != null) {
            addPlayer(player, players.get(player));
        }
    }

    public static void removePlayer(Player player) {
        final String playerId = ConfigHandler.getDefaultBukkitConfig().getBoolean("use.UUIDs", true) ? player.getUniqueId().toString() : player.getPlayer().getName();
        if (player.getScoreboard().getObjective(("materialsneeded" + playerId).substring(0, 16)) != null) {
            player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
        }
    }

    public static void togglePlayer(Player player, Player player2) {
        final String playerId = ConfigHandler.getDefaultBukkitConfig().getBoolean("use.UUIDs", true) ? player.getUniqueId().toString() : player.getPlayer().getName();
        if (player.getScoreboard().getObjective(("materialsneeded" + playerId).substring(0, 16)) != null) {
            removePlayer(player);
        } else {
            addPlayer(player, players.get(player2));
        }
    }

    public static void togglePlayer(Player player) {
        final String playerId = ConfigHandler.getDefaultBukkitConfig().getBoolean("use.UUIDs", true) ? player.getUniqueId().toString() : player.getPlayer().getName();
        if (player.getScoreboard().getObjective(("materialsneeded" + playerId).substring(0, 16)) != null) {
            removePlayer(player);
        } else {
            addPlayer(player, players.get(player));
        }
    }
}
