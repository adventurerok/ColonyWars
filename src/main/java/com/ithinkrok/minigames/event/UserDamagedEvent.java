package com.ithinkrok.minigames.event;

import com.ithinkrok.minigames.User;
import org.bukkit.event.Cancellable;
import org.bukkit.event.entity.EntityDamageEvent;

/**
 * Created by paul on 02/01/16.
 */
public class UserDamagedEvent<U extends User> extends UserEvent<U> implements Cancellable{

    private final EntityDamageEvent event;

    public UserDamagedEvent(U user, EntityDamageEvent event) {
        super(user);
        this.event = event;
    }

    @Override
    public boolean isCancelled() {
        return event.isCancelled();
    }

    @Override
    public void setCancelled(boolean cancel) {
        event.setCancelled(cancel);
    }

    public EntityDamageEvent.DamageCause getDamageCause() {
        return event.getCause();
    }

    public double getDamage() {
        return event.getDamage();
    }

    public double getFinalDamage() {
        return event.getFinalDamage();
    }
}
