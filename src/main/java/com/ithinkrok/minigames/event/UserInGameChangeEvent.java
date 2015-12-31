package com.ithinkrok.minigames.event;

import com.ithinkrok.minigames.User;

/**
 * Created by paul on 31/12/15.
 *
 * Called after a user changes their isInGame
 */
public class UserInGameChangeEvent<U extends User> extends UserEvent<U> {

    public UserInGameChangeEvent(U user) {
        super(user);
    }

    public boolean isInGame() {
        return getUser().isInGame();
    }
}
