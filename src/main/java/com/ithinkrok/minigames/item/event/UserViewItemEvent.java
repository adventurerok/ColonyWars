package com.ithinkrok.minigames.item.event;

import com.ithinkrok.minigames.User;
import com.ithinkrok.minigames.event.user.UserEvent;
import com.ithinkrok.minigames.item.ClickableItem;

/**
 * Created by paul on 02/01/16.
 */
public class UserViewItemEvent<U extends User> extends UserEvent<U> {

    private final ClickableItem<U> item;

    public UserViewItemEvent(U user, ClickableItem<U> item) {
        super(user);
        this.item = item;
    }

    public ClickableItem getItem() {
        return item;
    }
}
