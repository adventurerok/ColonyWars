package com.ithinkrok.cw.item;

import com.ithinkrok.minigames.api.event.ListenerLoadedEvent;
import com.ithinkrok.minigames.api.event.user.world.UserInteractEvent;
import com.ithinkrok.minigames.base.util.math.Calculator;
import com.ithinkrok.minigames.base.util.math.ExpressionCalculator;
import com.ithinkrok.util.config.Config;
import com.ithinkrok.util.event.CustomEventHandler;
import com.ithinkrok.util.event.CustomListener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Created by paul on 16/01/16.
 */
public class PotionAbility implements CustomListener {

    private PotionEffectType potionEffectType;
    private Calculator durationCalculator;
    private Calculator levelCalculator;

    @CustomEventHandler
    public void onListenerLoaded(ListenerLoadedEvent<?, ?> event) {
        Config config = event.getConfig();

        potionEffectType = PotionEffectType.getByName(config.getString("potion_effect"));
        durationCalculator = new ExpressionCalculator(config.getString("duration"));
        levelCalculator = new ExpressionCalculator(config.getString("level"));
    }


    @CustomEventHandler
    public void onInteract(UserInteractEvent event) {
        int duration = (int) (durationCalculator.calculate(event.getUser().getUpgradeLevels()) * 20d);
        int amp = (int) levelCalculator.calculate(event.getUser().getUpgradeLevels());
        if(amp <= 0 || duration <= 0) return;

        PotionEffect effect = new PotionEffect(potionEffectType, duration, amp);
        event.getUser().addPotionEffect(effect);

        event.setStartCooldownAfterAction(true);
    }

}
