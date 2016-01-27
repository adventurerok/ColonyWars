package com.ithinkrok.cw.item;

import com.ithinkrok.minigames.base.event.ListenerLoadedEvent;
import com.ithinkrok.minigames.base.event.MinigamesEventHandler;
import com.ithinkrok.minigames.base.event.user.game.UserAbilityCooldownEvent;
import com.ithinkrok.minigames.base.event.user.world.UserInteractEvent;
import com.ithinkrok.minigames.base.item.CustomItem;
import com.ithinkrok.minigames.base.task.GameTask;
import org.bukkit.Effect;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by paul on 18/01/16.
 */
public class Cloak implements Listener {

    private Map<PotionEffectType, Integer> cloakEffects;
    private Map<PotionEffectType, Integer> decloakEffects;

    @MinigamesEventHandler
    public void onListenerLoaded(ListenerLoadedEvent event) {
        cloakEffects = getEffectsFromConfig(event.getConfig().getConfigurationSection("cloak_effects"));
        decloakEffects = getEffectsFromConfig(event.getConfig().getConfigurationSection("decloak_effects"));
    }

    private Map<PotionEffectType, Integer> getEffectsFromConfig(ConfigurationSection config) {
        Map<PotionEffectType, Integer> result = new HashMap<>();

        for(String key : config.getKeys(false)) {
            result.put(PotionEffectType.getByName(key.toUpperCase()), config.getInt(key) - 1);
        }

        return result;
    }

    @MinigamesEventHandler
    public void onInteract(UserInteractEvent event) {
        event.getUser().cloak();

        CustomItem item = event.getCustomItem();

        int durationTicks = (int) (item.calculateRightClickTimeout(event.getUser().getUpgradeLevels()) * 20);

        for(Map.Entry<PotionEffectType, Integer> e : cloakEffects.entrySet()){
            event.getUser().addPotionEffect(new PotionEffect(e.getKey(), durationTicks, e.getValue()), true);
        }

        GameTask cloakTask = event.getUser().repeatInFuture(task -> {
            if (!event.getUser().isCloaked()) task.finish();

            event.getUser().getLocation().getWorld().playEffect(event.getUser().getLocation(), Effect.POTION_SWIRL, 0);
        }, 20, 20);

        event.getUser().bindTaskToInGame(cloakTask);

        event.setStartCooldownAfterAction(true);
    }

    @MinigamesEventHandler
    public void onTimeout(UserAbilityCooldownEvent event) {
        if (!event.getUser().isInGame()) return;

        event.getUser().decloak();

        for(Map.Entry<PotionEffectType, Integer> e : decloakEffects.entrySet()){
            event.getUser().addPotionEffect(new PotionEffect(e.getKey(), Integer.MAX_VALUE, e.getValue()));
        }
    }

}
