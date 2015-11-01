package com.ithinkrok.mccw;

import com.ithinkrok.mccw.util.TreeFeller;
import org.bukkit.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.event.block.BlockExpEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.ArrayList;

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

        PlayerInfo playerInfo = new PlayerInfo(event.getPlayer());
        plugin.setPlayerInfo(event.getPlayer(), playerInfo);

        plugin.setupScoreboard(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event){
        plugin.setPlayerInfo(event.getPlayer(), null);
    }

    @EventHandler
    public void onPickupItem(PlayerPickupItemEvent event){
        if(event.getItem().getItemStack().getType() == Material.GOLD_INGOT){
            PlayerInfo playerInfo = plugin.getPlayerInfo(event.getPlayer());

            playerInfo.addPlayerCash(100 * event.getItem().getItemStack().getAmount());
            plugin.updateScoreboard(event.getPlayer());
            event.getPlayer().playSound(event.getItem().getLocation(), Sound.ORB_PICKUP, 1.0f, 0.8f + (plugin
                    .getRandom().nextFloat()) * 0.4f);
        }

        event.setCancelled(true);
        event.getItem().remove();
    }

    @EventHandler
    public void onBlockExp(BlockExpEvent event){
        switch(event.getBlock().getType()) {
            case GOLD_ORE:
                event.getBlock().setType(Material.AIR);
                event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), new ItemStack(Material
                        .GOLD_INGOT, 3));
                break;
            case LOG:
                event.getBlock().setType(Material.AIR);
                event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), new ItemStack(Material
                        .GOLD_INGOT, 1));
                TreeFeller.fellTree(event.getBlock().getLocation());
                break;
        }
    }


    @EventHandler
    public void onDropItem(PlayerDropItemEvent event){
        event.setCancelled(true);
    }


}
