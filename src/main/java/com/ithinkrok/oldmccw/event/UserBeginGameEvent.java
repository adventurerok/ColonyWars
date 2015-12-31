package com.ithinkrok.oldmccw.event;

import com.ithinkrok.oldmccw.data.User;

/**
 * Created by paul on 18/11/15.
 *
 * An event for when a User begins a new game
 */
public class UserBeginGameEvent extends UserEvent {

    public UserBeginGameEvent(User user) {
        super(user);
    }

}
