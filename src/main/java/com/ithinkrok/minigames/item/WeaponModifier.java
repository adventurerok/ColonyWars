package com.ithinkrok.minigames.item;

import com.ithinkrok.minigames.User;
import com.ithinkrok.minigames.event.ListenerEnabledEvent;
import com.ithinkrok.minigames.event.user.world.UserAttackEvent;
import com.ithinkrok.minigames.event.user.world.UserInteractEvent;
import com.ithinkrok.minigames.util.math.Calculator;
import com.ithinkrok.minigames.util.math.ExpressionCalculator;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by paul on 03/01/16.
 */
public class WeaponModifier implements Listener {

    private static final double HEALTH_PER_HEART = 2;
    private static final double TICKS_PER_SECOND = 20;

    /**
     * Calculates damage in hearts
     */
    private Calculator damageCalculator;

    private Calculator fireCalculator;

    private List<EffectModifier> effectModifiers = new ArrayList<>();

    @EventHandler
    public void onListenerEnable(ListenerEnabledEvent event) {
        if(!event.hasConfig()) throw new RuntimeException("A WeaponModifier requires a config");

        load(event.getConfig());
    }

    private void load(ConfigurationSection config) {
        if(config.contains("damage")) damageCalculator = new ExpressionCalculator(config.getString("damage"));
        if(config.contains("fire")) fireCalculator = new ExpressionCalculator(config.getString("fire"));

        if(!config.contains("effects")) return;
        ConfigurationSection effects = config.getConfigurationSection("effects");

        for(String effectName : effects.getKeys(false)) {
            PotionEffectType effectType = PotionEffectType.getByName(effectName);
            effectModifiers.add(new EffectModifier(effectType, effects.getConfigurationSection(effectName)));
        }
    }

    @SuppressWarnings("unchecked")
    @EventHandler
    public void onUserAttack(UserAttackEvent<? extends User> attack) {
        if(attack.getInteractType() == UserInteractEvent.InteractType.REPRESENTING) return;
        if(attack.getDamageCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK) return;

        if(damageCalculator != null) {
            attack.setDamage(damageCalculator.calculate(attack.getUser().getUpgradeLevels()) * HEALTH_PER_HEART);
        }

        if(fireCalculator != null) {
            int fireTicks = (int) (fireCalculator.calculate(attack.getUser().getUpgradeLevels()) * TICKS_PER_SECOND);

            if(fireTicks > 0){
                if(attack.isAttackingUser()) attack.getTargetUser().setFireTicks(attack.getUser(), fireTicks);
                else attack.getClickedEntity().setFireTicks(fireTicks);
            }
        }

        for(EffectModifier effectModifier : effectModifiers) {
            effectModifier.modifyAttack(attack);
        }
    }

    private class EffectModifier {
        private PotionEffectType effectType;
        private Calculator durationCalculator;
        private Calculator levelCalculator;

        public EffectModifier(PotionEffectType effectType, ConfigurationSection config) {
            this.effectType = effectType;
            durationCalculator = new ExpressionCalculator(config.getString("duration"));

            if(!config.contains("level")) levelCalculator = new ExpressionCalculator("1");
            else levelCalculator = new ExpressionCalculator(config.getString("level"));
        }


        @SuppressWarnings("unchecked")
        public <U extends User> void modifyAttack(UserAttackEvent<U> attack) {
            int duration = (int) (durationCalculator.calculate(attack.getUser().getUpgradeLevels()) * TICKS_PER_SECOND);
            int amp = (int) (levelCalculator.calculate(attack.getUser().getUpgradeLevels()) - 1);

            if(duration <= 0 || amp < 0) return;

            if(attack.isAttackingUser() && effectType == PotionEffectType.WITHER) {
                attack.getTargetUser().setWitherTicks(attack.getUser(), duration, amp);
            } else {
                ((LivingEntity)attack.getClickedEntity()).addPotionEffect(new PotionEffect(effectType, duration, amp));
            }
        }
    }

}
