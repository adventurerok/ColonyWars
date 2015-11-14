package com.ithinkrok.mccw.event;

import com.ithinkrok.mccw.data.User;
import org.bukkit.event.Cancellable;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Created by paul on 14/11/15.
 *
 * Sent to the attacker's PlayerClassHandler when they attack someone by left clicking on them
 */
public class UserAttackUserEvent implements Cancellable{


    private User attacker;
    private User target;
    private EntityDamageByEntityEvent event;

    public UserAttackUserEvent(User attacker, User target, EntityDamageByEntityEvent event) {
        this.attacker = attacker;
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

    public User getAttacker() {
        return attacker;
    }

    public User getTarget() {
        return target;
    }

    public double getDamage(){
        return event.getDamage();
    }

    public void setDamage(double damage){
        event.setDamage(damage);
    }

    public ItemStack getWeapon(){
        return attacker.getPlayer().getItemInHand();
    }
}
