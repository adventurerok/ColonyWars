package com.ithinkrok.mccw.handler;

import com.ithinkrok.mccw.WarsPlugin;
import com.ithinkrok.mccw.data.User;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

/**
 * Created by paul on 18/11/15.
 * <p>
 * Handles the wool minigame
 */
public class WoolHeadMinigame implements LobbyMinigame {

    private WarsPlugin plugin;
    private UUID woolUserUniqueId;

    public WoolHeadMinigame(WarsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void resetMinigame() {
        woolUserUniqueId = null;
    }

    @Override
    public void onUserJoinLobby(User user) {
        if (woolUserUniqueId != null) return;

        user.getPlayerInventory().setHelmet(new ItemStack(Material.WOOL, 1, DyeColor.PINK.getWoolData()));
        woolUserUniqueId = user.getUniqueId();

        plugin.messageAllLocale("player-given-wool", user.getFormattedName());
        user.messageLocale("player-get-wool");
    }

    @Override
    public void onUserQuitLobby(User user) {
        if (!user.getUniqueId().equals(woolUserUniqueId)) return;
        if (plugin.getUsers().size() == 1) {
            woolUserUniqueId = null;
            return;
        }

        while (user.getUniqueId().equals(woolUserUniqueId)) {
            int index = plugin.getRandom().nextInt(plugin.getUsers().size());

            for (User next : plugin.getUsers()) {
                if (index == 0) {
                    woolUserUniqueId = next.getUniqueId();
                    break;
                }
                --index;
            }
        }

        User woolUser = plugin.getUser(woolUserUniqueId);

        woolUser.getPlayerInventory()
                .setHelmet(new ItemStack(Material.WOOL, 1, DyeColor.PINK.getWoolData()));

        plugin.messageAllLocale("player-given-wool", woolUser.getFormattedName());
        woolUser.messageLocale("player-get-wool");
    }

    @Override
    public boolean onUserInteractEntity(User user, Entity entity) {
        if(user.getUniqueId() != woolUserUniqueId || !(entity instanceof Player)) return false;

        user.getPlayerInventory().setHelmet(null);

        User newUser = plugin.getUser((Player) entity);
        newUser.getPlayerInventory().setHelmet(new ItemStack(Material.WOOL, 1, DyeColor.PINK.getWoolData()));
        woolUserUniqueId = newUser.getUniqueId();

        plugin.messageAllLocale("player-transfer-wool", user.getFormattedName(), newUser.getFormattedName());

        return true;
    }

    @Override
    public boolean onUserInteractWorld(User user, Block block) {
        return false;
    }
}
