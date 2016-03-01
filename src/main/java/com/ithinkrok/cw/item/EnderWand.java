package com.ithinkrok.cw.item;

import com.ithinkrok.minigames.api.event.ListenerLoadedEvent;
import com.ithinkrok.minigames.api.event.user.world.UserInteractEvent;
import com.ithinkrok.minigames.api.util.SoundEffect;
import com.ithinkrok.minigames.api.util.math.Calculator;
import com.ithinkrok.minigames.api.util.math.ExpressionCalculator;
import com.ithinkrok.util.config.Config;
import com.ithinkrok.util.event.CustomEventHandler;
import com.ithinkrok.util.event.CustomListener;
import org.bukkit.Sound;
import org.bukkit.entity.EnderPearl;
import org.bukkit.util.Vector;

/**
 * Created by paul on 19/01/16.
 */
public class EnderWand implements CustomListener {

    private Calculator speedMult;
    private Sound shootSound;

    @CustomEventHandler
    public void onListenerLoaded(ListenerLoadedEvent<?, ?> event) {
        Config config = event.getConfig();

        speedMult = new ExpressionCalculator(config.getString("speed"));
        shootSound = Sound.valueOf(config.getString("fire_sound", "ENTITY_ARROW_SHOOT"));
    }

    @CustomEventHandler
    public void onUserInteract(UserInteractEvent event) {
        EnderPearl pearl = event.getUser().launchProjectile(EnderPearl.class);

        Vector velocity = pearl.getVelocity();
        velocity.multiply(speedMult.calculate(event.getUser().getUpgradeLevels()));

        pearl.setVelocity(velocity);

        event.getUser().playSound(event.getUser().getLocation(), new SoundEffect(shootSound, 1.0f, 1.0f));

        event.setStartCooldownAfterAction(true);
    }
}
