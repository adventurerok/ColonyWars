package com.ithinkrok.minigames.event;

import com.ithinkrok.minigames.User;

/**
 * Created by paul on 31/12/15.
 */
public class UserEvent<U extends User>{

    private U user;

    public UserEvent(U user) {
        this.user = user;
    }

    public U getUser() {
        return user;
    }
}
