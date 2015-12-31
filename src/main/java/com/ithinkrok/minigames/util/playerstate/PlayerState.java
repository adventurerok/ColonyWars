package com.ithinkrok.minigames.util.playerstate;

import org.bukkit.entity.LivingEntity;

/**
 * Created by paul on 31/12/15.
 */
public class PlayerState {

    public void capture(LivingEntity entity) {
        capture(entity, CaptureParts.ALL);
    }

    public void capture(LivingEntity entity, CaptureParts...captureParts){

    }

    public void restore(LivingEntity entity, CaptureParts...captureParts){

    }

    public void restore(LivingEntity entity) {
        restore(entity, CaptureParts.ALL);
    }

    public enum CaptureParts {
        INVENTORY,
        ARMOR,
        NAME,
        EFFECTS,
        ALL
    }

    //private static class
}
