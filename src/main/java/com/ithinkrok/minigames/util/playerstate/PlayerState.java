package com.ithinkrok.minigames.util.playerstate;

import org.apache.commons.lang.ArrayUtils;
import org.bukkit.entity.LivingEntity;

/**
 * Created by paul on 31/12/15.
 */
public class PlayerState {

    private ArmorCapture armorCapture;

    public void capture(LivingEntity entity) {
        capture(entity, CaptureParts.ALL);
    }

    public void capture(LivingEntity entity, CaptureParts...captureParts){
        if(captureParts[0] == CaptureParts.ALL){
            capture(entity, CaptureParts.values());
            return;
        }

        if(ArrayUtils.contains(captureParts, CaptureParts.ARMOR)) {
            armorCapture = new ArmorCapture(entity.getEquipment().getArmorContents());
        }
    }

    public void restore(LivingEntity entity, CaptureParts...captureParts){
        if(captureParts[0] == CaptureParts.ALL){
            restore(entity, CaptureParts.values());
            return;
        }

        if(armorCapture != null && ArrayUtils.contains(captureParts, CaptureParts.ARMOR)) {
            entity.getEquipment().setArmorContents(armorCapture.getArmorContents());
        }
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
