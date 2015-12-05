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
        switch(block.getType()){
            case ENDER_CHEST:
                plugin.messageAllLocale("minigames.parcour.winner", user.getFormattedName(), 500);
                user.addPlayerCash(500);
                user.teleport(plugin.getLobbySpawn());

                return true;
            case REDSTONE_LAMP_OFF:
                plugin.messageAllLocale("minigames.parcour.advanced", user.getFormattedName(), 2000);
                user.addPlayerCash(2000);
                user.teleport(plugin.getLobbySpawn());

                return true;
            default:
                return false;
        }


    }
}
