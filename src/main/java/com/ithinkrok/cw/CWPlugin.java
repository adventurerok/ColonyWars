package com.ithinkrok.cw;

import com.ithinkrok.cw.database.UserCategoryStats;
import com.ithinkrok.minigames.Game;
import com.ithinkrok.minigames.MinigamesPlugin;

import java.util.List;

/**
 * Created by paul on 31/12/15.
 */
public class CWPlugin extends MinigamesPlugin {

    Game minigame;

    @Override
    public void onEnable() {
        super.onEnable();

        minigame = new Game(this);

        minigame.reloadConfig();
        minigame.registerListeners();
    }

    @Override
    public void onDisable() {
        minigame.unload();
    }

    @Override
    public List<Class<?>> getDatabaseClasses() {
        List<Class<?>> result = super.getDatabaseClasses();

        result.add(UserCategoryStats.class);

        return result;
    }
}
