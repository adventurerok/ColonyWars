package com.ithinkrok.minigames.metadata;

import com.ithinkrok.minigames.event.game.GameStateChangedEvent;
import com.ithinkrok.minigames.event.game.MapChangedEvent;
import com.ithinkrok.minigames.event.user.game.UserInGameChangeEvent;

/**
 * Created by paul on 05/01/16.
 */
public abstract class Money extends UserMetadata {

    public abstract int getMoney();
    public abstract boolean hasMoney(int amount);
    public abstract void addMoney(int amount, boolean message);
    public abstract void subtractMoney(int amount, boolean message);

    @Override
    public boolean removeOnInGameChange(UserInGameChangeEvent event) {
        return !event.isInGame();
    }

    @Override
    public boolean removeOnGameStateChange(GameStateChangedEvent event) {
        return false;
    }

    @Override
    public boolean removeOnMapChange(MapChangedEvent event) {
        return false;
    }
}
