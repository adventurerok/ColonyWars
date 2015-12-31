package com.ithinkrok.cw;

import com.ithinkrok.minigames.Minigame;
import org.bukkit.plugin.Plugin;

/**
 * Created by paul on 31/12/15.
 */
public class CWMinigame extends Minigame<CWUser, CWTeam, CWGameGroup, CWMinigame> {

    public CWMinigame(Plugin plugin) {
        super(plugin, CWGameGroup.class, CWTeam.class, CWUser.class);
    }
}
