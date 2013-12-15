package com.emberringstudios.blueprint;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.metadata.LazyMetadataValue;
import org.bukkit.metadata.MetadataValue;

/**
 *
 * @author Benjamin
 */
public class PlayerListener implements Listener {

    private final static List<Material> ignoreList = new CopyOnWriteArrayList();

    static {
        ignoreList.add(Material.DEAD_BUSH);
        ignoreList.add(Material.LONG_GRASS);
        ignoreList.add(Material.THIN_GLASS);
        ignoreList.add(Material.DOUBLE_PLANT);
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
    public void onEntityDamage(EntityDamageByEntityEvent ede) {
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
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent pie) {
        Player player = pie.getPlayer();
        if (DataHandler.isPlayerActive(ConfigHandler.getDefaultBukkitConfig().getBoolean("use.UUIDs", true) ? player.getUniqueId().toString() : player.getName())) {
            if (pie.getAction() == Action.RIGHT_CLICK_BLOCK) {
                if (pie.getItem() != null && (pie.getItem().getType() == Material.FLINT_AND_STEEL || pie.getItem().getType() == Material.BOW)) {
                    pie.setCancelled(true);
                }
                if (DataHandler.checkPlayerBlockNoData(ConfigHandler.getDefaultBukkitConfig().getBoolean("use.UUIDs", true) ? player.getUniqueId().toString() : player.getName(), pie.getClickedBlock(), player.getWorld().getName())) {
                    DataHandler.updatePlayerBlock(ConfigHandler.getDefaultBukkitConfig().getBoolean("use.UUIDs", true) ? player.getUniqueId().toString() : player.getName(), pie.getClickedBlock(), player.getWorld().getName());
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
                        DataHandler.addPlayerChest(ConfigHandler.getDefaultBukkitConfig().getBoolean("use.UUIDs", true) ? player.getUniqueId().toString() : player.getName(), pie.getClickedBlock(), player.getWorld().getName());
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
            if (DataHandler.checkPlayerBlock(ConfigHandler.getDefaultBukkitConfig().getBoolean("use.UUIDs", true) ? bbe.getPlayer().getUniqueId().toString() : bbe.getPlayer().getName(), bbe.getBlock(), bbe.getPlayer().getWorld().getName())) {
                DataHandler.removePlayerBlock(ConfigHandler.getDefaultBukkitConfig().getBoolean("use.UUIDs", true) ? bbe.getPlayer().getUniqueId().toString() : bbe.getPlayer().getName(), bbe.getBlock(), bbe.getPlayer().getWorld().getName());
            } else {
                if (!ignoreList.contains(bbe.getBlock().getType())) {
                    bbe.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent bpe) {
        if (DataHandler.isPlayerActive(ConfigHandler.getDefaultBukkitConfig().getBoolean("use.UUIDs", true) ? bpe.getPlayer().getUniqueId().toString() : bpe.getPlayer().getName()) && bpe.canBuild()) {
            DataHandler.addPlayerBlock(ConfigHandler.getDefaultBukkitConfig().getBoolean("use.UUIDs", true) ? bpe.getPlayer().getUniqueId().toString() : bpe.getPlayer().getName(), bpe.getBlockPlaced(), bpe.getPlayer().getWorld().getName());
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
