package com.ithinkrok.minigames.listener;

import com.ithinkrok.minigames.event.ListenerLoadedEvent;
import com.ithinkrok.minigames.event.user.game.UserJoinEvent;
import com.ithinkrok.minigames.item.CustomItem;
import com.ithinkrok.minigames.util.ConfigUtils;
import com.ithinkrok.minigames.util.math.MapVariables;
import com.ithinkrok.minigames.util.math.Variables;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by paul on 04/01/16.
 */
public class GiveCustomItemsOnJoin implements Listener {

    private boolean clearInventory = false;

    private List<CustomItemInfo> items = new ArrayList<>();

    @EventHandler
    public void onListenerEnabled(ListenerLoadedEvent event) {
        ConfigurationSection config = event.getConfig();

        clearInventory = config.getBoolean("clear_inventory", false);

        List<ConfigurationSection> itemConfigs = ConfigUtils.getConfigList(config, "items");
        items.addAll(itemConfigs.stream().map(CustomItemInfo::new).collect(Collectors.toList()));
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onUserJoin(UserJoinEvent event) {
        if(clearInventory) event.getUser().getInventory().clear();

        for (CustomItemInfo itemInfo : items) {
            CustomItem item = event.getUser().getGameGroup().getCustomItem(itemInfo.customItem);
            ItemStack itemStack;

            if (itemInfo.customVariables != null)
                itemStack = item.createWithVariables(event.getUser().getGameGroup(), itemInfo.customVariables);
            else itemStack = event.getUser().createCustomItemForUser(item);

            if(itemInfo.slot < 0) event.getUser().getInventory().addItem(itemStack);
            else event.getUser().getInventory().setItem(itemInfo.slot, itemStack);
        }
    }

    private static class CustomItemInfo {
        private Variables customVariables;
        private String customItem;
        private int slot = -1;

        public CustomItemInfo(ConfigurationSection config) {
            customItem = config.getString("name");
            slot = config.getInt("slot", -1);

            if (!config.contains("custom_variables")) return;
            customVariables = new MapVariables(config.getConfigurationSection("custom_variables"));
        }
    }
}
