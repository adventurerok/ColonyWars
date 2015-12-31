package com.ithinkrok.minigames.listeners;

import com.ithinkrok.minigames.GameGroup;
import com.ithinkrok.minigames.Minigame;
import com.ithinkrok.minigames.Team;
import com.ithinkrok.minigames.User;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * Created by paul on 31/12/15.
 */
public class MinigameListener<U extends User, T extends Team, G extends GameGroup>
        implements Listener {

    private Minigame minigame;

    public MinigameListener(Minigame minigame) {
        this.minigame = minigame;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {

    }

}
