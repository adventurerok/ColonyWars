package com.ithinkrok.oldmccw.event;

import com.ithinkrok.oldmccw.data.User;

/**
 * Created by paul on 21/11/15.
 *
 * An event for when a timed ability times out
 */
public class UserAbilityCooldownEvent extends UserEvent {

    private String ability;

    public UserAbilityCooldownEvent(User user, String ability) {
        super(user);
        this.ability = ability;
    }

    public String getAbility() {
        return ability;
    }
}
