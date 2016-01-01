package com.ithinkrok.cw;

import org.bukkit.plugin.java.JavaPlugin;

/**
 * Created by paul on 31/12/15.
 */
public class CWPlugin extends JavaPlugin {

    CWGame minigame;


    @Override
    public void onEnable() {
        super.onEnable();

        minigame = new CWGame(this);

        minigame.reloadConfig();
        minigame.registerListeners();
    }
}
