package com.ithinkrok.mccw;

import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * Created by paul on 01/11/15.
 * <p>
 * Listens for Bukkit events
 */
public class WarsListener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        event.getPlayer().setGameMode(GameMode.SPECTATOR);
    }


}
