package com.ithinkrok.minigames.util.playerstate;

import org.apache.commons.lang.ArrayUtils;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

/**
 * Created by paul on 31/12/15.
 */
public class PlayerState {

    private ArmorCapture armorCapture;
    private InventoryCapture inventoryCapture;

    /**
     * The entity that is placeholding for the player. Can be null
     */
    private LivingEntity placeholder;

    public void capture(LivingEntity entity) {
        capture(entity, CaptureParts.ALL);
    }

    public void capture(LivingEntity entity, CaptureParts... captureParts) {
        if (captureParts[0] == CaptureParts.ALL) {
            capture(entity, CaptureParts.values());
            return;
        }

        if (ArrayUtils.contains(captureParts, CaptureParts.ARMOR)) {
            armorCapture = new ArmorCapture(entity.getEquipment().getArmorContents());
        }

        if (ArrayUtils.contains(captureParts, CaptureParts.INVENTORY) && (entity instanceof Player)) {
            inventoryCapture = new InventoryCapture(this, ((Player) entity).getInventory().getContents());
        }
    }

    public void restore(LivingEntity entity, CaptureParts... captureParts) {
        if (captureParts[0] == CaptureParts.ALL) {
            restore(entity, CaptureParts.values());
            return;
        }

        if (armorCapture != null && ArrayUtils.contains(captureParts, CaptureParts.ARMOR)) {
            entity.getEquipment().setArmorContents(armorCapture.getArmorContents());
        }

        if (inventoryCapture != null && ArrayUtils.contains(captureParts, CaptureParts.INVENTORY) &&
                (entity instanceof Player)) {
            ((Player) entity).getInventory().setContents(inventoryCapture.getContents());
        }
    }

    public ArmorCapture getArmorCapture() {
        return armorCapture;
    }

    public void restore(LivingEntity entity) {
        restore(entity, CaptureParts.ALL);
    }

    public LivingEntity getPlaceholder() {
        return placeholder;
    }

    public void setPlaceholder(LivingEntity placeholder) {
        this.placeholder = placeholder;
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
