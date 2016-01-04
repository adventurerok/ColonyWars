package com.ithinkrok.minigames.user;

import com.ithinkrok.minigames.GameGroup;
import com.ithinkrok.minigames.User;
import com.ithinkrok.minigames.event.game.GameStateChangedEvent;
import com.ithinkrok.minigames.event.game.MapChangedEvent;
import com.ithinkrok.minigames.event.user.game.UserInGameChangeEvent;

/**
 * Created by paul on 04/01/16.
 */
public abstract class UserMetadata {

    abstract boolean removeOnGameStateChange(GameStateChangedEvent<? extends GameGroup> event);
    abstract boolean removeOnMapChange(MapChangedEvent<? extends GameGroup> event);
    abstract boolean removeOnInGameChange(UserInGameChangeEvent<? extends User> event);

    /**
     *
     * @return The class that is used as a key to store this metadata. The current class must be castable to it.
     */
    public Class<? extends UserMetadata> getMetadataClass() {
        return getClass();
    }
}
