package com.ithinkrok.mccw.event;

import com.ithinkrok.mccw.data.User;

/**
 * Created by paul on 28/12/15.
 */
public class UserQuitLobbyEvent extends UserEvent {

    public UserQuitLobbyEvent(User user) {
        super(user);
    }
}
