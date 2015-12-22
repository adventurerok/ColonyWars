package com.ithinkrok.mccw.inventory;

import com.ithinkrok.mccw.WarsPlugin;
import com.ithinkrok.mccw.data.Building;
import com.ithinkrok.mccw.data.Team;
import com.ithinkrok.mccw.data.User;
import com.ithinkrok.mccw.util.item.InventoryUtils;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.List;

/**
 * Created by paul on 13/11/15.
 *
 * Handles Spectator inventories
 */
public class SpectatorInventory implements InventoryHandler {

    private WarsPlugin plugin;

    public SpectatorInventory(WarsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onInventoryClick(ItemStack item, Building building, User user, Team team) {
        if (item == null || item.getType() != Material.SKULL_ITEM) return false;
        String owner = ((SkullMeta) item.getItemMeta()).getOwner();

        for (User info : plugin.getUsers()) {
            if (!info.getName().equals(owner)) continue;

            user.teleport(info.getLocation());
            return true;
        }

        return false;
    }

    @Override
    public void addInventoryItems(List<ItemStack> inventory, Building building, User user, Team team) {
        for(User info : plugin.getUsers()){
            if (!info.isInGame() || info.getTeamColor() == null) continue;

            ItemStack head = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
            InventoryUtils.setItemNameAndLore(head, info.getFormattedName(),
                    plugin.getLocale("spectators.player.name", info.getFormattedName()));


            SkullMeta skullMeta = (SkullMeta) head.getItemMeta();

            skullMeta.setOwner(info.getName());
            head.setItemMeta(skullMeta);

            inventory.add(head);
        }
    }
}
