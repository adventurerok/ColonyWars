package com.ithinkrok.mccw.lobby;

import com.ithinkrok.mccw.WarsPlugin;
import com.ithinkrok.mccw.data.User;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;

/**
 * Created by paul on 18/11/15.
 *
 * Handles the parcour minigame
 */
public class ParcourMinigame implements LobbyMinigame {

    WarsPlugin plugin;

    public ParcourMinigame(WarsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void resetMinigame() {

    }

    @Override
    public void onUserJoinLobby(User user) {

    }

    @Override
    public void onUserQuitLobby(User user) {

    }

    @Override
    public boolean onUserInteractEntity(User user, Entity entity) {
        return false;
    }

    @Override
    public boolean onUserInteractWorld(User user, Block block) {
        if(block.getType() != Material.ENDER_CHEST) return false;

        plugin.messageAllLocale("player-win-parcour", user.getFormattedName());
        user.addPlayerCash(1000);
        user.teleport(plugin.getLobbySpawn());

        return true;
    }
}
