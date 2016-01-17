package com.ithinkrok.cw.item;

import com.ithinkrok.minigames.event.user.world.UserInteractEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * Created by paul on 17/01/16.
 */
public class InvisibleUserToggle implements Listener {

    @EventHandler
    public void onUserInteract(UserInteractEvent event) {
        event.getUser().setShowCloakedUsers(!event.getUser().showCloakedUsers());

        event.setStartCooldownAfterAction(true);
    }
}
