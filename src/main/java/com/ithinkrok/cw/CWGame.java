package com.ithinkrok.cw;

import com.ithinkrok.minigames.Game;
import org.bukkit.plugin.Plugin;

/**
 * Created by paul on 31/12/15.
 */
public class CWGame extends Game<CWUser, CWTeam, CWGameGroup, CWGame> {

    public CWGame(Plugin plugin) {
        super(plugin, CWGameGroup.class, CWTeam.class, CWUser.class);
    }

}
