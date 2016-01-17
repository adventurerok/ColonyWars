package com.ithinkrok.minigames;

import com.ithinkrok.minigames.database.IntUserValue;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

/**
 * Created by paul on 17/01/16.
 */
public class MinigamesPlugin extends JavaPlugin {

    @Override
    public List<Class<?>> getDatabaseClasses() {
        List<Class<?>> result = super.getDatabaseClasses();

        result.add(IntUserValue.class);

        return result;
    }
}
