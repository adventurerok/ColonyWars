package com.ithinkrok.cw.item;

import com.ithinkrok.minigames.api.event.ListenerLoadedEvent;
import com.ithinkrok.minigames.api.event.user.world.UserInteractEvent;
import com.ithinkrok.util.math.Calculator;
import com.ithinkrok.util.math.ExpressionCalculator;
import com.ithinkrok.util.event.CustomEventHandler;
import com.ithinkrok.util.event.CustomListener;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.ItemStack;

/**
 * Created by paul on 21/01/16.
 */
public class CustomTNT implements CustomListener {

    private Calculator explosionPower;

    @CustomEventHandler
    public void onListenerLoaded(ListenerLoadedEvent<?, ?> event) {
        explosionPower = new ExpressionCalculator(event.getConfig().getString("explosion_power"));
    }

    @CustomEventHandler
    public void onInteract(UserInteractEvent event) {
        if (!event.hasBlock()) return;

        BlockFace mod = event.getBlockFace();

        float power = (float) explosionPower.calculate(event.getUser().getUpgradeLevels());

        Location loc = event.getClickedBlock().getLocation().clone()
                .add(mod.getModX() + 0.5, mod.getModY() + 0.5, mod.getModZ() + 0.5);

        event.getUser().createExplosion(loc, power, false, 80);

        //Cancel the event to make sure no block is placed (even though no block is placed anyway)
        event.setCancelled(true);

        //Decrement the count of the TNT by one
        //This is required as the spawned TNT makes the block place fail, which causes the item count to not be
        //decremented
        ItemStack oneLess = event.getItem().clone();
        if (oneLess.getAmount() > 1) oneLess.setAmount(oneLess.getAmount() - 1);
        else oneLess = null;
        event.getUser().getInventory().setItemInHand(oneLess);
    }
}
