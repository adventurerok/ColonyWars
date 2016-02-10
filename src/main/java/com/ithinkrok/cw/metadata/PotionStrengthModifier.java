package com.ithinkrok.cw.metadata;

import com.ithinkrok.minigames.base.User;
import com.ithinkrok.minigames.base.event.game.GameStateChangedEvent;
import com.ithinkrok.minigames.base.event.game.MapChangedEvent;
import com.ithinkrok.minigames.base.event.user.game.UserInGameChangeEvent;
import com.ithinkrok.minigames.base.metadata.UserMetadata;
import com.ithinkrok.minigames.base.task.GameTask;
import com.ithinkrok.util.config.Config;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;

/**
 * Created by paul on 17/01/16.
 */
public class PotionStrengthModifier extends UserMetadata {

    private double potionStrengthModifier = 1.0;

    private final double maxModifier;
    private final double minModifier;
    private final double lossOnPotion;
    private final double gainPerInterval;

    public PotionStrengthModifier(User user) {
        Config config = user.getSharedObjectOrEmpty("potion_strength_metadata");

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
