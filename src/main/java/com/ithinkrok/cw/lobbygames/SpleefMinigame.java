package com.ithinkrok.cw.lobbygames;

import com.ithinkrok.minigames.User;
import com.ithinkrok.minigames.event.ConfiguredListener;
import com.ithinkrok.minigames.event.UserBreakBlockEvent;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

/**
 * Created by paul on 01/01/16.
 */
public class SpleefMinigame implements ConfiguredListener {
    @Override
    public void configure(ConfigurationSection config) {
        System.out.println("Hi Spleef");
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onUserBreakBlock(UserBreakBlockEvent<User> event){
        if(event.getBlock().getType() == Material.SNOW_BLOCK) event.setCancelled(false);
    }
}
