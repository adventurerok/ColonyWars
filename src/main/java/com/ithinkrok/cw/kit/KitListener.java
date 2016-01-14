package com.ithinkrok.cw.kit;

import com.ithinkrok.cw.event.BuildingBuiltEvent;
import com.ithinkrok.cw.event.ShopOpenEvent;
import com.ithinkrok.minigames.User;
import com.ithinkrok.minigames.event.ListenerLoadedEvent;
import com.ithinkrok.minigames.util.ConfigUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by paul on 14/01/16.
 */
public class KitListener implements Listener {

    @EventHandler
    public void onListenerLoaded(ListenerLoadedEvent event) {

    }

    private static class BuildingConfig {

        private List<ConfigurationSection> extraShopItems = new ArrayList<>();
        private List<String> customItemGives = new ArrayList<>();
        private List<ItemStack> itemStackGives = new ArrayList<>();

        public BuildingConfig(ConfigurationSection config) {
            if (config.contains("shop")) extraShopItems = ConfigUtils.getConfigList(config, "shop");
            if (config.contains("custom_items")) customItemGives = config.getStringList("custom_items");

            if (config.contains("items")) {
                ConfigurationSection items = config.getConfigurationSection("items");
                itemStackGives.addAll(items.getKeys(false).stream()
                        .map(unusedName -> ConfigUtils.getItemStack(config, unusedName)).collect(Collectors.toList()));
            }
        }

        public void onShopOpen(ShopOpenEvent event) {
            event.getShop().loadFromConfig(extraShopItems);
        }

        public void onBuild(User user, BuildingBuiltEvent event) {
            //TODO add custom items
        }
    }
}
