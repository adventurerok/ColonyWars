package com.ithinkrok.mccw.event;

import com.ithinkrok.mccw.data.User;
import org.bukkit.entity.Entity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Created by paul on 14/11/15.
 *
 * Sent to the attacker's PlayerClassHandler when they attack an entity by left clicking on them
 */
public class UserAttackEvent extends UserEvent implements Cancellable{


    private User target;
    private EntityDamageByEntityEvent event;

    public UserAttackEvent(User attacker, User target, EntityDamageByEntityEvent event) {
        super(attacker);
        this.target = target;
        this.event = event;
    }

    @Override
    public boolean isCancelled() {
        return event.isCancelled();
    }

    @Override
    public void setCancelled(boolean b) {
        event.setCancelled(b);
    }


    public boolean isAttackingUser(){
        return target != null;
    }

    public Entity getTarget(){
        return event.getEntity();
    }

    public User getTargetUser() {
        return target;
    }

    public double getDamage(){
        return event.getDamage();
    }

    public void setDamage(double damage){
        event.setDamage(damage);
    }

    public ItemStack getWeapon(){
        return getUser().getPlayer().getItemInHand();
    }
}
