package com.ithinkrok.mccw.handler;

import com.ithinkrok.mccw.data.User;
import org.bukkit.entity.Entity;

/**
 * Created by paul on 18/11/15.
 *
 * A minigame in the lobby
 */
public interface LobbyMinigame {

    void resetMinigame();

    void onUserJoinLobby(User user);

    void onUserQuitLobby(User user);

    boolean onUserInteractEntity(User user, Entity entity);
}
