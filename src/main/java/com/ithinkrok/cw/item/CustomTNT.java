package com.ithinkrok.cw.item;

import com.ithinkrok.minigames.event.ListenerLoadedEvent;
import com.ithinkrok.minigames.event.MinigamesEventHandler;
import com.ithinkrok.minigames.event.user.world.UserInteractEvent;
import com.ithinkrok.minigames.util.math.Calculator;
import com.ithinkrok.minigames.util.math.ExpressionCalculator;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.event.Listener;

/**
 * Created by paul on 21/01/16.
 */
public class CustomTNT implements Listener {

    private Calculator explosionPower;

    @MinigamesEventHandler
    public void onListenerLoaded(ListenerLoadedEvent event) {
        explosionPower = new ExpressionCalculator(event.getConfig().getString("explosion_power"));
    }

    @MinigamesEventHandler
    public void onInteract(UserInteractEvent event) {
        if (!event.hasBlock()) return;

        BlockFace mod = event.getBlockFace();

        float power = (float) explosionPower.calculate(event.getUser().getUpgradeLevels());

        Location loc = event.getClickedBlock().getLocation().clone()
                .add(mod.getModX() + 0.5, mod.getModY() + 0.5, mod.getModZ() + 0.5);

        event.getUser().createExplosion(loc, power, false, 80);

    }
}
