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
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerEggThrowEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.LazyMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.yaml.snakeyaml.Yaml;

/**
 *
 * @author Max9403 <Max9403@live.com>
 */
public class PlayerListener implements Listener {

    private final static List<Integer> ignoreList = ConfigHandler.getGreenlistConfig().getIntegerList("Greenlist Items");
    private final static List<Player> damaged = new CopyOnWriteArrayList();
    private final static List<BlockPlaceEvent> blockPlacing = new CopyOnWriteArrayList();

    public static void addBlocPlaceEvent(final BlockPlaceEvent event) {
        blockPlacing.add(event);
    }

    /**
     * @return the damaged
     */
    public static List<Player> getDamaged() {
        return damaged;
    }

    public static boolean isPlayerDamamged(Player player) {
        return damaged.contains(player);
    }

    @EventHandler
    public void onPlayerCommandPreprocessEvent(final PlayerCommandPreprocessEvent pcpe) {
        final String playerId = ConfigHandler.getDefaultBukkitConfig().getBoolean("use.UUIDs", true) ? pcpe.getPlayer().getUniqueId().toString() : pcpe.getPlayer().getPlayer().getName();
        if (DataHandler.isPlayerActive(playerId)) {

            if (ConfigHandler.getDefaultBukkitConfig().getBoolean("limits.blacklist.commands", true)) {
                for (String command : ConfigHandler.getCommandsBlacklistConfig().getStringList("List Commands")) {
                    if (pcpe.getMessage().toLowerCase().matches(command)) {
                        pcpe.setCancelled(true);
                        pcpe.getPlayer().sendMessage("Can't run that command in blueprint mode");
                    }
                }
            } else {

                for (String command : ConfigHandler.getCommandsBlacklistConfig().getStringList("List Commands")) {
                    if (pcpe.getMessage().toLowerCase().matches(command)) {
                        return;
                    }
                }
                pcpe.setCancelled(true);
                pcpe.getPlayer().sendMessage("Can't run that command in blueprint mode");
            }
        }
    }

    @EventHandler
    public void onPlayerEggThrowEvent(final PlayerEggThrowEvent pete) {
        final String playerId = ConfigHandler.getDefaultBukkitConfig().getBoolean("use.UUIDs", true) ? pete.getPlayer().getUniqueId().toString() : pete.getPlayer().getPlayer().getName();
        if (DataHandler.isPlayerActive(playerId)) {
            pete.setHatching(false);
        }
    }

    @EventHandler
    public void onPlayerItemConsumeEvent(final PlayerItemConsumeEvent pice) {
        final String playerId = ConfigHandler.getDefaultBukkitConfig().getBoolean("use.UUIDs", true) ? pice.getPlayer().getUniqueId().toString() : pice.getPlayer().getPlayer().getName();
        if (DataHandler.isPlayerActive(playerId)) {
            pice.setCancelled(true);
        }
    }

    @EventHandler
    public void onPotionSplashEvent(final PotionSplashEvent pse) {
        if (pse.getEntity().getShooter() instanceof Player) {
            final Player player = (Player) pse.getEntity().getShooter();
            final String playerId = ConfigHandler.getDefaultBukkitConfig().getBoolean("use.UUIDs", true) ? player.getUniqueId().toString() : player.getPlayer().getName();
            if (DataHandler.isPlayerActive(playerId)) {
                pse.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onProjectileLaunch(final ProjectileLaunchEvent ple) {
        if (ple.getEntity().getShooter() instanceof Player) {
            final Player player = (Player) ple.getEntity().getShooter();
            final String playerId = ConfigHandler.getDefaultBukkitConfig().getBoolean("use.UUIDs", true) ? player.getUniqueId().toString() : player.getPlayer().getName();
            if (DataHandler.isPlayerActive(playerId)) {
                ple.setCancelled(true);
            }
        }
    }

    /**
     *
     * @param bbe
     */
    @EventHandler
    public void onBlockBreak(final BlockBreakEvent bbe) {
        if (DataHandler.isPlayerActive(ConfigHandler.getDefaultBukkitConfig().getBoolean("use.UUIDs", true) ? bbe.getPlayer().getUniqueId().toString() : bbe.getPlayer().getName())) {
            if (DataHandler.checkPlayerBlock(ConfigHandler.getDefaultBukkitConfig().getBoolean("use.UUIDs", true) ? bbe.getPlayer().getUniqueId().toString() : bbe.getPlayer().getName(), bbe.getBlock())) {
                DataHandler.removePlayerBlock(ConfigHandler.getDefaultBukkitConfig().getBoolean("use.UUIDs", true) ? bbe.getPlayer().getUniqueId().toString() : bbe.getPlayer().getName(), bbe.getBlock(), bbe.getPlayer().getWorld().getName());
            } else {
                if (!ignoreList.contains(bbe.getBlock().getTypeId())) {
                    bbe.setCancelled(true);
                }
            }
        } else {
            if (DataHandler.isBlueprintBlock(bbe.getBlock())) {
                bbe.setCancelled(true);
            }
        }
        if (bbe.getBlock().getTypeId() == 54) {
            if (DataHandler.isPlayerChest(bbe.getBlock()) || DataHandler.isPlayerChest(bbe.getBlock().getRelative(BlockFace.NORTH)) || DataHandler.isPlayerChest(bbe.getBlock().getRelative(BlockFace.EAST)) || DataHandler.isPlayerChest(bbe.getBlock().getRelative(BlockFace.WEST)) || DataHandler.isPlayerChest(bbe.getBlock().getRelative(BlockFace.EAST)) || DataHandler.isPlayerChest(bbe.getBlock().getRelative(BlockFace.SOUTH))) {
                DataHandler.removePlayerChest(ConfigHandler.getDefaultBukkitConfig().getBoolean("use.UUIDs", true) ? bbe.getPlayer().getUniqueId().toString() : bbe.getPlayer().getName(), bbe.getBlock());
            }
        }
        ScoreBoardSystem.updatePlayer(bbe.getPlayer());
    }

    /**
     *
     * @param bfte
     */
    @EventHandler
    public void onBlockFromTo(BlockFromToEvent bfte) {
        if (DataHandler.isBlueprintBlockAtLocation(bfte.getBlock().getLocation()) && DataHandler.isPlayerActive(DataHandler.getBlockOwnerAtLocation(bfte.getBlock().getLocation()))) {
            bfte.setCancelled(true);
        }
    }

    /**
     *
     * @param bpee
     */
    @EventHandler
    public void onBlockPistonEvent(BlockPistonExtendEvent bpee) {
        for (Block pushed : bpee.getBlocks()) {
            if (DataHandler.isBlueprintBlockAtLocation(pushed.getLocation()) && DataHandler.isPlayerActive(DataHandler.getBlockOwnerAtLocation(pushed.getLocation()))) {
                bpee.setCancelled(true);
                break;
            }
        }
    }

    /**
     *
     * @param bpe
     */
    @EventHandler
    public void onBlockPlace(final BlockPlaceEvent bpe) {
        ScoreBoardSystem.updatePlayer(bpe.getPlayer());
        if (blockPlacing.contains(bpe)) {
            blockPlacing.remove(bpe);
            return;
        }
        if (!bpe.isCancelled() && DataHandler.isPlayerActive(ConfigHandler.getDefaultBukkitConfig().getBoolean("use.UUIDs", true) ? bpe.getPlayer().getUniqueId().toString() : bpe.getPlayer().getName()) && bpe.canBuild() && PermissionChecker.canBuild(bpe.getPlayer(), bpe.getBlockPlaced().getLocation())) {
            if (ConfigHandler.getDefaultBukkitConfig().getBoolean("limits.blacklist")) {
                if (ConfigHandler.getBlockBlacklistConfig().getIntegerList("List Items").contains(bpe.getBlockPlaced().getTypeId()) || ConfigHandler.getBlockBlacklistConfig().getIntegerList("List Items").contains(bpe.getItemInHand().getTypeId())) {
                    bpe.setCancelled(true);
                    return;
                }
            } else {
                if (!ConfigHandler.getBlockBlacklistConfig().getIntegerList("List Items").contains(bpe.getBlockPlaced().getTypeId()) && ConfigHandler.getBlockBlacklistConfig().getIntegerList("List Items").contains(bpe.getItemInHand().getTypeId())) {
                    bpe.setCancelled(true);
                    return;
                }
            }
            DataHandler.addPlayerBlock(ConfigHandler.getDefaultBukkitConfig().getBoolean("use.UUIDs", true) ? bpe.getPlayer().getUniqueId().toString() : bpe.getPlayer().getName(), bpe.getItemInHand(), new BlockData(bpe.getBlockPlaced()));

            final Block temp = bpe.getBlockPlaced();
            final int tempId = bpe.getBlockPlaced().getTypeId();
            Bukkit.getScheduler().runTask(Blueprint.getPlugin(), new Runnable() {
                public void run() {
                    if (tempId == temp.getTypeId()) {
                        DataHandler.updateBlock(temp);
                    }
                }
            });

            if (bpe.getBlockPlaced().getType() == Material.REDSTONE_TORCH_ON) {
                bpe.getBlockPlaced().setType(Material.REDSTONE_TORCH_OFF);
            } else if (bpe.getBlockPlaced().getType() == Material.TNT) {
                bpe.getPlayer().sendMessage("TNT added to blueprint");
                bpe.setCancelled(true);
            }
            ScoreBoardSystem.updatePlayer(bpe.getPlayer());
//            CraftWorld cWorld = (CraftWorld) bbe.getPlayer().getWorld();
//            EntityBlock eMeteor = new EntityBlock(cWorld.getHandle(), Block.e(bbe.getBlock().getTypeId()), vv.getBlockX(), vv.getBlockY(), vv.getBlockZ());
//            cWorld.getHandle().addEntity(eMeteor, SpawnReason.NATURAL);
//            FallingBlock proj =  bbe.getPlayer().getWorld().spawnFallingBlock(bbe.getBlockPlaced().getLocation(), bbe.getBlockPlaced().getType(), bbe.getBlockPlaced().getData());
//            proj.setVelocity(new Vector(0, 0, 1));
//            bbe.getBlockPlaced().setType(Material.PISTON_MOVING_PIECE);
//            PacketContainer fakeBlock = Blueprint.getProtocolManager().createPacket(PacketType.Play.Server.BLOCK_CHANGE);
//            //fakeBlock.getIntegers().write(0, localBlock.getX() + 1).write(1, localBlock.getY() + 1).write(2, localBlock.getZ() + 1);
//            fakeBlock.getBlocks().write(0, localBlock.getType());
//            //tempWorld.getBlockAt(localBlock.getLocation()).setType(Material.AIR);
//            bbe.setCancelled(true);
//            try {
//                Blueprint.getProtocolManager().sendServerPacket(bbe.getPlayer(), fakeBlock);
//            } catch (InvocationTargetException ex) {
//                Logger.getLogger(PlayerListener.class.getName()).log(Level.SEVERE, null, ex);
//            }
//            World tempWorld = bbe.getPlayer().getWorld();
//            tempWorld.spawnFallingBlock(localBlock.getLocation(), localBlock.getType(), localBlock.getData());
//            bbe.getPlayer().sendBlockChange(localBlock.getLocation(), Material.BEDROCK, (byte) 0);
        }
    }

    /**
     *
     * @param bre
     */
    @EventHandler
    public void onBlockRedstoneChange(BlockRedstoneEvent bre) {
        if (DataHandler.isBlueprintBlock(bre.getBlock())) {
            bre.setNewCurrent(0);
        }
    }

    /**
     *
     * @param edea
     */
    @EventHandler
    public void onEntityDamage(EntityDamageEvent edea) {
        if (edea instanceof EntityDamageByEntityEvent) {
            final EntityDamageByEntityEvent ede = (EntityDamageByEntityEvent) edea;
            if (ede.getDamager() instanceof Player) {
                Player player = (Player) ede.getDamager();
                {
                    if (DataHandler.isPlayerActive(ConfigHandler.getDefaultBukkitConfig().getBoolean("use.UUIDs", true) ? player.getUniqueId().toString() : player.getName())) {
                        ede.setCancelled(true);
                    } else {
                        if (ede.getEntity() instanceof Player) {
                            damaged.add((Player) ede.getEntity());
                            Bukkit.getScheduler().runTaskLaterAsynchronously(Blueprint.getPlugin(), new Runnable() {

                                public void run() {
                                    getDamaged().remove((Player) ede.getEntity());
                                }
                            }, ConfigHandler.getDefaultBukkitConfig().getInt("limits.combat cooldown", 200));

                            damaged.add((Player) ede.getDamager());
                            Bukkit.getScheduler().runTaskLaterAsynchronously(Blueprint.getPlugin(), new Runnable() {

                                public void run() {
                                    getDamaged().remove((Player) ede.getDamager());
                                }
                            }, ConfigHandler.getDefaultBukkitConfig().getInt("limits.combat cooldown", 200));
                        }
                    }
                }
            }
            if (!ede.isCancelled() && ede.getEntity() instanceof Player) {
                damaged.add((Player) ede.getEntity());
                Bukkit.getScheduler().runTaskLaterAsynchronously(Blueprint.getPlugin(), new Runnable() {

                    public void run() {
                        getDamaged().remove((Player) ede.getEntity());
                    }
                }, ConfigHandler.getDefaultBukkitConfig().getInt("limits.combat cooldown", 200));
            }
        } else if (edea instanceof EntityDamageByBlockEvent) {
            EntityDamageByBlockEvent ede = (EntityDamageByBlockEvent) edea;
            if (DataHandler.isBlueprintBlock(ede.getEntity().getLocation().getBlock())) {
                edea.setCancelled(true);
            }
        }
    }

    /**
     *
     * @param ice
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent ice) {
        if (ice.getWhoClicked() instanceof Player) {
            Player player = (Player) ice.getWhoClicked();
            if (player.getInventory() != ice.getInventory()) {
                if (DataHandler.isPlayerActive(ConfigHandler.getDefaultBukkitConfig().getBoolean("use.UUIDs", true) ? player.getUniqueId().toString() : player.getName())) {
                    ice.setResult(Result.DENY);
                    ice.setCancelled(true);
                    player.updateInventory();
                }
            }
        }
    }

    /**
     *
     * @param ioe
     */
    @EventHandler
    public void onInventoryOpenEvent(InventoryOpenEvent ioe) {
        if (ioe.getPlayer() instanceof Player) {
            Player player = (Player) ioe.getPlayer();
            if (DataHandler.isPlayerActive(ConfigHandler.getDefaultBukkitConfig().getBoolean("use.UUIDs", true) ? player.getUniqueId().toString() : player.getName())) {
                if (!(ioe.getInventory().getHolder() instanceof PlayerInventory)) {
                    ioe.setCancelled(true);
                }
            }
        }
    }

    /**
     *
     * @param pde
     */
    @EventHandler
    public void onItemDrop(PlayerDropItemEvent pde) {
        if (DataHandler.isPlayerActive(ConfigHandler.getDefaultBukkitConfig().getBoolean("use.UUIDs", true) ? pde.getPlayer().getUniqueId().toString() : pde.getPlayer().getName())) {
            pde.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerPickupItem(PlayerPickupItemEvent ppie) {
        if (DataHandler.isPlayerActive(ConfigHandler.getDefaultBukkitConfig().getBoolean("use.UUIDs", true) ? ppie.getPlayer().getUniqueId().toString() : ppie.getPlayer().getName())) {
            ppie.setCancelled(true);
        }
    }

    /**
     *
     * @param bpe
     */
    @EventHandler
    public void onPhysicsEvent(final BlockPhysicsEvent bpe) {
        if (DataHandler.isBlueprintBlock(bpe.getBlock())) {
            DataHandler.updateBlock(bpe.getBlock());
            bpe.setCancelled(true);
        }
    }

//    @EventHandler
    /**
     *
     * @param pbee
     */
    @EventHandler
    public void onPlayerBucketEmpty(final PlayerBucketEmptyEvent pbee) {
        ScoreBoardSystem.updatePlayer(pbee.getPlayer());
        if (!pbee.isCancelled() && DataHandler.isPlayerActive(ConfigHandler.getDefaultBukkitConfig().getBoolean("use.UUIDs", true) ? pbee.getPlayer().getUniqueId().toString() : pbee.getPlayer().getName())) {
            if (ConfigHandler.getDefaultBukkitConfig().getBoolean("limits.blacklist")) {
                if (ConfigHandler.getBlockBlacklistConfig().getIntegerList("List Items").contains(pbee.getBucket().getId())) {
                    pbee.setCancelled(true);
                    return;
                }
            } else {
                if (ConfigHandler.getBlockBlacklistConfig().getIntegerList("List Items").contains(pbee.getBucket().getId())) {
                    pbee.setCancelled(true);
                    return;
                }
            }
            final BlockData block = new BlockData(pbee.getBlockClicked().getRelative(pbee.getBlockFace()));
            if (DataHandler.isBlueprintBlockAtLocation(pbee.getBlockClicked().getRelative(pbee.getBlockFace()).getLocation())) {
                DataHandler.updatePlayerBlock(ConfigHandler.getDefaultBukkitConfig().getBoolean("use.UUIDs", true) ? pbee.getPlayer().getUniqueId().toString() : pbee.getPlayer().getName(), block);
            } else {
                DataHandler.addPlayerBlock(ConfigHandler.getDefaultBukkitConfig().getBoolean("use.UUIDs", true) ? pbee.getPlayer().getUniqueId().toString() : pbee.getPlayer().getName(), new ItemStack(pbee.getBucket()), block);
            }
        }
    }

    @EventHandler
    public void onPlayerInteractEntityEvent(final PlayerInteractEntityEvent piee) {
        Player player = piee.getPlayer();
        if (DataHandler.isPlayerActive(ConfigHandler.getDefaultBukkitConfig().getBoolean("use.UUIDs", true) ? player.getUniqueId().toString() : player.getName())) {
            piee.setCancelled(true);
        }
    }

    /**
     *
     * @param pie
     */
    @EventHandler
    public void onPlayerInteract(final PlayerInteractEvent pie) {

        ScoreBoardSystem.updatePlayer(pie.getPlayer());
        final Player player = pie.getPlayer();
        if (DataHandler.isPlayerActive(ConfigHandler.getDefaultBukkitConfig().getBoolean("use.UUIDs", true) ? player.getUniqueId().toString() : player.getName())) {
            if (pie.getAction() == Action.RIGHT_CLICK_BLOCK) {
                if (pie.getItem() != null && !pie.getItem().getType().isBlock() && (pie.getItem().getType() == Material.BOW || pie.getItem().getType() == Material.MONSTER_EGG || pie.getItem().getType() == Material.MONSTER_EGGS || pie.getItem().getType() == Material.FLINT_AND_STEEL || pie.getItem().getType() == Material.ITEM_FRAME)) {
                    pie.setCancelled(true);
                }
                if (pie.getClickedBlock().getType() == Material.ITEM_FRAME || pie.getClickedBlock().getType() == Material.SIGN || pie.getClickedBlock().getType() == Material.SIGN_POST || pie.getClickedBlock().getType() == Material.WALL_SIGN) {
                    if (ConfigHandler.getDefaultBukkitConfig().getBoolean("limits.disable signs", true)) {
                        pie.setCancelled(true);
                        return;
                    }
                }
                if (DataHandler.checkPlayerBlockNoData(ConfigHandler.getDefaultBukkitConfig().getBoolean("use.UUIDs", true) ? player.getUniqueId().toString() : player.getName(), pie.getClickedBlock())) {
                    DataHandler.updatePlayerBlock(ConfigHandler.getDefaultBukkitConfig().getBoolean("use.UUIDs", true) ? player.getUniqueId().toString() : player.getName(), pie.getClickedBlock());
                    if (DataHandler.inMarkMode(player)) {
                        player.sendMessage("You can't mark a blueprint chest");
                    }
                } else if (pie.getClickedBlock().getType() == Material.CHEST) {
                    if (DataHandler.inMarkMode(player) || DataHandler.inUnmarkMode(player)) {
                        pie.setCancelled(true);
                    }
                DataHandler.markPlayer(DataHandler.getMarkMode(player), pie.getClickedBlock());
                DataHandler.unmarkPlayer(DataHandler.getUnmarkMode(player), pie.getClickedBlock());
                }
            } else if (pie.getAction() == Action.RIGHT_CLICK_AIR) {
                pie.setCancelled(true);
            }
        } else if (pie.getAction() == Action.RIGHT_CLICK_BLOCK || pie.getAction() == Action.LEFT_CLICK_BLOCK) {
            if (DataHandler.isBlueprintBlock(pie.getClickedBlock())) {
                player.sendMessage("Can't interact with a blueprint block");
                pie.setCancelled(true);
            }
            if (pie.getAction() == Action.RIGHT_CLICK_BLOCK && pie.getClickedBlock().getType() == Material.CHEST) {
                if (DataHandler.inMarkMode(player) || DataHandler.inUnmarkMode(player)) {
                    pie.setCancelled(true);
                }
                DataHandler.markPlayer(DataHandler.getMarkMode(player), pie.getClickedBlock());
                DataHandler.unmarkPlayer(DataHandler.getUnmarkMode(player), pie.getClickedBlock());
            }
        }
    }

    @EventHandler
    public void onPlayerQuitEvent(PlayerQuitEvent pqe) {
        if (ConfigHandler.getDefaultBukkitConfig().getBoolean("limits.disabale on logout", false)) {
            Player player = pqe.getPlayer();
            final String playerId = ConfigHandler.getDefaultBukkitConfig().getBoolean("use.UUIDs", true) ? player.getUniqueId().toString() : player.getPlayer().getName();
            if (DataHandler.isPlayerActive(playerId)) {
                PlayerInventory tempStore = DataHandler.deactivatePlayer(playerId);
                player.getInventory().setArmorContents(tempStore.getArmour());
                player.getInventory().setContents(tempStore.getItems());
                player.teleport(DataHandler.getPlayerLocation(playerId));
                BlockSetter.getBlocks().airAll(DataHandler.getBlueprint(playerId, player.getWorld().getName()));
                player.setGameMode(DataHandler.getOriginalPlayerGameMode(playerId));
                player.saveData();
            }
        } else if (ConfigHandler.getDefaultBukkitConfig().getBoolean("limits.logging destroy and build", true)) {
            Player player = pqe.getPlayer();
            final String playerId = ConfigHandler.getDefaultBukkitConfig().getBoolean("use.UUIDs", true) ? player.getUniqueId().toString() : player.getPlayer().getName();
            if (DataHandler.isPlayerActive(playerId)) {
                BlockSetter.getBlocks().airAll(DataHandler.getBlueprint(playerId, player.getWorld().getName()));
            }
        }
    }

    /**
     *
     * @param pje
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoinEvent(PlayerJoinEvent pje) {
        Player player = pje.getPlayer();
        final String playerId = ConfigHandler.getDefaultBukkitConfig().getBoolean("use.UUIDs", true) ? player.getUniqueId().toString() : player.getPlayer().getName();

        if (!DataHandler.playerExists(playerId)) {
            Yaml durpStore = new Yaml();
            String items = durpStore.dump(ItemSerial.serializeItemList(player.getInventory().getContents()));
            String armour = durpStore.dump(ItemSerial.serializeItemList(player.getInventory().getArmorContents()));

            DataHandler.setPlayer(playerId, player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), items.replaceAll("'", "''"), armour.replaceAll("'", "''"), 1, player.getGameMode(), player.getWorld().getName());
        }
        if (DataHandler.isPlayerActive(playerId)) {

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
                pje.getPlayer().sendMessage(ChatColor.RED + "There are blocks in your resource chest" + (playerChestLocations.size() > 1 ? "s. " : ". ") + "Blocks in your resource chest will still placed even when in blueprint mode and will have to be removed manualy");
            }
            BlockSetter.getBlocks().addAll(DataHandler.getBlueprint(playerId, pje.getPlayer().getWorld().getName()));
            pje.getPlayer().setGameMode(GameMode.CREATIVE);
            pje.getPlayer().sendMessage("You are still in blueprint mode");
        }
    }
}
