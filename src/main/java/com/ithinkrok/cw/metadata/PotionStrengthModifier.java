package com.ithinkrok.cw.metadata;

import com.ithinkrok.minigames.User;
import com.ithinkrok.minigames.event.game.GameStateChangedEvent;
import com.ithinkrok.minigames.event.game.MapChangedEvent;
import com.ithinkrok.minigames.event.user.game.UserInGameChangeEvent;
import com.ithinkrok.minigames.metadata.UserMetadata;
import com.ithinkrok.minigames.task.GameTask;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;

/**
 * Created by paul on 17/01/16.
 */
public class PotionStrengthModifier extends UserMetadata {

    private double potionStrengthModifier = 1.0;

    private double maxModifier;
    private double minModifier;
    private double lossOnPotion;
    private double gainPerInterval;

    public PotionStrengthModifier(User user) {
        ConfigurationSection config = user.getSharedObject("potion_strength_metadata");
        if (config == null) config = new MemoryConfiguration();

        maxModifier = config.getDouble("max_modifier", 1.0);
        minModifier = config.getDouble("min_modifier", 0.5);
        lossOnPotion = config.getDouble("loss_on_potion", 0.05);

        int updateInterval = (int) (config.getDouble("update_interval", 0.5) * 20);
        gainPerInterval = config.getDouble("gain_per_second", 0.10) * (updateInterval / 20d);

        GameTask task = user.repeatInFuture(
                t -> potionStrengthModifier = Math.min(potionStrengthModifier + gainPerInterval, maxModifier),
                updateInterval, updateInterval);
        bindTaskToMetadata(task);
    }

    public static PotionStrengthModifier getOrCreate(User user) {
        PotionStrengthModifier modifier = user.getMetadata(PotionStrengthModifier.class);

        if(modifier == null) {
            modifier = new PotionStrengthModifier(user);
            user.setMetadata(modifier);
        }

        return modifier;
    }

    public void onPotionUsed() {
        potionStrengthModifier = Math.max(potionStrengthModifier - lossOnPotion, minModifier);
    }

    public double getPotionStrengthModifier() {
        return potionStrengthModifier;
    }

    @Override
    public boolean removeOnInGameChange(UserInGameChangeEvent event) {
        return true;
    }

    @Override
    public boolean removeOnGameStateChange(GameStateChangedEvent event) {
        return false;
    }

    @Override
    public boolean removeOnMapChange(MapChangedEvent event) {
        return true;
    }
}
