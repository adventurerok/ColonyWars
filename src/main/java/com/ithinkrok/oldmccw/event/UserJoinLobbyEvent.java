package com.ithinkrok.oldmccw.event;

import com.ithinkrok.oldmccw.data.User;

/**
 * Created by paul on 28/12/15.
 */
public class UserJoinLobbyEvent extends UserEvent {

    public UserJoinLobbyEvent(User user) {
        super(user);
    }
}
