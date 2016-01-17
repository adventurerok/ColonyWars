package com.ithinkrok.cw.item;

import com.ithinkrok.minigames.User;
import com.ithinkrok.minigames.event.ListenerLoadedEvent;
import com.ithinkrok.minigames.event.user.world.UserInteractEvent;
import com.ithinkrok.minigames.inventory.ClickableInventory;
import com.ithinkrok.minigames.inventory.ClickableItem;
import com.ithinkrok.minigames.inventory.event.UserClickItemEvent;
import com.ithinkrok.minigames.util.InventoryUtils;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.UUID;

/**
 * Created by paul on 17/01/16.
 */
public class SpectateChooser implements Listener {

    private String titleLocale;

    @EventHandler
    public void onListenerLoaded(ListenerLoadedEvent event) {
        ConfigurationSection config = event.getConfig();
        if(config == null) config = new MemoryConfiguration();

        titleLocale = config.getString("title_locale");
    }

    @EventHandler
    public void onUserInteract(UserInteractEvent event) {

        ClickableInventory inv = new ClickableInventory(event.getUserGameGroup().getLocale(titleLocale));

        for(User user : event.getUserGameGroup().getUsers()) {
            if(!user.isInGame()) continue;

            ItemStack item = InventoryUtils.createItemWithNameAndLore(Material.SKULL_ITEM, 1, 0, user
                    .getFormattedName());

            UUID userUUID = user.getUuid();
            SkullMeta meta = (SkullMeta) item.getItemMeta();
            meta.setOwner(user.getName());
            item.setItemMeta(meta);

            inv.addItem(new ClickableItem(item) {
                @Override
                public void onClick(UserClickItemEvent event) {
                    User clicked = event.getUserGameGroup().getUser(userUUID);

                    if(clicked == null){
                        event.getUser().redoInventory();
                        return;
                    }

                    event.getUser().teleport(clicked.getLocation());
                    event.getUser().closeInventory();
                }
            });
        }

        event.getUser().showInventory(inv, null);
    }
}
