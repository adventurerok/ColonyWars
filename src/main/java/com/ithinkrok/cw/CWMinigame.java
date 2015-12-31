package com.ithinkrok.cw;

import com.ithinkrok.minigames.Minigame;

/**
 * Created by paul on 31/12/15.
 */
public class CWMinigame extends Minigame<CWUser, CWTeam, CWGameGroup, CWMinigame> {

    public CWMinigame() {
        super(CWGameGroup.class, CWTeam.class, CWUser.class);
    }
}
