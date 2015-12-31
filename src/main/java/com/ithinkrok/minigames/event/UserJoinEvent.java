package com.ithinkrok.minigames.event;

import com.ithinkrok.minigames.User;

/**
 * Created by paul on 31/12/15.
 */
public class UserJoinEvent<U extends User> extends UserEvent<U> {

    public UserJoinEvent(U user) {
        super(user);
    }
}
