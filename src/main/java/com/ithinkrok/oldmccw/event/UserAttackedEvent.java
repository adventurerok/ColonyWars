package com.ithinkrok.oldmccw.event;

import com.ithinkrok.oldmccw.data.User;
import org.bukkit.event.Cancellable;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Created by paul on 13/12/15.
 *
 * Sent to the target's PlayerClassHandler when they are attacked by an entity
 */
public class UserAttackedEvent extends UserEvent implements Cancellable{

    private EntityDamageByEntityEvent event;
    private User attacker;

    public UserAttackedEvent(User target, User attacker, EntityDamageByEntityEvent event) {
        super(target);
        this.event = event;
        this.attacker = attacker;
    }

    @Override
    public boolean isCancelled() {
        return event.isCancelled();
    }

    @Override
    public void setCancelled(boolean cancel) {
        event.setCancelled(cancel);
    }

    public double getDamage(){
        return event.getDamage();
    }

    public double getFinalDamage() {
        return event.getFinalDamage();
    }

    public void setDamage(double damage){
        event.setDamage(damage);
    }

    public EntityDamageEvent.DamageCause getDamageCause() {
        return event.getCause();
    }

    public boolean wasAttackedByUser() {
        return attacker != null;
    }

    public User getAttackerUser() {
        return attacker;
    }

    public ItemStack getWeapon() {
        if(attacker == null || getDamageCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK) return null;
        return attacker.getPlayerInventory().getItemInHand();
    }
}
