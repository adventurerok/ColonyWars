package com.ithinkrok.mccw.handler;

import com.ithinkrok.mccw.data.User;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;

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
     * @param user The User who is joining the lobby
     */
    void onUserJoinLobby(User user);

    /**
     * Called when a user quits the lobby. Note this is only called when the user leaves the game when there is no
     * game in progress, not when the user leaves the lobby as the next game starts.
     * @param user The User who is leaving the lobby
     */
    void onUserQuitLobby(User user);

    /**
     * Called when a User right clicks on an entity while in the lobby
     * @param user The User who right clicked on the entity
     * @param entity The entity the User right clicked on
     * @return If the event was handled and should not be handled in other LobbyMinigame classes
     */
    boolean onUserInteractEntity(User user, Entity entity);

    /**
     * Called when a User right clicks on a block in the lobby
     * @param user The User who right clicked on the block
     * @param block The block the User right clicked on
     * @return If the event was handled and should not be handled in other LobbyMinigame classes
     */
    boolean onUserInteractWorld(User user, Block block);
}
