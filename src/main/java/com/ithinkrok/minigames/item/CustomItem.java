package com.ithinkrok.minigames.item;

import com.ithinkrok.minigames.util.InventoryUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

/**
 * Created by paul on 02/01/16.
 * <p>
 * An item with custom use or inventory click listeners
 */
public class CustomItem {

    private static int customItemCount = 0;

    private ItemStack itemStack;
    private ItemConfiguration itemConfiguration;
    private int customItemId = customItemCount++;

    public CustomItem(ItemConfiguration config) {
        itemConfiguration = config.clone();
        itemConfiguration.item = this;
    }

    private String generateIdString() {
        StringBuilder result = new StringBuilder(ChatColor.BLACK.toString());

        for(int i = 0; i < 16; i += 4) {
            result.append(ChatColor.getByChar(Integer.toHexString((customItemId >> i) & 0xf)));
        }

        result.append(ChatColor.WHITE);

        return result.toString();
    }

    private void configChanged() {
        if(itemStack == null) itemStack = new ItemStack(itemConfiguration.getMaterial());

        itemStack.setAmount(itemConfiguration.getAmount());
        itemStack.setDurability((short) itemConfiguration.getDurabiltiy());

        InventoryUtils.setItemNameAndLore(itemStack, generateIdString() + itemConfiguration.getName(),
                itemConfiguration.getLore());

        for(Enchantment enchantment : itemStack.getEnchantments().keySet()) {
            itemStack.removeEnchantment(enchantment);
        }

        InventoryUtils.enchantItem(itemStack, itemConfiguration.getEnchantments());
    }

    public ItemConfiguration getItemConfiguration() {
        return itemConfiguration;
    }

    public static class ItemConfiguration {
        private CustomItem item;

        private Material material;

        private int amount = 1;
        private int durabiltiy = 0;
        private String name = null;
        private String[] lore = new String[0];
        private Object[] enchantments;

        public ItemConfiguration(Material material) {
            this.material = material;
        }

        public Material getMaterial() {
            return this.material;
        }

        public ItemConfiguration setMaterial(Material material) {
            this.material = material;
            if(item != null) item.configChanged();
            return this;
        }

        public int getAmount() {
            return this.amount;
        }

        public ItemConfiguration setAmount(int amount) {
            this.amount = amount;
            if(item != null) item.configChanged();
            return this;
        }

        public int getDurabiltiy() {
            return this.durabiltiy;
        }

        public ItemConfiguration setDurabiltiy(int durabiltiy) {
            this.durabiltiy = durabiltiy;
            if(item != null) item.configChanged();
            return this;
        }

        public String getName() {
            return this.name;
        }

        public ItemConfiguration setName(String name) {
            this.name = name;
            if(item != null) item.configChanged();
            return this;
        }

        public String[] getLore() {
            return this.lore;
        }

        public ItemConfiguration setLore(String... lore) {
            this.lore = lore;
            if(item != null) item.configChanged();
            return this;
        }

        public Object[] getEnchantments() {
            return this.enchantments;
        }

        public ItemConfiguration setEnchantments(Object... enchantments) {
            this.enchantments = enchantments;
            if(item != null) item.configChanged();
            return this;
        }

        @SuppressWarnings("CloneDoesntCallSuperClone")
        @Override
        public ItemConfiguration clone() {
            return new ItemConfiguration(material).setAmount(amount).setDurabiltiy(durabiltiy).setName(name)
                    .setLore(lore).setEnchantments(enchantments);
        }
    }
}
