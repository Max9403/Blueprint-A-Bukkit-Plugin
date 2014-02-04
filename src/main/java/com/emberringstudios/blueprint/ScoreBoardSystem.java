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

    private static final ConcurrentHashMap<Player, Scoreboard> scoreBoards = new ConcurrentHashMap();

    public static void addPlayer(Player player) {
        if (!scoreBoards.contains(player)) {
            final String playerId = ConfigHandler.getDefaultBukkitConfig().getBoolean("use.UUIDs", true) ? player.getUniqueId().toString() : player.getPlayer().getName();
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
            scoreBoards.put(player, board);
            player.setScoreboard(board);
            if (count > 15) {
                player.sendMessage("To many blocks to list in scoreboard");
            }
        } else {
            updatePlayer(player);
        }
    }

    public static void updatePlayer(Player player) {
        if (scoreBoards.containsKey(player)) {
            final String playerId = ConfigHandler.getDefaultBukkitConfig().getBoolean("use.UUIDs", true) ? player.getUniqueId().toString() : player.getPlayer().getName();
            List<ItemStack> blueprint = Commands.sortItemStack(DataHandler.getBlueprintItemTypes(playerId));
            Scoreboard board = scoreBoards.get(player);

            if (blueprint.size() > 0) {
                Objective objective = board.getObjective(("materialsneeded" + playerId).substring(0, 16));
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
            }
            scoreBoards.put(player, board);
            player.setScoreboard(board);
        }
    }

    public static void removePlayer(Player player) {
        player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
        scoreBoards.remove(player);
    }

    public static void togglePlayer(Player player) {

        if (scoreBoards.containsKey(player)) {
            removePlayer(player);
        } else {
            addPlayer(player);
        }
    }
}
