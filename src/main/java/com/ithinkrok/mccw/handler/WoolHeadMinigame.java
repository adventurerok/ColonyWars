package com.ithinkrok.mccw.handler;

import com.ithinkrok.mccw.WarsPlugin;
import com.ithinkrok.mccw.data.User;
import org.bukkit.DyeColor;
import org.bukkit.Material;
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
    private UUID woolUser;

    public WoolHeadMinigame(WarsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void resetMinigame() {
        woolUser = null;
    }

    @Override
    public void onUserJoinLobby(User user) {
        if (woolUser != null) return;

        user.getPlayerInventory().setHelmet(new ItemStack(Material.WOOL, 1, DyeColor.PINK.getWoolData()));
        woolUser = user.getUniqueId();
    }

    @Override
    public void onUserQuitLobby(User user) {
        if (!user.getUniqueId().equals(woolUser)) return;
        if (plugin.getUsers().size() == 1) {
            woolUser = null;
            return;
        }

        while (user.getUniqueId().equals(woolUser)) {
            int index = plugin.getRandom().nextInt(plugin.getUsers().size());

            for (User next : plugin.getUsers()) {
                if (index == 0) {
                    woolUser = next.getUniqueId();
                    break;
                }
                --index;
            }
        }

        plugin.getUser(woolUser).getPlayerInventory()
                .setHelmet(new ItemStack(Material.WOOL, 1, DyeColor.PINK.getWoolData()));
    }

    @Override
    public boolean onUserInteractEntity(User user, Entity entity) {
        if(user.getUniqueId() != woolUser || !(entity instanceof Player)) return false;

        Player newPlayer = (Player) entity;
        user.getPlayerInventory().setHelmet(null);

        newPlayer.getInventory().setHelmet(new ItemStack(Material.WOOL, 1, DyeColor.PINK.getWoolData()));
        woolUser = newPlayer.getUniqueId();

        return true;
    }
}
