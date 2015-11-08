package com.ithinkrok.mccw;

import com.ithinkrok.mccw.data.PlayerInfo;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Created by paul on 08/11/15.
 *
 * Base listener for ColonyWars. Listens both during the game and in the lobby
 */
public class WarsBaseListener implements Listener {

    protected final WarsPlugin plugin;

    public WarsBaseListener(WarsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent event){
        PlayerInfo playerInfo = new PlayerInfo(plugin, event.getPlayer());
        plugin.setPlayerInfo(event.getPlayer(), playerInfo);

        playerInfo.message(ChatColor.GREEN + "Welcome to Colony Wars!");
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onQuit(PlayerQuitEvent event) {
        plugin.setPlayerInfo(event.getPlayer(), null);
    }

    @EventHandler
    public void onDropItem(PlayerDropItemEvent event) {
        event.setCancelled(true);
    }

}
