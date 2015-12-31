package com.ithinkrok.oldmccw.event;

import com.ithinkrok.oldmccw.data.User;

/**
 * Created by paul on 28/12/15.
 */
public class UserQuitLobbyEvent extends UserEvent {

    public UserQuitLobbyEvent(User user) {
        super(user);
    }
}
