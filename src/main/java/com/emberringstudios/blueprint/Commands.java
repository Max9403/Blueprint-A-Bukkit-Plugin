package com.emberringstudios.blueprint;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.metadata.LazyMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.yaml.snakeyaml.Yaml;

/**
 * If you want me to change this PM me or leave a comment
 *
 * @author Max9403 <Max9403@live.com>
 */
public class Commands {

    public static void register() {
        Blueprint.getPlugin().getCommand("blueprint").setExecutor(new BlueprintCommand());
        Blueprint.getPlugin().getCommand("markresourcechest").setExecutor(new MarkCommand());
    }

    private static class MarkCommand implements CommandExecutor {

        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                if (player.hasMetadata("inMarkMode")) {
//                        DataHandler.addPlayerChest(ConfigHandler.getDefaultBukkitConfig().getBoolean("use.UUIDs", true) ? player.getUniqueId().toString() : player.getName(), pie.getClickedBlock());
                    for (MetadataValue meta : player.getMetadata("inMarkMode")) {
                        if (meta.getOwningPlugin() == Blueprint.getPlugin()) {
                            if (meta.asBoolean()) {
                                player.setMetadata("inMarkMode", new LazyMetadataValue(Blueprint.getPlugin(), new Callable() {

                                    public Object call() throws Exception {
                                        return false;
                                    }
                                }));
                                player.sendMessage("Resource chest mark cancled ");
                                break;
                            } else {
                                player.setMetadata("inMarkMode", new LazyMetadataValue(Blueprint.getPlugin(), new Callable() {

                                    public Object call() throws Exception {
                                        return true;
                                    }
                                }));
                                player.sendMessage("Now in resource chest marker, please right click a chest\nType command again to cancel ");
                            }
                        }
                    }
                } else {
                    player.setMetadata("inMarkMode", new LazyMetadataValue(Blueprint.getPlugin(), new Callable() {

                        public Object call() throws Exception {
                            return true;
                        }
                    }));
                    player.sendMessage("Now in resource chest marker, please right click a chest\nType command again to cancel ");
                }
            }
            return true;
        }

    }

    private static class BlueprintCommand implements CommandExecutor {

        public boolean onCommand(CommandSender sender, Command cmnd, String string, String[] strings) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                final String playerId = ConfigHandler.getDefaultBukkitConfig().getBoolean("use.UUIDs", true) ? player.getUniqueId().toString() : player.getPlayer().getName();
                if (player.getGameMode() == GameMode.CREATIVE) {
                    if (DataHandler.isPlayerActive(playerId)) {
                        PlayerInventory tempStore = DataHandler.deactivatePlayer(playerId);
                        player.getInventory().setArmorContents(tempStore.getArmour());
                        player.getInventory().setContents(tempStore.getItems());
                        player.teleport(DataHandler.getPlayerLocation(playerId).convertToLocation(player.getWorld()));
                        List<BlockData> blocks = DataHandler.getBlueprint(playerId, player.getWorld().getName());
                        for (BlockData thatData : blocks) {
                            try {
                                thatData.setType(Material.AIR.getId());
                                thatData.loadBlockIntoWorld();
                            } catch (NoWorldGivenException ex) {
                                Logger.getLogger(Commands.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                        player.setGameMode(DataHandler.getOriginalPlayerGameMode(playerId));
                        player.sendMessage("You are no longer in blueprint mode");
                    }
                } else {
                    if (!DataHandler.setOriginalPlayerGameMode(playerId, player.getGameMode())) {
                        sender.sendMessage("Something went wrong, we'll send the goblins to fix it");
                        return true;
                    }

                    Yaml durpStore = new Yaml();
                    String items = durpStore.dump(ItemSerial.serializeItemList(player.getInventory().getContents()));
                    String armour = durpStore.dump(ItemSerial.serializeItemList(player.getInventory().getArmorContents()));

                    DataHandler.activatePlayer(playerId, items, armour);
                    DataHandler.setPlayerLocation(playerId, player.getLocation());
                    List<BlockData> blocks = DataHandler.getBlueprint(playerId, player.getWorld().getName());
                    for (BlockData thatData : blocks) {
                        try {
                            Block tempBlock = player.getWorld().getBlockAt(thatData.getX(), thatData.getY(), thatData.getZ());
                            if (player.getWorld().getBlockAt(thatData.getX(), thatData.getY(), thatData.getZ()).getTypeId() == 0) {
                                thatData.loadBlockIntoWorld();
                            } else {
                                if (thatData.equalToBlock(tempBlock)) {
                                    DataHandler.removePlayerBlock(playerId, thatData, player.getWorld().getName());
                                }
                            }
                        } catch (NoWorldGivenException ex) {
                            Logger.getLogger(Commands.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    player.setGameMode(GameMode.CREATIVE);
                    player.sendMessage("You are now in blueprint mode");
                }
            } else {
                sender.sendMessage("You must be a player!");
            }
            return true;
        }
    }
}
