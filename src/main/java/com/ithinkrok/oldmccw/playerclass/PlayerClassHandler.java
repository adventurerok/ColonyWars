package com.ithinkrok.oldmccw.playerclass;

import com.ithinkrok.oldmccw.event.*;
import com.ithinkrok.oldmccw.inventory.InventoryHandler;

/**
 * Created by paul on 05/11/15.
 * <p>
 * Interface to handle operations that depend on the player's class
 */
public interface PlayerClassHandler extends InventoryHandler {

    /**
     * Called when the user's team has built the first building of a type
     *
     * @param event The event object for this event
     */
    void onBuildingBuilt(UserTeamBuildingBuiltEvent event);

    /**
     * Called when the user begins a game
     *
     * @param event The event object for this event
     */
    void onUserBeginGame(UserBeginGameEvent event);

    /**
     * Called when the user left or right clicks on air, a block or an entity
     *
     * @param event The event object describing this event
     * @return If the event should be cancelled (If the event was handled by this handler)
     */
    boolean onInteract(UserInteractEvent event);

    /**
     * Called when the user's level in a skill increases
     *
     * @param event The event object describing this event
     */
    void onPlayerUpgrade(UserUpgradeEvent event);

    /**
     * Called when the user attacks an entity, such as another user or a wolf
     *
     * @param event The event object describing this event
     */
    void onUserAttack(UserAttackEvent event);

    /**
     * Called when the user is attacked by an entity
     *
     * @param event The event object describing this event
     */
    void onUserAttacked(UserAttackedEvent event);

    /**
     * Called when an ability of the user has cooled down
     *
     * @param event The event object describing this event
     */
    void onAbilityCooldown(UserAbilityCooldownEvent event);
}
