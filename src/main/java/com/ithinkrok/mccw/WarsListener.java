package com.ithinkrok.mccw;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scoreboard.Scoreboard;

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

    public void onQuit(PlayerQuitEvent event){
        plugin.setPlayerInfo(event.getPlayer(), null);
    }

    @EventHandler
    public void onPickupItem(PlayerPickupItemEvent event){
        if(event.getItem().getItemStack().getType() == Material.GOLD_INGOT){
            PlayerInfo playerInfo = plugin.getPlayerInfo(event.getPlayer());

            playerInfo.addPlayerCash(100);
        }

        event.setCancelled(true);
        event.getItem().remove();
    }

    public void onDropItem(PlayerDropItemEvent event){
        event.setCancelled(true);
    }


}
