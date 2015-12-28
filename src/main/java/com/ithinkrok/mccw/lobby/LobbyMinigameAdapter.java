package com.ithinkrok.mccw.lobby;

import com.ithinkrok.mccw.event.*;

/**
 * Created by paul on 28/12/15.
 */
public abstract  class LobbyMinigameAdapter implements LobbyMinigame {

    @Override
    public void resetMinigame() {

    }

    @Override
    public void onUserJoinLobby(UserJoinLobbyEvent event) {

    }

    @Override
    public void onUserQuitLobby(UserQuitLobbyEvent event) {

    }

    @Override
    public boolean onUserInteract(UserInteractEvent event) {
        return false;
    }

    @Override
    public void onUserBreakBlock(UserBreakBlockEvent event) {

    }

    @Override
    public void onUserDamaged(UserDamagedEvent event) {

    }
}
