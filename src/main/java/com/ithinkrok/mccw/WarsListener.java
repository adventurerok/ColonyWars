package com.ithinkrok.mccw;

import com.ithinkrok.mccw.enumeration.TeamColor;
import com.ithinkrok.mccw.util.TreeFeller;
import org.bukkit.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExpEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Created by paul on 01/11/15.
 * <p>
 * Listens for Bukkit events
 */
public class WarsListener implements Listener{

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

        plugin.setPlayerTeam(event.getPlayer(), TeamColor.RED);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event){
        plugin.setPlayerInfo(event.getPlayer(), null);
    }

    @EventHandler
    public void onPickupItem(PlayerPickupItemEvent event){
        if(event.getPlayer().getGameMode() != GameMode.SURVIVAL){
            event.setCancelled(true);
            return;
        }


        switch(event.getItem().getItemStack().getType()){
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

    private void giveCashPerItem(PlayerPickupItemEvent event, int playerCash, int teamCash){
        PlayerInfo playerInfo = plugin.getPlayerInfo(event.getPlayer());

        playerInfo.addPlayerCash(playerCash * event.getItem().getItemStack().getAmount());
        event.getPlayer().playSound(event.getItem().getLocation(), Sound.ORB_PICKUP, 1.0f, 0.8f + (plugin
                .getRandom().nextFloat()) * 0.4f);

        TeamInfo teamInfo = plugin.getTeamData(playerInfo.getTeamColor());
        teamInfo.addTeamCash(teamCash);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event){
        if(event.getPlayer().getGameMode() != GameMode.SURVIVAL){
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockExp(BlockExpEvent event){
        switch(event.getBlock().getType()) {
            case GOLD_ORE:
                event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), new ItemStack(Material
                        .GOLD_INGOT, 3));
                break;
            case LOG:
                event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), new ItemStack(Material
                        .GOLD_INGOT, 1));
                TreeFeller.fellTree(event.getBlock().getLocation());
                break;
            case DIAMOND_ORE:
                event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), new ItemStack(Material
                        .DIAMOND, 1));
        }

        event.getBlock().setType(Material.AIR);
    }


    @EventHandler
    public void onDropItem(PlayerDropItemEvent event){
        event.setCancelled(true);
    }

    @EventHandler
    public void onLeavesDecay(LeavesDecayEvent event){
        event.getBlock().setType(Material.AIR);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event){
        if(event.getSlotType().equals(InventoryType.SlotType.ARMOR)) event.setCancelled(true);
    }


}
