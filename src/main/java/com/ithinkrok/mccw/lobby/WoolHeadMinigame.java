package com.ithinkrok.mccw.lobby;

import com.ithinkrok.mccw.WarsPlugin;
import com.ithinkrok.mccw.data.User;
import com.ithinkrok.mccw.event.UserInteractEvent;
import com.ithinkrok.mccw.event.UserJoinLobbyEvent;
import com.ithinkrok.mccw.event.UserQuitLobbyEvent;
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
public class WoolHeadMinigame extends LobbyMinigameAdapter {

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
    public void onUserJoinLobby(UserJoinLobbyEvent event) {
        if (woolUserUniqueId != null) return;

        event.getUserInventory().setHelmet(new ItemStack(Material.WOOL, 1, DyeColor.PINK.getWoolData()));
        woolUserUniqueId = event.getUser().getUniqueId();

        plugin.messageAllLocale("minigames.wool.initial", event.getUser().getFormattedName());
        event.getUser().sendLocale("minigames.wool.given");
    }

    @Override
    public void onUserQuitLobby(UserQuitLobbyEvent event) {
        if (!event.getUser().getUniqueId().equals(woolUserUniqueId)) return;
        if (plugin.getUsers().size() <= 1) {
            woolUserUniqueId = null;
            return;
        }

        while (event.getUser().getUniqueId().equals(woolUserUniqueId)) {
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

        plugin.messageAllLocale("minigames.wool.initial", woolUser.getFormattedName());
        woolUser.sendLocale("minigames.wool.given");
    }

    @Override
    public boolean onUserInteract(UserInteractEvent event) {
        return event.hasEntity() && onUserInteractEntity(event.getUser(), event.getClickedEntity());
    }

    public boolean onUserInteractEntity(User user, Entity entity) {
        if(user.getUniqueId() != woolUserUniqueId || !(entity instanceof Player)) return false;

        user.getPlayerInventory().setHelmet(null);

        User newUser = plugin.getUser((Player) entity);
        newUser.getPlayerInventory().setHelmet(new ItemStack(Material.WOOL, 1, DyeColor.PINK.getWoolData()));
        woolUserUniqueId = newUser.getUniqueId();

        plugin.messageAllLocale("minigames.wool.transfer", user.getFormattedName(), newUser.getFormattedName());

        return true;
    }


}
