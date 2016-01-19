package com.ithinkrok.cw.kit;

import com.ithinkrok.cw.event.BuildingBuiltEvent;
import com.ithinkrok.cw.event.ShopOpenEvent;
import com.ithinkrok.minigames.User;
import com.ithinkrok.minigames.event.ListenerLoadedEvent;
import com.ithinkrok.minigames.event.MinigamesEventHandler;
import com.ithinkrok.minigames.item.CustomItem;
import com.ithinkrok.minigames.util.ConfigUtils;
import com.ithinkrok.minigames.util.InventoryUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

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

    @MinigamesEventHandler
    public void onListenerLoaded(ListenerLoadedEvent<User> event) {
        owner = event.getCreator();

        ConfigurationSection buildings = event.getConfig().getConfigurationSection("buildings");

        for (String buildingName : buildings.getKeys(false)) {
            ConfigurationSection buildingConfig = buildings.getConfigurationSection(buildingName);

            buildingConfigs.put(buildingName, new BuildingConfig(buildingConfig));
        }
    }

    @MinigamesEventHandler
    public void onShopOpen(ShopOpenEvent event) {
        BuildingConfig config = buildingConfigs.get(event.getBuilding().getBuildingName());

        if (config == null) return;
        config.onShopOpen(event);
    }

    @MinigamesEventHandler
    public void onBuildingBuilt(BuildingBuiltEvent event) {
        BuildingConfig config = buildingConfigs.get(event.getBuilding().getBuildingName());

        if (config == null) return;
        config.onBuild(owner);
    }

    private static class BuildingConfig {

        private List<ConfigurationSection> extraShopItems = new ArrayList<>();
        private List<String> customItemGives = new ArrayList<>();
        private List<ItemStack> itemStackGives = new ArrayList<>();
        private List<PotionEffect> potionEffects = new ArrayList<>();
        private Map<String, Integer> upgrades = new HashMap<>();

        public BuildingConfig(ConfigurationSection config) {
            if (config.contains("shop")) extraShopItems = ConfigUtils.getConfigList(config, "shop");
            if (config.contains("custom_items")) customItemGives = config.getStringList("custom_items");

            if (config.contains("items")) {
                ConfigurationSection items = config.getConfigurationSection("items");
                itemStackGives.addAll(items.getKeys(false).stream()
                        .map(unusedName -> ConfigUtils.getItemStack(items, unusedName)).collect(Collectors.toList()));
            }

            if (config.contains("potion_effects")) {
                ConfigurationSection potions = config.getConfigurationSection("potion_effects");

                for (String potionName : potions.getKeys(false)) {
                    PotionEffectType potionEffectType = PotionEffectType.getByName(potionName);

                    potionEffects
                            .add(new PotionEffect(potionEffectType, Integer.MAX_VALUE, potions.getInt(potionName) - 1));
                }
            }

            if(config.contains("upgrades")) {
                ConfigurationSection upgrades = config.getConfigurationSection("upgrades");

                for(String upgradeName : upgrades.getKeys(false)) {
                    this.upgrades.put(upgradeName, upgrades.getInt(upgradeName));
                }
            }
        }

        public void onShopOpen(ShopOpenEvent event) {
            event.getShop().loadFromConfig(extraShopItems);
        }

        public void onBuild(User user) {
            PlayerInventory inv = user.getInventory();

            for (String customItemName : customItemGives) {
                CustomItem customItem = user.getGameGroup().getCustomItem(customItemName);
                if (!InventoryUtils.containsIdentifier(inv, customItem.getIdentifier())) {
                    inv.addItem(customItem.createForUser(user));
                }
            }

            for (ItemStack item : itemStackGives) {
                inv.addItem(item.clone());
            }

            potionEffects.forEach(user::addPotionEffect);

            for(Map.Entry<String, Integer> upgrade : upgrades.entrySet()) {
                if(user.getUpgradeLevel(upgrade.getKey()) >= upgrade.getValue()) continue;

                user.setUpgradeLevel(upgrade.getKey(), upgrade.getValue());
            }
        }
    }
}
