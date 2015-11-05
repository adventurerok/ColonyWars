package com.ithinkrok.mccw;

import com.ithinkrok.mccw.data.BuildingInfo;
import com.ithinkrok.mccw.data.PlayerInfo;
import com.ithinkrok.mccw.data.SchematicData;
import com.ithinkrok.mccw.data.TeamInfo;
import com.ithinkrok.mccw.enumeration.PlayerClass;
import com.ithinkrok.mccw.enumeration.TeamColor;
import com.ithinkrok.mccw.inventory.InventoryHandler;
import com.ithinkrok.mccw.playerclass.PlayerClassHandler;
import com.ithinkrok.mccw.util.SchematicBuilder;
import com.ithinkrok.mccw.util.TreeFeller;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by paul on 01/11/15.
 * <p>
 * Listens for Bukkit events
 */
public class WarsListener implements Listener {

    private WarsPlugin plugin;

    public WarsListener(WarsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        event.getPlayer().setGameMode(GameMode.SPECTATOR);

        PlayerInfo playerInfo = new PlayerInfo(plugin, event.getPlayer());
        plugin.setPlayerInfo(event.getPlayer(), playerInfo);

        playerInfo.setupScoreboard();

        event.getPlayer().setMaxHealth(plugin.getMaxHealth());
        event.getPlayer().setHealth(plugin.getMaxHealth());

        //Just for testing
        plugin.setPlayerTeam(event.getPlayer(), TeamColor.RED);
        playerInfo.setPlayerClass(PlayerClass.GENERAL);
        plugin.setupPlayers();
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.setPlayerInfo(event.getPlayer(), null);
    }

    @EventHandler
    public void onPickupItem(PlayerPickupItemEvent event) {
        if (event.getPlayer().getGameMode() != GameMode.SURVIVAL) {
            event.setCancelled(true);
            return;
        }


        switch (event.getItem().getItemStack().getType()) {
            case GOLD_INGOT:
                giveCashPerItem(event, 120, 80);
                break;
            case DIAMOND:
                giveCashPerItem(event, 1200, 800);
                break;
        }

        event.setCancelled(true);
        event.getItem().remove();
    }

    private void giveCashPerItem(PlayerPickupItemEvent event, int playerCash, int teamCash) {
        PlayerInfo playerInfo = plugin.getPlayerInfo(event.getPlayer());

        playerInfo.addPlayerCash(playerCash * event.getItem().getItemStack().getAmount());
        event.getPlayer().playSound(event.getItem().getLocation(), Sound.ORB_PICKUP, 1.0f,
                0.8f + (plugin.getRandom().nextFloat()) * 0.4f);

        TeamInfo teamInfo = plugin.getTeamInfo(playerInfo.getTeamColor());
        teamInfo.addTeamCash(teamCash * event.getItem().getItemStack().getAmount());
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.getBlock().getType() != Material.LAPIS_ORE) return;

        ItemMeta meta = event.getItemInHand().getItemMeta();
        if (!meta.hasDisplayName()) return;

        SchematicData schematicData = plugin.getSchematicData(meta.getDisplayName());
        if (schematicData == null) return;


        PlayerInfo playerInfo = plugin.getPlayerInfo(event.getPlayer());

        if (!SchematicBuilder
                .buildSchematic(plugin, schematicData, event.getBlock().getLocation(), playerInfo.getTeamColor())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("You cannot build that here!");
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.getPlayer().getGameMode() != GameMode.SURVIVAL) {
            event.setCancelled(true);
            return;
        }

        resetDurability(event.getPlayer().getItemInHand());

        if (event.getBlock().getType() != Material.OBSIDIAN) return;

        BuildingInfo buildingInfo = plugin.getBuildingInfo(event.getBlock().getLocation());
        if (buildingInfo == null) {
            plugin.getLogger().warning("The player destroyed an obsidian block, but it wasn't a building. Odd");
            plugin.getLogger().warning("Obsidian location: " + event.getBlock().getLocation());
            event.getPlayer().sendMessage("That obsidian block doesn't appear to be part of a building");
            return;
        }

        PlayerInfo playerInfo = plugin.getPlayerInfo(event.getPlayer());

        if (playerInfo.getTeamColor() == buildingInfo.getTeamColor()) {
            event.getPlayer().sendMessage("You cannot destroy your own team's buildings!");
            event.setCancelled(true);
            return;
        }

        buildingInfo.explode();
    }

    private void resetDurability(ItemStack item) {
        if(item == null) return;
        if (item.getDurability() != 0 && item.getType().getMaxDurability() != 0) {
            item.setDurability((short) 0);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event){
        event.setDroppedExp(0);
        event.getDrops().clear();
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockExp(BlockExpEvent event) {
        switch (event.getBlock().getType()) {
            case GOLD_ORE:
                event.getBlock().getWorld()
                        .dropItemNaturally(event.getBlock().getLocation(), new ItemStack(Material.GOLD_INGOT, 3));
                break;
            case LOG:
            case LOG_2:
                event.getBlock().getWorld()
                        .dropItemNaturally(event.getBlock().getLocation(), new ItemStack(Material.GOLD_INGOT, 1));
                TreeFeller.fellTree(event.getBlock().getLocation());
                break;
            case DIAMOND_ORE:
                event.getBlock().getWorld()
                        .dropItemNaturally(event.getBlock().getLocation(), new ItemStack(Material.DIAMOND, 1));
        }

        event.getBlock().setType(Material.AIR);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        resetDurability(event.getPlayer().getItemInHand());

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getClickedBlock().getType() != Material.OBSIDIAN) {
            return;
        }

        BuildingInfo buildingInfo = plugin.getBuildingInfo(event.getClickedBlock().getLocation());
        if (buildingInfo == null) {
            plugin.getLogger().warning("The player destroyed an obsidian block, but it wasn't a building. Odd");
            plugin.getLogger().warning("Obsidian location: " + event.getClickedBlock().getLocation());
            event.getPlayer().sendMessage("That obsidian block doesn't appear to be part of a building.");
            return;
        }

        PlayerInfo playerInfo = plugin.getPlayerInfo(event.getPlayer());

        if (playerInfo.getTeamColor() != buildingInfo.getTeamColor()) {
            event.getPlayer().sendMessage("That building does not belong to your team. Mine this block to destroy it!");
            return;
        }

        if (!buildingInfo.isFinished()) {
            event.getPlayer().sendMessage("You must wait until the building has finished construction.");
            return;
        }

        playerInfo.setShopBlock(buildingInfo.getCenterBlock());

        Inventory shopInv = Bukkit.createInventory(event.getPlayer(), 9, buildingInfo.getBuildingName());

        TeamInfo teamInfo = plugin.getTeamInfo(playerInfo.getTeamColor());

        InventoryHandler inventoryHandler = plugin.getInventoryHandler(buildingInfo.getBuildingName());
        List<ItemStack> contents;

        if(inventoryHandler != null) contents = inventoryHandler.getInventoryContents(playerInfo, teamInfo);
        else contents = new ArrayList<>();

        PlayerClassHandler classHandler = plugin.getPlayerClassHandler(playerInfo.getPlayerClass());
        classHandler.addExtraInventoryItems(contents, buildingInfo.getBuildingName(), playerInfo, teamInfo);

        int index = 0;

        for (ItemStack item : contents) {
            shopInv.setItem(index++, item);
        }

        event.getPlayer().openInventory(shopInv);
        playerInfo.setShopInventory(shopInv);
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            resetDurability(((Player) event.getDamager()).getItemInHand());
        }
    }

    @EventHandler
    public void onDropItem(PlayerDropItemEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onLeavesDecay(LeavesDecayEvent event) {
        event.getBlock().setType(Material.AIR);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getSlotType().equals(InventoryType.SlotType.ARMOR)) event.setCancelled(true);

        if (event.getInventory().getType() != InventoryType.PLAYER &&
                event.getSlotType() == InventoryType.SlotType.CONTAINER) {

            event.setCancelled(true);

            if(event.getCurrentItem() == null) return;
            InventoryHandler handler = plugin.getInventoryHandler(event.getInventory().getTitle());
            if (handler == null) return;

            PlayerInfo playerInfo = plugin.getPlayerInfo((Player) event.getWhoClicked());
            playerInfo.setShopInventory(event.getInventory());

            TeamInfo teamInfo = plugin.getTeamInfo(playerInfo.getTeamColor());

            PlayerClassHandler classHandler = plugin.getPlayerClassHandler(playerInfo.getPlayerClass());

            boolean done = classHandler.onInventoryClick(event.getCurrentItem(), event.getInventory().getTitle(),
                    playerInfo, teamInfo);

            if(done) return;

            handler.onInventoryClick(event.getCurrentItem(), playerInfo, teamInfo);
        }
    }


}
