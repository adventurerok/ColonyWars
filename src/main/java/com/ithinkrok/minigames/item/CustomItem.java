package com.ithinkrok.minigames.item;

import com.ithinkrok.minigames.User;
import com.ithinkrok.minigames.item.event.CustomItemLoreCalculateEvent;
import com.ithinkrok.minigames.lang.LanguageLookup;
import com.ithinkrok.minigames.util.EventExecutor;
import com.ithinkrok.minigames.util.InventoryUtils;
import com.ithinkrok.minigames.util.math.Calculator;
import com.ithinkrok.minigames.util.math.Variables;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by paul on 02/01/16.
 * <p>
 * An item with custom use or inventory click listeners
 */
public class CustomItem<U extends User> implements Identifiable {

    private static int customItemCount = 0;

    private int customItemId = customItemCount++;

    private List<Listener> rightClickActions;
    private List<Listener> leftClickActions;
    private List<Listener> timeoutActions;
    private List<Listener> attackActions;

    private String name;
    private String itemDisplayLocale;
    private Material itemMaterial;
    private int durability;
    private boolean unbreakable;

    private String rightClickCooldownFinishedLocale;
    private Calculator rightClickCooldown;
    private String timeoutAbility;
    private String timeoutDescriptionLocale;
    private String timeoutFinishedLocale;
    private Calculator timeoutCalculator;
    private String rightClickCooldownAbility;
    private String descriptionLocale;

    private List<EnchantmentEffect> enchantmentEffects;

    public CustomItem(String name, ConfigurationSection config) {
        this.name = name;

        this.itemDisplayLocale = config.getString("display_name", null);
        this.descriptionLocale = config.getString("description", null);
        this.itemMaterial = Material.matchMaterial(config.getString("material"));
        this.durability = config.getInt("durability", 0);
        this.unbreakable = config.getBoolean("unbreakable", true);
    }

    public ItemStack createWithVariables(LanguageLookup languageLookup, Variables variables) {
        List<String> lore = new ArrayList<>();

        if (descriptionLocale != null) lore.add(languageLookup.getLocale(descriptionLocale));

        CustomItemLoreCalculateEvent<U> event =
                new CustomItemLoreCalculateEvent<>(this, lore, languageLookup, variables);

        EventExecutor.executeEvent(event, rightClickActions, leftClickActions, timeoutActions, attackActions);

        if (rightClickCooldown != null) {
            double seconds = rightClickCooldown.calculate(variables);
            lore.add(languageLookup.getLocale("lore.cooldown", seconds));
        }

        if (timeoutCalculator != null) {
            double seconds = timeoutCalculator.calculate(variables);
            lore.add(languageLookup.getLocale(timeoutDescriptionLocale, seconds));
        }

        String[] loreArray = new String[lore.size()];
        lore.toArray(loreArray);

        String itemDisplayName = languageLookup.getLocale(itemDisplayLocale);

        ItemStack item =
                InventoryUtils.createItemWithNameAndLore(itemMaterial, 1, durability, itemDisplayName, loreArray);

        if(enchantmentEffects != null) {
            for(EnchantmentEffect enchantmentEffect : enchantmentEffects) {
                int level = (int) enchantmentEffect.levelCalculator.calculate(variables);
                if(level <= 0) continue;

                item.addUnsafeEnchantment(enchantmentEffect.enchantment, level);
            }
        }

        item.getItemMeta().spigot().setUnbreakable(unbreakable);

        return item;
    }

    private static class EnchantmentEffect {
        private Enchantment enchantment;
        private Calculator levelCalculator;

        public EnchantmentEffect(Enchantment enchantment, Calculator levelCalculator) {
            this.enchantment = enchantment;
            this.levelCalculator = levelCalculator;
        }
    }

    public String getName() {
        return name;
    }

    @Override
    public int getIdentifier() {
        return customItemId;
    }

}
