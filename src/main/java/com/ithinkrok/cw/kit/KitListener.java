package com.ithinkrok.cw.kit;

import com.ithinkrok.cw.event.BuildingBuiltEvent;
import com.ithinkrok.cw.event.ShopOpenEvent;
import com.ithinkrok.minigames.User;
import com.ithinkrok.minigames.event.ListenerLoadedEvent;
import com.ithinkrok.minigames.item.CustomItem;
import com.ithinkrok.minigames.util.ConfigUtils;
import com.ithinkrok.minigames.util.InventoryUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by paul on 14/01/16.
 */
public class KitListener implements Listener {

    private User owner;

    private Map<String, BuildingConfig> buildingConfigs = new HashMap<>();

    @EventHandler
    public void onListenerLoaded(ListenerLoadedEvent<User> event) {
        owner = event.getCreator();

        ConfigurationSection buildings = event.getConfig().getConfigurationSection("buildings");

        for(String buildingName : buildings.getKeys(false)) {
            ConfigurationSection buildingConfig = buildings.getConfigurationSection(buildingName);

            buildingConfigs.put(buildingName, new BuildingConfig(buildingConfig));
        }
    }

    @EventHandler
    public void onShopOpen(ShopOpenEvent event) {
        BuildingConfig config = buildingConfigs.get(event.getBuilding().getBuildingName());

        if(config == null) return;
        config.onShopOpen(event);
    }

    @EventHandler
    public void onBuildingBuilt(BuildingBuiltEvent event) {
        BuildingConfig config = buildingConfigs.get(event.getBuilding().getBuildingName());

        if(config == null) return;
        config.onBuild(owner, event);
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
            PlayerInventory inv = user.getInventory();

            for(String customItemName : customItemGives) {
                CustomItem customItem = user.getGameGroup().getCustomItem(customItemName);
                if(!InventoryUtils.containsIdentifier(inv, customItem.getIdentifier())) {
                    inv.addItem(customItem.createForUser(user));
                }
            }

            for(ItemStack item : itemStackGives) {
                inv.addItem(item.clone());
            }
        }
    }
}
