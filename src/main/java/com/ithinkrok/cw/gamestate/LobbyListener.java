package com.ithinkrok.cw.gamestate;

import com.ithinkrok.minigames.event.user.game.UserJoinEvent;
import com.ithinkrok.minigames.event.user.state.UserDamagedEvent;
import com.ithinkrok.minigames.event.user.state.UserFoodLevelChangeEvent;
import com.ithinkrok.minigames.event.user.world.*;
import com.ithinkrok.minigames.item.CustomItem;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

/**
 * Created by paul on 31/12/15.
 */
public class LobbyListener implements Listener {

    @EventHandler
    public void eventBlockBreak(UserBreakBlockEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void eventBlockPlace(UserPlaceBlockEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void eventUserJoin(UserJoinEvent event) {
        System.out.println(event.getUser().getUuid() + " joined!");
    }

    @EventHandler
    public void eventUserDropItem(UserDropItemEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void eventUserPickupItem(UserPickupItemEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void eventUserDamaged(UserDamagedEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void eventUserInteract(UserInteractEvent event) {
        if(event.getInteractType() == UserInteractEvent.InteractType.REPRESENTING) return;
        if(event.hasItem() && event.getItem().getType() == Material.WRITTEN_BOOK) return;

        if(!event.hasBlock() || !isRedstoneControl(event.getClickedBlock().getType())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void eventUserFoodLevelChange(UserFoodLevelChangeEvent event) {
        event.setFoodLevel(20);
    }

    private static boolean isRedstoneControl(Material type) {
        switch (type) {
            case LEVER:
            case STONE_BUTTON:
            case WOOD_BUTTON:
            case STONE_PLATE:
            case WOOD_PLATE:
            case GOLD_PLATE:
            case IRON_PLATE:
            case WOOD_DOOR:
            case TRAP_DOOR:
                return true;
            default:
                return false;
        }
    }


}
