package com.ithinkrok.cw.item;

import com.ithinkrok.minigames.base.User;
import com.ithinkrok.minigames.base.event.ListenerLoadedEvent;
import com.ithinkrok.minigames.base.event.user.world.UserInteractEvent;
import com.ithinkrok.minigames.base.util.MinigamesConfigs;
import com.ithinkrok.minigames.base.util.SoundEffect;
import com.ithinkrok.minigames.base.util.math.Calculator;
import com.ithinkrok.minigames.base.util.math.ExpressionCalculator;
import com.ithinkrok.util.event.CustomEventHandler;
import com.ithinkrok.util.event.CustomListener;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Created by paul on 20/01/16.
 */
public class HealingScroll implements CustomListener {

    private PotionEffectType potionEffectType;
    private Calculator duration, level;
    private SoundEffect sound;

    @CustomEventHandler
    public void onListenerLoaded(ListenerLoadedEvent<?, ?> event) {
        ConfigurationSection config = event.getConfig();

        potionEffectType = PotionEffectType.getByName(config.getString("effect"));

        duration = new ExpressionCalculator(config.getString("duration"));
        level = new ExpressionCalculator(config.getString("level"));

        sound = MinigamesConfigs.getSoundEffect(config, "sound");

    }

    @CustomEventHandler
    public void onInteract(UserInteractEvent event) {
        int durationTicks = (int) (duration.calculate(event.getUser().getUpgradeLevels()) * 20);
        int amp = (int) (level.calculate(event.getUser().getUpgradeLevels()) - 1);

        if (amp < 0 || durationTicks < 1) return;

        PotionEffect effect = new PotionEffect(potionEffectType, durationTicks, amp);

        for (User user : event.getUser().getTeam().getUsers()) {
            user.addPotionEffect(effect, true);
            user.playSound(user.getLocation(), sound);
        }

        event.setStartCooldownAfterAction(true);
    }
}
