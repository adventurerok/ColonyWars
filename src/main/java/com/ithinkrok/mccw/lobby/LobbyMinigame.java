package com.ithinkrok.mccw.lobby;

import com.ithinkrok.mccw.event.*;

/**
 * Created by paul on 18/11/15.
 *
 * A minigame in the lobby
 */
public interface LobbyMinigame {

    /**
     * Called when the server is first started and when a game ends to reset the lobby minigame.
     */
    void resetMinigame();

    /**
     * Called when a user joins the lobby. This can be either if they join while there is no game in progress or when
     * they join the lobby again after a game.
     * @param event The event object describing this event
     */
    void onUserJoinLobby(UserJoinLobbyEvent event);

    /**
     * Called when a user quits the lobby. Note this is only called when the user leaves the game when there is no
     * game in progress, not when the user leaves the lobby as the next game starts.
     * @param event the event object describing this event
     */
    void onUserQuitLobby(UserQuitLobbyEvent event);

    /**
     * Called when a User left or right clicks while in the lobby
     * @param event The event object describing this event
     * @return If the event was handled and should not be handled in other LobbyMinigame classes
     */
    boolean onUserInteract(UserInteractEvent event);

    /**
     * Called when a User breaks a block while in the lobby.
     * These events are cancelled by default and can be uncancelled by the LobbyMinigame
     * @param event The event object describing this event
     */
    void onUserBreakBlock(UserBreakBlockEvent event);

    /**
     * Called when a User is damaged.
     * These events are cancelled by default and can be uncancelled by the LobbyMinigame
     * @param event The event object describing this event
     */
    void onUserDamaged(UserDamagedEvent event);

}
