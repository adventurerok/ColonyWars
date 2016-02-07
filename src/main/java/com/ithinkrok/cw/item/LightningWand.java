package com.ithinkrok.cw.item;

import com.ithinkrok.minigames.base.event.ListenerLoadedEvent;
import com.ithinkrok.minigames.base.event.user.world.UserAttackEvent;
import com.ithinkrok.minigames.base.event.user.world.UserInteractEvent;
import com.ithinkrok.util.event.CustomEventHandler;
import com.ithinkrok.util.event.CustomListener;
import org.bukkit.block.Block;
import org.bukkit.entity.LightningStrike;
import org.bukkit.event.entity.EntityDamageEvent;

/**
 * Created by paul on 04/01/16.
 */
public class LightningWand implements CustomListener {

    private double lightingMultiplier;
    private int maxRange;

    @CustomEventHandler
    public void onListenerEnabled(ListenerLoadedEvent<?, ?> event) {
        maxRange = event.getConfig().getInt("max_range");
        lightingMultiplier = event.getConfig().getDouble("damage_multiplier");
    }

    @CustomEventHandler
    public void onUserAttack(UserAttackEvent event) {
        if(event.getInteractType() != UserInteractEvent.InteractType.REPRESENTING) return;

        if(event.getDamageCause() == EntityDamageEvent.DamageCause.LIGHTNING) {
            event.setDamage(event.getDamage() * lightingMultiplier);
        }
    }

    @CustomEventHandler
    public void onUserInteract(UserInteractEvent event) {
        if(event.getInteractType() != UserInteractEvent.InteractType.RIGHT_CLICK) return;
        Block target = event.getUser().rayTraceBlocks(maxRange);
        if(target == null) return;

        event.setStartCooldownAfterAction(true);

        LightningStrike strike = event.getUser().getLocation().getWorld().strikeLightning(target.getLocation());
        event.getUser().makeEntityRepresentUser(strike);
    }
}
