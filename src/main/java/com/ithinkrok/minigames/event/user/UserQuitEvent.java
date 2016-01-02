package com.ithinkrok.minigames.event.user;

import com.ithinkrok.minigames.User;

/**
 * Created by paul on 02/01/16.
 */
public class UserQuitEvent<U extends User> extends UserEvent<U> {

    private final QuitReason reason;
    private boolean removeUser = true;

    public UserQuitEvent(U user, QuitReason reason) {
        super(user);
        this.reason = reason;
    }

    public boolean getRemoveUser() {
        return removeUser;
    }

    public void setRemoveUser(boolean removeUser) {
        if(!removeUser && reason != QuitReason.QUIT_SERVER) {
            throw new UnsupportedOperationException("The user can only be not removed if they are quitting the server");
        }

        this.removeUser = removeUser;
    }

    public QuitReason getReason() {
        return reason;
    }

    public enum QuitReason {
        QUIT_SERVER,
        CHANGED_GAMEGROUP
    }
}
