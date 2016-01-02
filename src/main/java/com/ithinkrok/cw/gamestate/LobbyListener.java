package com.ithinkrok.cw.gamestate;

import com.ithinkrok.cw.CWUser;
import com.ithinkrok.minigames.event.user.*;
import com.ithinkrok.minigames.item.ClickableInventory;
import com.ithinkrok.minigames.item.ClickableItem;
import com.ithinkrok.minigames.item.event.UserClickItemEvent;
import com.ithinkrok.minigames.item.event.UserViewItemEvent;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

/**
 * Created by paul on 31/12/15.
 */
public class LobbyListener implements Listener {

    @EventHandler
    public void eventBlockBreak(UserBreakBlockEvent<CWUser> event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void eventBlockPlace(UserPlaceBlockEvent<CWUser> event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void eventUserJoin(UserJoinEvent<CWUser> event) {
        System.out.println(event.getUser().getUuid() + " joined!");
    }

    @EventHandler
    public void eventUserDropItem(UserDropItemEvent<CWUser> event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void eventUserPickupItem(UserPickupItemEvent<CWUser> event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void eventUserDamaged(UserDamagedEvent<CWUser> event) {
        event.setCancelled(true);

        ClickableInventory<CWUser> inventory = new ClickableInventory<>("m8, amazing");
        inventory.addItem(new ClickableItem<CWUser>(new ItemStack(Material.DIAMOND)) {
            @Override
            public boolean isVisible(UserViewItemEvent<CWUser> event) {
                return true;
            }

            @Override
            public void onClick(UserClickItemEvent<CWUser> event) {

            }
        });

        event.getUser().showInventory(inventory);
    }

    @EventHandler
    public void eventUserInteract(UserInteractEvent<CWUser> event) {
        if(event.hasItem() && event.getItem().getType() == Material.WRITTEN_BOOK) return;

        if(!event.hasBlock() || !isRedstoneControl(event.getClickedBlock().getType())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void eventUserFoodLevelChange(UserFoodLevelChangeEvent<CWUser> event) {
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
