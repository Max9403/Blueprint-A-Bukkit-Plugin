package com.emberringstudios.blueprint;

import com.emberringstudios.blueprint.background.BlockSetter;
import com.emberringstudios.blueprint.blockdata.BlockData;
import com.emberringstudios.blueprint.blockdata.BlockDataChest;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.LazyMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.yaml.snakeyaml.Yaml;

/**
 * If you want me to change this PM me or leave a comment
 *
 * @author Max9403 <Max9403@live.com>
 */
public class Commands {

    /**
     *
     */
    public static void register() {
        Blueprint.getPlugin().getCommand("blueprint").setExecutor(new BlueprintCommand());
        Blueprint.getPlugin().getCommand("markresourcechest").setExecutor(new MarkCommand());
        Blueprint.getPlugin().getCommand("unmarkresourcechest").setExecutor(new UnmarkCommand());
        Blueprint.getPlugin().getCommand("listresources").setExecutor(new ResourceLister());
        Blueprint.getPlugin().getCommand("resourceboard").setExecutor(new ResourceScoreboard());
    }

    private static class MarkCommand implements CommandExecutor {

        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (sender.hasPermission("blueprint.mark")) {
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
                } else {
                    sender.sendMessage("You must be a player!");
                }
            }
            return true;
        }

    }

    private static class BlueprintCommand implements CommandExecutor {

        public boolean onCommand(CommandSender sender, Command cmnd, String string, String[] strings) {
            if (sender.hasPermission("blueprint.switch")) {
                if (sender instanceof Player) {
                    final Player player = (Player) sender;
                    final String playerId = ConfigHandler.getDefaultBukkitConfig().getBoolean("use.UUIDs", true) ? player.getUniqueId().toString() : player.getPlayer().getName();
                    if (DataHandler.isPlayerActive(playerId)) {
                        PlayerInventory tempStore = DataHandler.deactivatePlayer(playerId);
                        player.getInventory().setArmorContents(tempStore.getArmour());
                        player.getInventory().setContents(tempStore.getItems());
                        player.teleport(DataHandler.getPlayerLocation(playerId));
                        BlockSetter.getBlocks().airAll(DataHandler.getBlueprint(playerId, player.getWorld().getName()));
                        player.setGameMode(DataHandler.getOriginalPlayerGameMode(playerId));
                        player.sendMessage("You are no longer in blueprint mode, just gona deconstruct it");
                    } else {
                        if (PlayerListener.isPlayerDamamged(player)) {
                            player.sendMessage("You are still to warn out from a fight to start building");
                        } else {
                            Yaml durpStore = new Yaml();
                            String items = durpStore.dump(ItemSerial.serializeItemList(player.getInventory().getContents()));
                            String armour = durpStore.dump(ItemSerial.serializeItemList(player.getInventory().getArmorContents()));

                            DataHandler.setPlayer(playerId, player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), items.replaceAll("'", "''"), armour.replaceAll("'", "''"), 1, player.getGameMode(), player.getWorld().getName());

                            List<BlockDataChest> playerChestLocations = DataHandler.getPlayerChestLocations(playerId);

                            boolean resCheck = false;
                            for (BlockData loc : playerChestLocations) {
                                Inventory inv;
                                if (loc.getLocation().getBlock().getState() instanceof InventoryHolder) {
                                    inv = ((InventoryHolder) loc.getLocation().getBlock().getState()).getInventory();
                                    for (ItemStack check : inv.getContents()) {
                                        if (check != null) {
                                            resCheck = true;
                                            break;
                                        }
                                    }
                                }
                                if (resCheck) {
                                    resCheck = true;
                                    break;
                                }
                            }
                            if (resCheck) {
                                player.sendMessage(ChatColor.RED + "There are blocks in your resource chest" + (playerChestLocations.size() > 1 ? "s. " : ". ") + "Blocks in your resource chest will still placed even when in blueprint mode and will have to be removed manualy");
                            }
                            BlockSetter.getBlocks().addAll(DataHandler.getBlueprint(playerId, player.getWorld().getName()));
                            player.setGameMode(GameMode.CREATIVE);
                            player.sendMessage("You are now in blueprint mode, just busy reconstructing it");
                        }
                    }
                } else {
                    sender.sendMessage("You must be a player!");
                }
            }
            return true;
        }
    }

    private static class ResourceLister implements CommandExecutor {

        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (sender.hasPermission("blueprint.listresources")) {
                if (sender.hasPermission("blueprint.listresources.others") && args.length > 0) {
                    List<ItemStack> blueprint;
                    for (String part : args) {
                        if (ConfigHandler.getDefaultBukkitConfig().getBoolean("use.UUIDs", true)) {
                            Player player = Bukkit.getServer().getPlayer(part);
                            if (player == null) {
                                sender.sendMessage("Can only get player when he/she is online in UUID mode");
                                continue;
                            } else {
                                blueprint = sortItemStack(DataHandler.getBlueprintItemTypes(player.getUniqueId().toString()));
                            }
                        } else {
                            blueprint = sortItemStack(DataHandler.getBlueprintItemTypes(part));
                        }
                        if (blueprint.size() > 0) {
                            String message = part + " needs:";
                            for (ItemStack data : blueprint) {
                                message += "\n" + ItemResolver.getName(new ItemTemp(data)) + ": " + data.getAmount();
                            }
                            sender.sendMessage(message);
                        } else {
                            sender.sendMessage(part + " needs no matterials");
                        }
                    }
                    return true;
                } else {
                    if (sender instanceof Player) {
                        Player player = (Player) sender;
                        final String playerId = ConfigHandler.getDefaultBukkitConfig().getBoolean("use.UUIDs", true) ? player.getUniqueId().toString() : player.getPlayer().getName();
                        List<ItemStack> blueprint = sortItemStack(DataHandler.getBlueprintItemTypes(playerId));
                        if (blueprint.size() > 0) {
                            String message = "You need:";
                            for (ItemStack data : blueprint) {
                                message += "\n" + ItemResolver.getName(new ItemTemp(data)) + ": " + data.getAmount();
                            }
                            sender.sendMessage(message);
                        } else {
                            sender.sendMessage("You need no matterials");
                        }
                    } else {
                        sender.sendMessage("You must be a player!");
                    }
                }
            }
            return true;
        }
    }

    private static class ResourceScoreboard implements CommandExecutor {

        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (sender.hasPermission("blueprint.resourceboard")) {
                if (sender instanceof Player) {
                    ScoreBoardSystem.togglePlayer((Player) sender);
                } else {
                    sender.sendMessage("You must be a player!");
                }
            }
            return true;
        }
    }

    public static List<ItemStack> sortItemStack(List<ItemStack> items) {
        List<ItemStack> list = new CopyOnWriteArrayList();
        for (ItemStack item : items) {
            int contains = -1;
            for (int count = 0; count < list.size(); count++) {
                if (list.get(count).isSimilar(item)) {
                    contains = count;
                    break;
                }
            }
            if (contains > -1) {
                list.get(contains).setAmount(list.get(contains).getAmount() + 1);
            } else {
                list.add(item);
            }
        }
        return list;
    }

    private static class UnmarkCommand implements CommandExecutor {

        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (sender.hasPermission("blueprint.unmark")) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    if (player.hasMetadata("inUnmarkMode")) {
//                        DataHandler.addPlayerChest(ConfigHandler.getDefaultBukkitConfig().getBoolean("use.UUIDs", true) ? player.getUniqueId().toString() : player.getName(), pie.getClickedBlock());
                        for (MetadataValue meta : player.getMetadata("inUnmarkMode")) {
                            if (meta.getOwningPlugin() == Blueprint.getPlugin()) {
                                if (meta.asBoolean()) {
                                    player.setMetadata("inUnmarkMode", new LazyMetadataValue(Blueprint.getPlugin(), new Callable() {

                                        public Object call() throws Exception {
                                            return false;
                                        }
                                    }));
                                    player.sendMessage("Resource chest unmark cancled ");
                                    break;
                                } else {
                                    player.setMetadata("inUnmarkMode", new LazyMetadataValue(Blueprint.getPlugin(), new Callable() {

                                        public Object call() throws Exception {
                                            return true;
                                        }
                                    }));
                                    player.sendMessage("Now in resource chest unmarker, please right click a chest\nType command again to cancel ");
                                }
                            }
                        }
                    } else {
                        player.setMetadata("inUnmarkMode", new LazyMetadataValue(Blueprint.getPlugin(), new Callable() {

                            public Object call() throws Exception {
                                return true;
                            }
                        }));
                        player.sendMessage("Now in resource chest unmarker, please right click a chest\nType command again to cancel ");
                    }
                } else {
                    sender.sendMessage("You must be a player!");
                }
            }
            return true;
        }

    }
}
