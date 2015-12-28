package com.ithinkrok.mccw.event;

import com.ithinkrok.mccw.data.User;

/**
 * Created by paul on 28/12/15.
 */
public class UserJoinLobbyEvent extends UserEvent {

    public UserJoinLobbyEvent(User user) {
        super(user);
    }
}
