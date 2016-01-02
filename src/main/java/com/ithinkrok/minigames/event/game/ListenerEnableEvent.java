package com.ithinkrok.minigames.event.game;

import com.ithinkrok.minigames.GameGroup;

/**
 * Created by paul on 02/01/16.
 *
 * Called on a listener when it is enabled (ready and going to start receiving events)
 */
public class ListenerEnableEvent<G extends GameGroup> extends GameEvent<G> {

    public ListenerEnableEvent(G gameGroup) {
        super(gameGroup);
    }
}
