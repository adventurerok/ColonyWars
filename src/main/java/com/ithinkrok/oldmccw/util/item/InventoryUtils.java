package com.ithinkrok.oldmccw.util.item;

import com.ithinkrok.oldmccw.data.Team;
import com.ithinkrok.oldmccw.data.User;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionType;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by paul on 03/11/15.
 * <p>
 * Utility class for inventories
 */
public class InventoryUtils {

    public static ItemStack addPrice(ItemStack item, int cost, boolean team) {
        ItemMeta im = item.getItemMeta();

        String teamText = ChatColor.GRAY + " (" + ChatColor.GOLD + (team ? "Team Money" : "Player Money") +
                ChatColor.GRAY + ")";

        List<String> lore;
        if (im.hasLore()) lore = im.getLore();
        else lore = new ArrayList<>();

        lore.add(ChatColor.GRAY + "Cost: " + ChatColor.DARK_AQUA + "$" + cost + teamText);
        im.setLore(lore);

        item.setItemMeta(im);

        return item;
    }

    public static ItemStack enchantItem(ItemStack item, Object... enchantments) {
        return com.ithinkrok.minigames.util.InventoryUtils.enchantItem(item, enchantments);
    }

    public static ItemStack createPotion(PotionType type, int level, boolean splash, boolean extended, int amount) {
        return com.ithinkrok.minigames.util.InventoryUtils.createPotion(type, level, splash, extended, amount);
    }

    public static ItemStack createItemWithEnchantments(Material mat, int amount, int damage, String name, String desc,
                                                       Object... enchantments) {
        return com.ithinkrok.minigames.util.InventoryUtils
                .createItemWithEnchantments(mat, amount, damage, name, desc, enchantments);
    }

    public static ItemStack createItemWithNameAndLore(Material mat, int amount, int damage, String name,
                                                      String... lore) {
        ItemStack stack = new ItemStack(mat, amount, (short) damage);

        return setItemNameAndLore(stack, name, lore);
    }

    public static ItemStack setItemNameAndLore(ItemStack item, String name, String... lore) {
        return com.ithinkrok.minigames.util.InventoryUtils.setItemNameAndLore(item, name, lore);
    }

    public static boolean payWithTeamCash(int amount, Team team, User user) {
        int teamAmount = Math.min(team.getTeamCash(), amount);
        int playerAmount = amount - teamAmount;

        if (playerAmount > 0 && !user.subtractPlayerCash(playerAmount, true)) return false;

        if (teamAmount > 0) team.subtractTeamCash(teamAmount, true);

        if (playerAmount > 0) user.sendLocale("purchase.team-failed", playerAmount);

        return true;
    }

    public static boolean hasTeamCash(int amount, Team team, User user) {
        int teamAmount = Math.min(team.getTeamCash(), amount);
        int playerAmount = amount - teamAmount;

        return !(playerAmount > 0 && !user.hasPlayerCash(playerAmount));

    }

    public static void playBuySound(User user) {
        user.playSound(user.getLocation(), Sound.BLAZE_HIT, 1.0f, 1.0f);
    }

    public static void replaceItem(Inventory inventory, ItemStack stack) {
        com.ithinkrok.minigames.util.InventoryUtils.replaceItem(inventory, stack);
    }
}
