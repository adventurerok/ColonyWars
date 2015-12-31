package com.ithinkrok.cw;

import com.ithinkrok.minigames.GameGroup;

import java.lang.reflect.Constructor;

/**
 * Created by paul on 31/12/15.
 */
public class CWGameGroup extends GameGroup<CWUser, CWTeam, CWGameGroup, CWMinigame> {

    public CWGameGroup(CWMinigame minigame, Constructor<CWTeam> teamConstructor) {
        super(minigame, teamConstructor);
    }
}
