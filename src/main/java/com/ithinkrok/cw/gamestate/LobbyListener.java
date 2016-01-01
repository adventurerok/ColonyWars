package com.ithinkrok.cw.gamestate;

import com.ithinkrok.cw.CWUser;
import com.ithinkrok.minigames.event.UserBreakBlockEvent;
import com.ithinkrok.minigames.event.UserJoinEvent;
import com.ithinkrok.minigames.event.UserPlaceBlockEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

/**
 * Created by paul on 31/12/15.
 */
public class LobbyListener implements Listener {

    @EventHandler
    public void eventBlockBreak(UserBreakBlockEvent<CWUser> event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void eventBlockPlace(UserPlaceBlockEvent<CWUser> event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void eventUserJoin(UserJoinEvent<CWUser> event) {
        System.out.println(event.getUser().getUuid() + " joined!");
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void monitorUserJoin(UserJoinEvent<CWUser> event) {
        System.out.println("Monitored!");
    }


}
