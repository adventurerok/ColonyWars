package com.ithinkrok.minigames.user;

import com.ithinkrok.minigames.User;

import java.util.UUID;

/**
 * Created by paul on 03/01/16.
 */
public class AttackerTracker<U extends User> {

    private final UserResolver<U> userResolver;
    private final int defaultAttackerTimer = 600;

    private UUID attacker;
    private int attackerTimer;

    public AttackerTracker(UserResolver<U> userResolver) {
        this.userResolver = userResolver;
    }

    public void decreaseAttackerTimer(int ticks) {
        if(attackerTimer <= 0) return;

        attackerTimer -= ticks;
        if(attackerTimer > 0) return;

        attacker = null;
    }

    public void setAttacker(U attacker) {
        setAttacker(attacker, defaultAttackerTimer);
    }

    public void setAttacker(U attacker, int ticks) {
        if(attacker != null) {
            this.attacker = attacker.getUuid();
            this.attackerTimer = ticks;
        } else {
            this.attacker = null;
            this.attackerTimer = 0;
        }
    }

    public U getAttacker() {
        if(attacker == null) return null;
        return userResolver.getUser(attacker);
    }

}
