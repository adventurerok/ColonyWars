package com.ithinkrok.minigames.item;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;

/**
 * Created by paul on 02/01/16.
 *
 * An item with custom use or inventory click listeners
 */
public class CustomItem {

    public static class ItemConfiguration {
        private Material material;

        private int amount = 1;
        private int durabiltiy = 0;
        private String name = null;
        private String[] lore = new String[0];
        private Collection<Enchantment> enchantments;


        public Material getMaterial() {
            return this.material;
        }

        public ItemConfiguration setMaterial(Material material) {
            this.material = material;
            return this;
        }

        public int getAmount() {
            return this.amount;
        }

        public ItemConfiguration setAmount(int amount) {
            this.amount = amount;
            return this;
        }

        public int getDurabiltiy() {
            return this.durabiltiy;
        }

        public ItemConfiguration setDurabiltiy(int durabiltiy) {
            this.durabiltiy = durabiltiy;
            return this;
        }

        public String getName() {
            return this.name;
        }

        public ItemConfiguration setName(String name) {
            this.name = name;
            return this;
        }

        public String[] getLore() {
            return this.lore;
        }

        public ItemConfiguration setLore(String[] lore) {
            this.lore = lore;
            return this;
        }

        public Collection<Enchantment> getEnchantments() {
            return this.enchantments;
        }

        public ItemConfiguration setEnchantments(Collection<Enchantment> enchantments) {
            this.enchantments = enchantments;
            return this;
        }
    }

    private ItemStack itemStack;


    public CustomItem(ItemConfiguration config) {
        //ItemStack
    }

}
