package com.ithinkrok.minigames.metadata;

import com.ithinkrok.minigames.User;
import com.ithinkrok.minigames.event.game.MapChangedEvent;
import com.ithinkrok.minigames.event.user.game.UserInGameChangeEvent;

/**
 * Created by paul on 05/01/16.
 */
public abstract class Money extends UserMetadata {

    public abstract int getMoney();
    public abstract boolean hasMoney(int amount);
    public abstract void addMoney(int amount, boolean message);
    public abstract boolean subtractMoney(int amount, boolean message);

    @Override
    public boolean removeOnInGameChange(UserInGameChangeEvent event) {
        return !event.isInGame();
    }

    @Override
    public boolean removeOnMapChange(MapChangedEvent event) {
        return false;
    }

    @Override
    public Class<? extends UserMetadata> getMetadataClass() {
        return Money.class;
    }

    public static Money getOrCreate(MetadataHolder<? super UserMetadata> holder) {
        if(holder.hasMetadata(Money.class)) return holder.getMetadata(Money.class);

        if(holder instanceof User) {
            return new UserMoney((User) holder);
        }

        throw new RuntimeException("Unsupported MetadataHolder type:" + holder);
    }
}
