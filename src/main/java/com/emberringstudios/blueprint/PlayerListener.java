package com.emberringstudios.blueprint;

import java.util.List;
import java.util.concurrent.Callable;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
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
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.metadata.LazyMetadataValue;
import org.bukkit.metadata.MetadataValue;

/**
 *
 * @author Max9403 <Max9403@live.com>
 */
public class PlayerListener implements Listener {

    private final static List<Integer> ignoreList = ConfigHandler.getGreenlistConfig().getIntegerList("Greenlist Items");

//    @EventHandler
//    public void onExplosionEvent(ExplosionPrimeEvent epe) {
//        Location placedBlock = epe.getEntity().getLocation();
//        Vector test = epe.getEntity().getVelocity();
//        if (DataHandler.isBlueprintBlock(Material.TNT.getId(), (int) (placedBlock.getX() + test.getX()), (int) (placedBlock.getY() + test.getY()), (int) (placedBlock.getZ() + test.getZ()), placedBlock.getWorld().getName())) {
//            epe.setCancelled(true);
//        }
//    }    
    @EventHandler
    public void onPlayerBucketEmpty(final PlayerBucketEmptyEvent pbee) {
        if (!pbee.isCancelled() && DataHandler.isPlayerActive(ConfigHandler.getDefaultBukkitConfig().getBoolean("use.UUIDs", true) ? pbee.getPlayer().getUniqueId().toString() : pbee.getPlayer().getName())) {
            if (ConfigHandler.getDefaultBukkitConfig().getBoolean("limits.blacklist")) {
                if (ConfigHandler.getBlacklistConfig().getIntegerList("List Items").contains(pbee.getBucket().getId())) {
                    pbee.setCancelled(true);
                    return;
                }
            } else {
                if (!ConfigHandler.getBlacklistConfig().getIntegerList("List Items").contains(pbee.getBucket().getId())) {
                    pbee.setCancelled(true);
                    return;
                }
            }
            Bukkit.getScheduler().runTask(Blueprint.getPlugin(), new Runnable() {
                public void run() {
                    final BlockData block = new BlockData(pbee.getBlockClicked().getRelative(pbee.getBlockFace()));
                    if (DataHandler.isBlueprintBlockAtLocation(pbee.getBlockClicked().getRelative(pbee.getBlockFace()).getLocation())) {
                        DataHandler.updatePlayerBlock(ConfigHandler.getDefaultBukkitConfig().getBoolean("use.UUIDs", true) ? pbee.getPlayer().getUniqueId().toString() : pbee.getPlayer().getName(), block);
                    } else {
                        DataHandler.addPlayerBlock(ConfigHandler.getDefaultBukkitConfig().getBoolean("use.UUIDs", true) ? pbee.getPlayer().getUniqueId().toString() : pbee.getPlayer().getName(), pbee.getBucket().getId(), 0, block);
                    }
                }
            });
        }
    }

    @EventHandler
    public void onPhysicsEvent(final BlockPhysicsEvent bpe) {
        if (DataHandler.isBlueprintBlock(bpe.getBlock())) {
            if (bpe.getChangedTypeId() != 0) {
                DataHandler.updateBlock(bpe.getBlock());
            }
            bpe.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPistonEvent(BlockPistonExtendEvent bpee) {
        for (Block pushed : bpee.getBlocks()) {
            if (DataHandler.isBlueprintBlockAtLocation(pushed.getLocation()) && DataHandler.isPlayerActive(DataHandler.getBlockOwnerAtLocation(pushed.getLocation()))) {
                bpee.setCancelled(true);
                break;
            }
        }
    }

    @EventHandler
    public void onBlockFromTo(BlockFromToEvent bfte) {
        if (DataHandler.isBlueprintBlockAtLocation(bfte.getBlock().getLocation()) && DataHandler.isPlayerActive(DataHandler.getBlockOwnerAtLocation(bfte.getBlock().getLocation()))) {
            bfte.setCancelled(true);
        }
    }

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

    @EventHandler
    public void onEntityDamage(EntityDamageEvent edea) {
        if (edea instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent ede = (EntityDamageByEntityEvent) edea;
            if (ede.getDamager() instanceof Player) {
                Player player = (Player) ede.getDamager();
                {
                    if (DataHandler.isPlayerActive(ConfigHandler.getDefaultBukkitConfig().getBoolean("use.UUIDs", true) ? player.getUniqueId().toString() : player.getName())) {
                        ede.setCancelled(true);
                    }
                }
            }
        } else if (edea instanceof EntityDamageByBlockEvent) {
            EntityDamageByBlockEvent ede = (EntityDamageByBlockEvent) edea;
            if (!DataHandler.isBlueprintBlock(ede.getEntity().getLocation().getBlock())) {
                edea.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(final PlayerInteractEvent pie) {
        final Player player = pie.getPlayer();
        if (DataHandler.isPlayerActive(ConfigHandler.getDefaultBukkitConfig().getBoolean("use.UUIDs", true) ? player.getUniqueId().toString() : player.getName())) {
            if (pie.getAction() == Action.RIGHT_CLICK_BLOCK) {
                if (pie.getItem() != null && !pie.getItem().getType().isBlock() && (pie.getItem().getType() == Material.BOW || pie.getItem().getType() == Material.MONSTER_EGG || pie.getItem().getType() == Material.MONSTER_EGGS || pie.getItem().getType() == Material.FLINT_AND_STEEL)) {
                    pie.setCancelled(true);
                }
                if (DataHandler.checkPlayerBlockNoData(ConfigHandler.getDefaultBukkitConfig().getBoolean("use.UUIDs", true) ? player.getUniqueId().toString() : player.getName(), pie.getClickedBlock())) {
                    DataHandler.updatePlayerBlock(ConfigHandler.getDefaultBukkitConfig().getBoolean("use.UUIDs", true) ? player.getUniqueId().toString() : player.getName(), pie.getClickedBlock());
                    if (player.hasMetadata("inMarkMode")) {
                        for (MetadataValue meta : player.getMetadata("inMarkMode")) {
                            if (meta.getOwningPlugin() == Blueprint.getPlugin()) {
                                if (meta.asBoolean()) {
                                    player.sendMessage("You can't mark a blueprint chest");
                                    break;
                                }
                            }
                        }
                    }
                } else if (pie.getClickedBlock().getType() == Material.CHEST) {
                    if (player.hasMetadata("inMarkMode")) {
                        for (MetadataValue meta : player.getMetadata("inMarkMode")) {
                            if (meta.getOwningPlugin() == Blueprint.getPlugin()) {
                                if (meta.asBoolean()) {
                                    if (!DataHandler.isPlayerChest(pie.getClickedBlock())) {
                                        DataHandler.addPlayerChest(ConfigHandler.getDefaultBukkitConfig().getBoolean("use.UUIDs", true) ? player.getUniqueId().toString() : player.getName(), pie.getClickedBlock());
                                        player.setMetadata("inMarkMode", new LazyMetadataValue(Blueprint.getPlugin(), new Callable() {

                                            public Object call() throws Exception {
                                                return false;
                                            }
                                        }));
                                        player.sendMessage("Resource chest marked");
                                    } else {
                                        player.sendMessage("This chest is already marked");
                                    }
                                    break;
                                }
                            }
                        }
                    }
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
                if (player.hasMetadata("inMarkMode")) {
                    for (MetadataValue meta : player.getMetadata("inMarkMode")) {
                        if (meta.getOwningPlugin() == Blueprint.getPlugin()) {
                            if (meta.asBoolean()) {
                                if (!DataHandler.isPlayerChest(pie.getClickedBlock())) {
                                    DataHandler.addPlayerChest(ConfigHandler.getDefaultBukkitConfig().getBoolean("use.UUIDs", true) ? player.getUniqueId().toString() : player.getName(), pie.getClickedBlock());
                                    player.setMetadata("inMarkMode", new LazyMetadataValue(Blueprint.getPlugin(), new Callable() {

                                        public Object call() throws Exception {
                                            return false;
                                        }
                                    }));
                                    player.sendMessage("Resource chest marked");
                                } else {
                                    player.sendMessage("This chest is already marked");
                                }
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

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

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent pde) {
        if (DataHandler.isPlayerActive(ConfigHandler.getDefaultBukkitConfig().getBoolean("use.UUIDs", true) ? pde.getPlayer().getUniqueId().toString() : pde.getPlayer().getName())) {
            pde.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockBreak(final BlockBreakEvent bbe) {
        if (DataHandler.isPlayerActive(ConfigHandler.getDefaultBukkitConfig().getBoolean("use.UUIDs", true) ? bbe.getPlayer().getUniqueId().toString() : bbe.getPlayer().getName())) {
            if (DataHandler.checkPlayerBlock(ConfigHandler.getDefaultBukkitConfig().getBoolean("use.UUIDs", true) ? bbe.getPlayer().getUniqueId().toString() : bbe.getPlayer().getName(), bbe.getBlock())) {
                final BlockData block = new BlockData(bbe.getBlock());
                DataHandler.removePlayerBlock(ConfigHandler.getDefaultBukkitConfig().getBoolean("use.UUIDs", true) ? bbe.getPlayer().getUniqueId().toString() : bbe.getPlayer().getName(), block, bbe.getPlayer().getWorld().getName());
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
    }

    @EventHandler
    public void onBlockRedstoneChange(BlockRedstoneEvent bre) {
        if (DataHandler.isBlueprintBlock(bre.getBlock())) {
            bre.setNewCurrent(0);
        }
    }

    @EventHandler
    public void onBlockPlace(final BlockPlaceEvent bpe) {
        if (!bpe.isCancelled() && DataHandler.isPlayerActive(ConfigHandler.getDefaultBukkitConfig().getBoolean("use.UUIDs", true) ? bpe.getPlayer().getUniqueId().toString() : bpe.getPlayer().getName()) && bpe.canBuild()) {
            if (ConfigHandler.getDefaultBukkitConfig().getBoolean("limits.blacklist")) {
                if (ConfigHandler.getBlacklistConfig().getIntegerList("List Items").contains(bpe.getBlock().getTypeId()) || ConfigHandler.getBlacklistConfig().getIntegerList("List Items").contains(bpe.getItemInHand().getTypeId())) {
                    bpe.setCancelled(true);
                    return;
                }
            } else {
                if (!ConfigHandler.getBlacklistConfig().getIntegerList("List Items").contains(bpe.getBlock().getTypeId()) && !ConfigHandler.getBlacklistConfig().getIntegerList("List Items").contains(bpe.getItemInHand().getTypeId())) {
                    bpe.setCancelled(true);
                    return;
                }
            }
            DataHandler.addPlayerBlock(ConfigHandler.getDefaultBukkitConfig().getBoolean("use.UUIDs", true) ? bpe.getPlayer().getUniqueId().toString() : bpe.getPlayer().getName(), bpe.getItemInHand(), new BlockData(bpe.getBlock()));

            if (bpe.getBlockPlaced().getType() == Material.REDSTONE_TORCH_ON) {
                bpe.getBlockPlaced().setType(Material.REDSTONE_TORCH_OFF);
            } else if (bpe.getBlockPlaced().getType() == Material.TNT) {
                bpe.getPlayer().sendMessage("TNT added to blueprint");
                bpe.setCancelled(true);
            }
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
            //World tempWorld = bbe.getPlayer().getWorld();
            //tempWorld.spawnFallingBlock(localBlock.getLocation(), localBlock.getType(), localBlock.getData());
            //bbe.getPlayer().sendBlockChange(localBlock.getLocation(), Material.BEDROCK, (byte) 0);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoinEvent(PlayerJoinEvent pje) {
        if (DataHandler.isPlayerActive(ConfigHandler.getDefaultBukkitConfig().getBoolean("use.UUIDs", true) ? pje.getPlayer().getUniqueId().toString() : pje.getPlayer().getName())) {
            pje.getPlayer().setGameMode(GameMode.CREATIVE);
            pje.getPlayer().sendMessage("You are still in blueprint mode");
        }
    }
}
