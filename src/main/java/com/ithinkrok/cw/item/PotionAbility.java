package com.ithinkrok.cw.item;

import com.ithinkrok.minigames.event.ListenerLoadedEvent;
import com.ithinkrok.minigames.event.user.world.UserInteractEvent;
import com.ithinkrok.minigames.util.math.Calculator;
import com.ithinkrok.minigames.util.math.ExpressionCalculator;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Created by paul on 16/01/16.
 */
public class PotionAbility implements Listener {

    private PotionEffectType potionEffectType;
    private Calculator durationCalculator;
    private Calculator levelCalculator;

    @EventHandler
    public void onListenerLoaded(ListenerLoadedEvent event) {
        ConfigurationSection config = event.getConfig();

        potionEffectType = PotionEffectType.getByName(config.getString("potion_effect"));
        durationCalculator = new ExpressionCalculator(config.getString("duration"));
        levelCalculator = new ExpressionCalculator(config.getString("level"));
    }


    @EventHandler
    public void onInteract(UserInteractEvent event) {
        int duration = (int) (durationCalculator.calculate(event.getUser().getUpgradeLevels()) * 20d);
        int amp = (int) levelCalculator.calculate(event.getUser().getUpgradeLevels());
        if(amp <= 0 || duration <= 0) return;

        PotionEffect effect = new PotionEffect(potionEffectType, duration, amp);
        event.getUser().addPotionEffect(effect);

        event.setStartCooldownAfterAction(true);
    }

}
