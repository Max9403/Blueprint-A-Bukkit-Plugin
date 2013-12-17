package com.emberringstudios.blueprint;

import java.text.MessageFormat;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.metadata.LazyMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.util.Vector;

/**
 *
 * @author Max9403 <Max9403@live.com>
 */
public class PlayerListener implements Listener {
    
    private final static List<Material> ignoreList = new CopyOnWriteArrayList();
    
    static {
        ignoreList.add(Material.DEAD_BUSH);
        ignoreList.add(Material.LONG_GRASS);
        ignoreList.add(Material.THIN_GLASS);
        ignoreList.add(Material.DOUBLE_PLANT);
    }
    
//    @EventHandler
//    public void onExplosionEvent(ExplosionPrimeEvent epe) {
//        Location placedBlock = epe.getEntity().getLocation();
//        Vector test = epe.getEntity().getVelocity();
//        if (DataHandler.isBlueprintBlock(Material.TNT.getId(), (int) (placedBlock.getX() + test.getX()), (int) (placedBlock.getY() + test.getY()), (int) (placedBlock.getZ() + test.getZ()), placedBlock.getWorld().getName())) {
//            epe.setCancelled(true);
//        }
//    }
    
    @EventHandler
    public void onPhysicsEvent(BlockPhysicsEvent bpe) {
        if (DataHandler.isBlueprintBlock(bpe.getBlock())) {
            bpe.setCancelled(true);
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
                    } else if (player.hasMetadata("inMarkMode")) {
                        for (MetadataValue meta : player.getMetadata("inMarkMode")) {
                            if (meta.getOwningPlugin() == Blueprint.getPlugin()) {
                                if (meta.asBoolean()) {
                                    ede.setCancelled(true);
                                    break;
                                }
                            }
                        }
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
        Player player = pie.getPlayer();
        if (DataHandler.isPlayerActive(ConfigHandler.getDefaultBukkitConfig().getBoolean("use.UUIDs", true) ? player.getUniqueId().toString() : player.getName())) {
            if (pie.getAction() == Action.RIGHT_CLICK_BLOCK) {
                if (pie.getItem() != null && (pie.getItem().getType() == Material.FLINT_AND_STEEL || pie.getItem().getType() == Material.BOW)) {
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
                        DataHandler.addPlayerChest(ConfigHandler.getDefaultBukkitConfig().getBoolean("use.UUIDs", true) ? player.getUniqueId().toString() : player.getName(), pie.getClickedBlock());
                        for (MetadataValue meta : player.getMetadata("inMarkMode")) {
                            if (meta.getOwningPlugin() == Blueprint.getPlugin()) {
                                if (meta.asBoolean()) {
                                    player.setMetadata("inMarkMode", new LazyMetadataValue(Blueprint.getPlugin(), new Callable() {
                                        
                                        public Object call() throws Exception {
                                            return false;
                                        }
                                    }));
                                    player.sendMessage("Resource chest marked");
                                    break;
                                }
                            }
                        }
                    }
                }
            } else if (pie.getAction() == Action.RIGHT_CLICK_AIR) {
                if (pie.getItem() != null && (pie.getItem().getType() == Material.BOW)) {
                    pie.setCancelled(true);
                }
            }
        } else if (pie.getAction() == Action.RIGHT_CLICK_BLOCK || pie.getAction() == Action.LEFT_CLICK_BLOCK) {
            if (DataHandler.isBlueprintBlock(pie.getClickedBlock())) {
                player.sendMessage("Can't interact with a blueprint block");
                pie.setCancelled(true);
            }
            if (pie.getAction() == Action.RIGHT_CLICK_BLOCK && pie.getClickedBlock().getType() == Material.CHEST) {
                if (player.hasMetadata("inMarkMode")) {
                    DataHandler.addPlayerChest(ConfigHandler.getDefaultBukkitConfig().getBoolean("use.UUIDs", true) ? player.getUniqueId().toString() : player.getName(), pie.getClickedBlock());
                    for (MetadataValue meta : player.getMetadata("inMarkMode")) {
                        if (meta.getOwningPlugin() == Blueprint.getPlugin()) {
                            if (meta.asBoolean()) {
                                player.setMetadata("inMarkMode", new LazyMetadataValue(Blueprint.getPlugin(), new Callable() {
                                    
                                    public Object call() throws Exception {
                                        pie.setCancelled(true);
                                        return false;
                                    }
                                }));
                                player.sendMessage("Resource chest marked");
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
    public void onBlockBreak(BlockBreakEvent bbe) {
        if (DataHandler.isPlayerActive(ConfigHandler.getDefaultBukkitConfig().getBoolean("use.UUIDs", true) ? bbe.getPlayer().getUniqueId().toString() : bbe.getPlayer().getName())) {
            if (DataHandler.checkPlayerBlock(ConfigHandler.getDefaultBukkitConfig().getBoolean("use.UUIDs", true) ? bbe.getPlayer().getUniqueId().toString() : bbe.getPlayer().getName(), bbe.getBlock())) {
                DataHandler.removePlayerBlock(ConfigHandler.getDefaultBukkitConfig().getBoolean("use.UUIDs", true) ? bbe.getPlayer().getUniqueId().toString() : bbe.getPlayer().getName(), bbe.getBlock(), bbe.getPlayer().getWorld().getName());
            } else {
                if (!ignoreList.contains(bbe.getBlock().getType())) {
                    bbe.setCancelled(true);
                }
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
    public void onBlockPlace(BlockPlaceEvent bpe) {
        if (!bpe.isCancelled() && DataHandler.isPlayerActive(ConfigHandler.getDefaultBukkitConfig().getBoolean("use.UUIDs", true) ? bpe.getPlayer().getUniqueId().toString() : bpe.getPlayer().getName()) && bpe.canBuild()) {
            
            DataHandler.addPlayerBlock(ConfigHandler.getDefaultBukkitConfig().getBoolean("use.UUIDs", true) ? bpe.getPlayer().getUniqueId().toString() : bpe.getPlayer().getName(), bpe.getBlockPlaced());
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
}
