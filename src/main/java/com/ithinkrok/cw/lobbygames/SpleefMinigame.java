package com.ithinkrok.cw.lobbygames;

import com.ithinkrok.minigames.event.ConfiguredListener;
import org.bukkit.configuration.ConfigurationSection;

/**
 * Created by paul on 01/01/16.
 */
public class SpleefMinigame implements ConfiguredListener {
    @Override
    public void configure(ConfigurationSection config) {
        System.out.println("Hi Spleef");
    }
}
