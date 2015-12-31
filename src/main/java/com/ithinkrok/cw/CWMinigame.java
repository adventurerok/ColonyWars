package com.ithinkrok.cw;

import com.ithinkrok.minigames.Minigame;

/**
 * Created by paul on 31/12/15.
 */
public class CWMinigame extends Minigame<CWUser, CWTeam, CWGameGroup, CWMinigame> {

    public CWMinigame(Class<CWGameGroup> gameGroupClass, Class<CWTeam> teamClass, Class<CWUser> userClass) {
        super(gameGroupClass, teamClass, userClass);
    }
}
