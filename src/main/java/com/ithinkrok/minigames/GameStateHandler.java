package com.ithinkrok.minigames;

import com.ithinkrok.minigames.event.UserBreakBlockEvent;
import com.ithinkrok.minigames.event.UserInGameChangeEvent;
import com.ithinkrok.minigames.event.UserJoinEvent;
import com.ithinkrok.minigames.event.UserPlaceBlockEvent;

/**
 * Created by paul on 31/12/15.
 */
public abstract class GameStateHandler<U extends User> {

    public void eventBlockBreak(UserBreakBlockEvent<U> event) {
    }

    public void eventBlockPlace(UserPlaceBlockEvent<U> event) {

    }

    public void eventUserJoin(UserJoinEvent<U> event) {

    }

    public void eventUserInGameChanged(UserInGameChangeEvent<U> event) {

    }
}
