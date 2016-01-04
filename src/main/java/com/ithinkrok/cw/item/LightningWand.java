package com.ithinkrok.cw.item;

import com.ithinkrok.minigames.User;
import com.ithinkrok.minigames.event.user.world.UserAttackEvent;
import com.ithinkrok.minigames.event.user.world.UserInteractEvent;
import org.bukkit.block.Block;
import org.bukkit.entity.LightningStrike;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * Created by paul on 04/01/16.
 */
public class LightningWand implements Listener {

    @EventHandler
    public void onUserAttack(UserAttackEvent<? extends User> event) {

    }

    @EventHandler
    public void onUserInteract(UserInteractEvent<? extends User> event) {
        Block target = event.getUser().rayTraceBlocks(200);
        if(target == null) return;

        event.setStartCooldownAfterAction(true);

        LightningStrike strike = event.getUser().getLocation().getWorld().strikeLightning(target.getLocation());
        event.getUser().makeEntityRepresentUser(strike);
    }
}
