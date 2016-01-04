package com.ithinkrok.minigames.metadata;

import com.ithinkrok.minigames.event.game.GameStateChangedEvent;
import com.ithinkrok.minigames.event.game.MapChangedEvent;

/**
 * Created by paul on 04/01/16.
 */
public abstract class Metadata {

    public abstract boolean removeOnGameStateChange(GameStateChangedEvent event);

    public abstract boolean removeOnMapChange(MapChangedEvent event);

    /**
     *
     * @return The class that is used as a key to store this metadata. The current class must be castable to it.
     */
    public Class<? extends Metadata> getMetadataClass() {
        return getClass();
    }
}
