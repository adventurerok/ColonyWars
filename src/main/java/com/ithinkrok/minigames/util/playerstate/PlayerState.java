package com.ithinkrok.minigames.util.playerstate;

import org.apache.commons.lang.ArrayUtils;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

/**
 * Created by paul on 31/12/15.
 */
public class PlayerState {

    private ArmorCapture armorCapture;
    private InventoryCapture inventoryCapture;
    private EffectsCapture effectsCapture;
    private ModeCapture modeCapture;
    private HealthCapture healthCapture;
    private FoodCapture foodCapture;

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

        if (ArrayUtils.contains(captureParts, CaptureParts.EFFECTS)) {
            effectsCapture = new EffectsCapture(entity.getActivePotionEffects(), entity.getFireTicks());
        }

        if (ArrayUtils.contains(captureParts, CaptureParts.MODE) && (entity instanceof Player)) {
            modeCapture = new ModeCapture((Player) entity);
        }

        if (ArrayUtils.contains(captureParts, CaptureParts.HEALTH)) {
            healthCapture = new HealthCapture(entity);
        }

        if (ArrayUtils.contains(captureParts, CaptureParts.FOOD) && (entity instanceof Player)) {
            foodCapture = new FoodCapture((Player) entity);
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

        if (effectsCapture != null && ArrayUtils.contains(captureParts, CaptureParts.EFFECTS)) {
            for (PotionEffect effect : effectsCapture.getPotionEffects()) {
                entity.addPotionEffect(effect, true);
            }

            entity.setFireTicks(effectsCapture.getFireTicks());
        }

        if (modeCapture != null && ArrayUtils.contains(captureParts, CaptureParts.MODE) && (entity instanceof Player)) {
            modeCapture.restore((Player) entity);
        }

        if (healthCapture != null && ArrayUtils.contains(captureParts, CaptureParts.HEALTH)) {
            healthCapture.restore(entity);
        }

        if (foodCapture != null && ArrayUtils.contains(captureParts, CaptureParts.FOOD) && (entity instanceof Player)) {
            foodCapture.restore((Player) entity);
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
        EFFECTS,
        MODE,
        HEALTH,
        FOOD,
        ALL
    }

    //private static class
}
