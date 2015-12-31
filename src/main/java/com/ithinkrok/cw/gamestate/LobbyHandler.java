package com.ithinkrok.cw.gamestate;

import com.ithinkrok.cw.CWUser;
import com.ithinkrok.minigames.GameStateHandler;
import com.ithinkrok.minigames.event.UserBreakBlockEvent;
import com.ithinkrok.minigames.event.UserPlaceBlockEvent;

/**
 * Created by paul on 31/12/15.
 */
public class LobbyHandler extends GameStateHandler<CWUser> {

    @Override
    public void eventBlockBreak(UserBreakBlockEvent<CWUser> event) {
        event.setCancelled(true);
    }

    @Override
    public void eventBlockPlace(UserPlaceBlockEvent<CWUser> event) {
        event.setCancelled(true);
    }
}
