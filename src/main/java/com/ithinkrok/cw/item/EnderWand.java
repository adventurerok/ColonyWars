package com.ithinkrok.cw.item;

import com.ithinkrok.minigames.event.ListenerLoadedEvent;
import com.ithinkrok.minigames.event.user.world.UserInteractEvent;
import com.ithinkrok.minigames.util.SoundEffect;
import com.ithinkrok.minigames.util.math.Calculator;
import com.ithinkrok.minigames.util.math.ExpressionCalculator;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EnderPearl;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.util.Vector;

/**
 * Created by paul on 19/01/16.
 */
public class EnderWand implements Listener {

    private Calculator speedMult;
    private Sound shootSound;

    @EventHandler
    public void onListenerLoaded(ListenerLoadedEvent event) {
        ConfigurationSection config = event.getConfig();

        speedMult = new ExpressionCalculator(config.getString("speed"));
        shootSound = Sound.valueOf(config.getString("fire_sound", "SHOOT_ARROW"));
    }

    @EventHandler
    public void onUserInteract(UserInteractEvent event) {
        EnderPearl pearl = event.getUser().launchProjectile(EnderPearl.class);

        Vector velocity = pearl.getVelocity();
        velocity.multiply(speedMult.calculate(event.getUser().getUpgradeLevels()));

        pearl.setVelocity(velocity);

        event.getUser().playSound(event.getUser().getLocation(), new SoundEffect(shootSound, 1.0f, 1.0f));

        event.setStartCooldownAfterAction(true);
    }
}
