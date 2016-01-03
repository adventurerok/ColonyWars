package com.ithinkrok.minigames.event.user.state;

import com.ithinkrok.minigames.User;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Created by paul on 03/01/16.
 */
public class UserAttackedEvent<U extends User> extends UserDamagedEvent<U> {

    private final EntityDamageByEntityEvent event;
    private final U attacker;

    public UserAttackedEvent(U user, EntityDamageByEntityEvent event, U attacker) {
        super(user, event);
        this.event = event;
        this.attacker = attacker;
    }

    public Entity getAttacker() {
        return event.getDamager();
    }

    public boolean wasAttackedByUser() {
        return attacker != null;
    }

    public User getAttackerUser() {
        return attacker;
    }

    public ItemStack getWeapon() {
        if(attacker == null || getDamageCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK) return null;
        return attacker.getInventory().getItemInHand();
    }
}
