package com.ithinkrok.cw.kit;

import com.ithinkrok.cw.event.BuildingBuiltEvent;
import com.ithinkrok.cw.event.ShopOpenEvent;
import com.ithinkrok.cw.metadata.CWTeamStats;
import com.ithinkrok.minigames.api.Kit;
import com.ithinkrok.minigames.api.event.ListenerLoadedEvent;
import com.ithinkrok.minigames.api.event.user.game.UserInGameChangeEvent;
import com.ithinkrok.minigames.api.item.CustomItem;
import com.ithinkrok.minigames.api.user.User;
import com.ithinkrok.minigames.api.util.InventoryUtils;
import com.ithinkrok.minigames.api.util.MinigamesConfigs;
import com.ithinkrok.util.config.Config;
import com.ithinkrok.util.event.CustomEventHandler;
import com.ithinkrok.util.event.CustomListener;
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
public class KitListener implements CustomListener {

    private User owner;

    private final Map<String, BuildingConfig> buildingConfigs = new HashMap<>();

    @CustomEventHandler
    public void onListenerLoaded(ListenerLoadedEvent<User, Kit> event) {
        owner = event.getCreator();

        Config buildings = event.getConfig().getConfigOrNull("buildings");

        for (String buildingName : buildings.getKeys(false)) {
            Config buildingConfig = buildings.getConfigOrNull(buildingName);

            buildingConfigs.put(buildingName, new BuildingConfig(buildingConfig));
        }

        recheckBuildings();
    }

    @CustomEventHandler
    public void onShopOpen(ShopOpenEvent event) {
        BuildingConfig config = buildingConfigs.get(event.getBuilding().getBuildingName());

        if (config == null) return;
        config.onShopOpen(event);
    }

    @CustomEventHandler
    public void onBuildingBuilt(BuildingBuiltEvent event) {
        String buildingName = event.getBuilding().getBuildingName();
        applyBuildingBenefits(buildingName);
    }

    @CustomEventHandler
    public void onUserInGameChange(UserInGameChangeEvent event) {
        if(event.getUser().isInGame()) {
            recheckBuildings();
        }
    }

    public void applyBuildingBenefits(String buildingName) {
        BuildingConfig config = buildingConfigs.get(buildingName);

        if (config == null) return;
        config.onBuild(owner);
    }


    public void recheckBuildings() {
        if(!owner.isInGame() || owner.getTeam() == null) return;

        CWTeamStats teamStats = CWTeamStats.getOrCreate(owner.getTeam());

        for (String building : buildingConfigs.keySet()) {
            if(teamStats.everHadBuilding(building)) {
                applyBuildingBenefits(building);
            }
        }

    }

    private static class BuildingConfig {

        private List<Config> extraShopItems = new ArrayList<>();
        private List<String> customItemGives = new ArrayList<>();
        private final List<ItemStack> itemStackGives = new ArrayList<>();
        private final List<PotionEffect> potionEffects = new ArrayList<>();
        private final Map<String, Integer> upgrades = new HashMap<>();

        public BuildingConfig(Config config) {
            if (config.contains("shop")) extraShopItems = config.getConfigList("shop");
            if (config.contains("custom_items")) customItemGives = config.getStringList("custom_items");

            if (config.contains("items")) {
                Config items = config.getConfigOrNull("items");
                itemStackGives.addAll(items.getKeys(false).stream()
                        .map(unusedName -> MinigamesConfigs.getItemStack(items, unusedName))
                        .collect(Collectors.toList()));
            }

            if (config.contains("potion_effects")) {
                Config potions = config.getConfigOrNull("potion_effects");

                for (String potionName : potions.getKeys(false)) {
                    PotionEffectType potionEffectType = PotionEffectType.getByName(potionName);

                    potionEffects
                            .add(new PotionEffect(potionEffectType, Integer.MAX_VALUE, potions.getInt(potionName) - 1));
                }
            }

            if (config.contains("upgrades")) {
                Config upgrades = config.getConfigOrNull("upgrades");

                for (String upgradeName : upgrades.getKeys(false)) {
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
                if (!InventoryUtils.containsIdentifier(inv, customItem.getName())) {
                    inv.addItem(customItem.createForUser(user));
                }
            }

            for (ItemStack item : itemStackGives) {
                inv.addItem(item.clone());
            }

            potionEffects.forEach(user::addPotionEffect);

            for (Map.Entry<String, Integer> upgrade : upgrades.entrySet()) {
                if (user.getUserVariable(upgrade.getKey()) >= upgrade.getValue()) continue;

                user.setUserVariable(upgrade.getKey(), upgrade.getValue());
            }
        }
    }
}
