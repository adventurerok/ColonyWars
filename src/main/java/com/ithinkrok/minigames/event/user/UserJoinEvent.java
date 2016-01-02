package com.ithinkrok.minigames.event.user;

import com.ithinkrok.minigames.User;

/**
 * Created by paul on 31/12/15.
 */
public class UserJoinEvent<U extends User> extends UserEvent<U> {

    private final JoinReason reason;

    public UserJoinEvent(U user, JoinReason reason) {
        super(user);
        this.reason = reason;
    }

    public JoinReason getReason() {
        return reason;
    }

    public enum JoinReason {
        JOINED_SERVER,
        CHANGED_GAMESTATE,
        CHANGED_GAMEGROUP
    }
}
