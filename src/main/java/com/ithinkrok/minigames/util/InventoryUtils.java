package com.ithinkrok.minigames.util;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by paul on 31/12/15.
 */
public class InventoryUtils {

    private static final String ID_START = ChatColor.BLACK.toString() + ChatColor.WHITE.toString();

    public static boolean isEmpty(ItemStack stack){
        return stack == null || stack.getType() == Material.AIR || stack.getAmount() == 0;
    }

    public static boolean isMaterial(ItemStack stack, Material material) {
        return stack != null && stack.getType() == material;
    }

    public static ItemStack createLeatherArmorItem(Material material, Color armorColor) {
        return setLeatherArmorColor(new ItemStack(material), armorColor);
    }

    public static ItemStack setUnbreakable(ItemStack itemStack, boolean unbreakable) {
        ItemMeta meta = itemStack.getItemMeta();

        meta.spigot().setUnbreakable(unbreakable);
        itemStack.setItemMeta(meta);

        return itemStack;
    }

    public static ItemStack setLeatherArmorColor(ItemStack armor, Color armorColor) {
        LeatherArmorMeta meta = (LeatherArmorMeta) armor.getItemMeta();

        meta.setColor(armorColor);
        armor.setItemMeta(meta);
        return armor;
    }

    public static ItemStack enchantItem(ItemStack item, Object... enchantments) {
        for (int i = 0; i < enchantments.length; i += 2) {
            int level = (int) enchantments[i + 1];
            if (level == 0) continue;

            item.addUnsafeEnchantment((Enchantment) enchantments[i], level);
        }

        return item;
    }

    public static ItemStack parseItem(String itemString) {
        String[] parts = itemString.trim().split(",");

        Material material = Material.matchMaterial(parts[0].trim());

        int amount = parts.length >= 2 ? Integer.parseInt(parts[1].trim()) : 1;
        int durability = parts.length >= 3 ? Integer.parseInt(parts[2].trim()) : 0;

        return new ItemStack(material, amount, (short) durability);
    }

    public static ItemStack createPotion(PotionType type, int level, boolean splash, boolean extended, int amount){
        Potion pot = new Potion(type, level);
        pot.setSplash(splash);
        if(extended) pot.setHasExtendedDuration(true);

        return pot.toItemStack(amount);
    }

    public static ItemStack setItemNameAndLore(ItemStack item, String name, String... lore) {
        ItemMeta im = item.getItemMeta();
        if (name != null) im.setDisplayName(name);
        im.setLore(Arrays.asList(lore));
        item.setItemMeta(im);
        return item;
    }

    public static ItemStack createItemWithEnchantments(Material mat, int amount, int damage, String name, String desc,
                                                       Object... enchantments) {
        ItemStack stack;
        if (desc != null) stack = createItemWithNameAndLore(mat, amount, damage, name, desc);
        else stack = createItemWithNameAndLore(mat, amount, damage, name);

        return enchantItem(stack, enchantments);
    }

    public static ItemStack createItemWithNameAndLore(Material mat, int amount, int damage, String name,
                                                      String... lore) {
        ItemStack stack = new ItemStack(mat, amount, (short) damage);

        return setItemNameAndLore(stack, name, lore);
    }

    public static void replaceItem(Inventory inventory, ItemStack stack) {
        int first = inventory.first(stack.getType());

        if (first == -1) inventory.addItem(stack);
        else inventory.setItem(first, stack);
    }

    public static String generateIdentifierString(int identifier) {
        StringBuilder result = new StringBuilder(ID_START);

        for(int i = 28; i >= 0; i -= 4) {
            result.append(ChatColor.getByChar(Integer.toHexString((identifier >> i) & 0xf)));
        }

        return result.append(ID_START).toString();
    }

    public static boolean isIdentifierString(String test) {
        return test.startsWith(ID_START);
    }

    public static int getIdentifierFromString(String idString) {
        idString = idString.substring(4, 20).replace("§", "");

        return Integer.parseInt(idString, 16);
    }

    public static ItemStack addIdentifier(ItemStack item, int identifier) {
        ItemMeta im = item.getItemMeta();

        List<String> lore;
        if(im.hasLore()) lore = im.getLore();
        else lore = new ArrayList<>();

        lore.add(generateIdentifierString(identifier));

        im.setLore(lore);
        item.setItemMeta(im);

        return item;
    }

    public static int getIdentifier(ItemStack item) {
        if(isEmpty(item)) return -1;
        ItemMeta im = item.getItemMeta();

        if(!im.hasLore()) return -1;
        List<String> lore = im.getLore();

        for(String s : lore) {
            if(isIdentifierString(s)) return getIdentifierFromString(s);
        }

        return -1;
    }

}
