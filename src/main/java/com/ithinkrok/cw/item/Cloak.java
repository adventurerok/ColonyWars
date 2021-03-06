package com.ithinkrok.cw.item;

import com.ithinkrok.minigames.api.event.ListenerLoadedEvent;
import com.ithinkrok.minigames.api.event.user.game.UserAbilityCooldownEvent;
import com.ithinkrok.minigames.api.event.user.world.UserInteractEvent;
import com.ithinkrok.minigames.api.item.CustomItem;
import com.ithinkrok.minigames.api.task.GameTask;
import com.ithinkrok.util.config.Config;
import com.ithinkrok.util.event.CustomEventHandler;
import com.ithinkrok.util.event.CustomListener;
import org.bukkit.Effect;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by paul on 18/01/16.
 */
public class Cloak implements CustomListener {

    private Map<PotionEffectType, Integer> cloakEffects;
    private Map<PotionEffectType, Integer> decloakEffects;

    @CustomEventHandler
    public void onListenerLoaded(ListenerLoadedEvent<?, ?> event) {
        cloakEffects = getEffectsFromConfig(event.getConfig().getConfigOrNull("cloak_effects"));
        decloakEffects = getEffectsFromConfig(event.getConfig().getConfigOrNull("decloak_effects"));
    }

    private Map<PotionEffectType, Integer> getEffectsFromConfig(Config config) {
        Map<PotionEffectType, Integer> result = new HashMap<>();

        for(String key : config.getKeys(false)) {
            result.put(PotionEffectType.getByName(key.toUpperCase()), config.getInt(key) - 1);
        }

        return result;
    }

    @CustomEventHandler
    public void onInteract(UserInteractEvent event) {
        event.getUser().cloak();

        CustomItem item = event.getCustomItem();

        int durationTicks = (int) (item.calculateRightClickTimeout(event.getUser().getUserVariables()) * 20);

        for(Map.Entry<PotionEffectType, Integer> e : cloakEffects.entrySet()){
            event.getUser().addPotionEffect(new PotionEffect(e.getKey(), durationTicks, e.getValue()), true);
        }

        GameTask cloakTask = event.getUser().repeatInFuture(task -> {
            if (!event.getUser().isCloaked()) task.finish();

            event.getUser().getLocation().getWorld().playEffect(event.getUser().getLocation(), Effect.POTION_SWIRL, 0);
        }, 14, 14);

        event.getUser().bindTaskToInGame(cloakTask);

        event.setStartCooldownAfterAction(true);
    }

    @CustomEventHandler
    public void onTimeout(UserAbilityCooldownEvent event) {
        if (!event.getUser().isInGame()) return;

        event.getUser().decloak();

        for(Map.Entry<PotionEffectType, Integer> e : decloakEffects.entrySet()){
            event.getUser().addPotionEffect(new PotionEffect(e.getKey(), Integer.MAX_VALUE, e.getValue()));
        }
    }

}
