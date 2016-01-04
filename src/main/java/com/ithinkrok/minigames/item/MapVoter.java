package com.ithinkrok.minigames.item;

import com.ithinkrok.minigames.event.ListenerEnabledEvent;
import com.ithinkrok.minigames.event.user.world.UserInteractEvent;
import com.ithinkrok.minigames.item.event.UserClickItemEvent;
import com.ithinkrok.minigames.item.event.UserViewItemEvent;
import com.ithinkrok.minigames.metadata.MapVote;
import com.ithinkrok.minigames.util.InventoryUtils;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * Created by paul on 04/01/16.
 */
public class MapVoter implements Listener {

    private List<String> votable;
    private Material mapMaterial;

    @EventHandler
    public void onListenerEnabled(ListenerEnabledEvent event) {
        ConfigurationSection config = event.getConfig();

        votable = config.getStringList("votable_maps");
        mapMaterial = Material.matchMaterial(config.getString("map_material", "EMPTY_MAP"));
    }

    @SuppressWarnings("unchecked")
    @EventHandler
    public void onInteract(UserInteractEvent event) {
        if (event.getInteractType() != UserInteractEvent.InteractType.RIGHT_CLICK) return;
        event.setCancelled(true);

        ClickableInventory inventory = new ClickableInventory("Map Voter");

        for (String mapName : votable) {
            ItemStack display = InventoryUtils.createItemWithNameAndLore(mapMaterial, 1, 0, mapName);

            ClickableItem item = new ClickableItem(display) {
                @Override
                public boolean isVisible(UserViewItemEvent event) {
                    return true; //TODO replace isVisible in ClickableItem with a method to calculate the ItemStack
                }

                @Override
                public void onClick(UserClickItemEvent event) {
                    event.getUser().setMetadata(new MapVote(event.getUser(), mapName));
                }
            };

            inventory.addItem(item);
        }

        event.getUser().showInventory(inventory);
    }
}
