package com.ithinkrok.mccw.inventory;

import com.ithinkrok.mccw.WarsPlugin;
import com.ithinkrok.mccw.event.ItemPurchaseEvent;
import com.ithinkrok.mccw.strings.Buildings;
import com.ithinkrok.mccw.util.InventoryUtils;
import com.ithinkrok.mccw.util.SchematicBuilder;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionType;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by paul on 07/11/15.
 * <p>
 * An inventory handler for every building inventory
 */
public class OmniInventory extends BuyableInventory {

    public OmniInventory(WarsPlugin plugin, FileConfiguration config) {
        super(getBuyables(plugin, config));
    }

    private static List<Buyable> getBuyables(WarsPlugin plugin, FileConfiguration config) {
        List<Buyable> result = new ArrayList<>();

        addBaseItems(result, config);
        addFarmItems(result, config);
        addChurchItems(result, plugin, config);
        addCathedralItems(result, config);
        addGreenhouseItems(result, config);
        addBlacksmithItems(result, config);
        addScoutTowerItems(result, plugin, config);

        return result;
    }

    private static void addScoutTowerItems(List<Buyable> result, WarsPlugin plugin, FileConfiguration config) {
        int cannonTowerCost = config.getInt("costs.buildings." + Buildings.CANNONTOWER);

        result.add(new Buyable(InventoryUtils.createItemWithNameAndLore(Material.LAPIS_ORE, 1, 0, Buildings.CANNONTOWER,
                "Replace this ScoutTower with a CannonTower"), Buildings.SCOUTTOWER,
                cannonTowerCost, true, 1) {


            @Override
            public void onPurchase(ItemPurchaseEvent event) {
                event.getBuilding().remove();

                if (!SchematicBuilder.buildSchematic(plugin, plugin.getSchematicData(Buildings.CANNONTOWER),
                        event.getBuilding().getCenterBlock(), event.getBuilding().getRotation(),
                        event.getBuilding().getTeamColor())) {
                    event.getUser().message("We failed to build a CannonTower here. Have the block yourself " +
                            "to find a better place!");

                    event.getUserInventory().addItem(InventoryUtils
                            .createItemWithNameAndLore(Material.LAPIS_ORE, 1, 0, Buildings.CANNONTOWER,
                                    "Builds a CannonTower when placed!"));
                }
            }

            @Override
            public boolean canBuy(ItemPurchaseEvent event) {
                return true;
            }
        });
    }

    private static void addBlacksmithItems(List<Buyable> result, FileConfiguration config) {
        int scoutTowerCost = config.getInt("costs.buildings." + Buildings.SCOUTTOWER);
        int wallCost = config.getInt("costs.buildings." + Buildings.WALL);
        int mineCost = config.getInt("costs.buildings." + Buildings.LANDMINE);

        result.add(new BuildingBuyable(Buildings.SCOUTTOWER, Buildings.BLACKSMITH, scoutTowerCost));
        result.add(new BuildingBuyable(Buildings.WALL, Buildings.BLACKSMITH, wallCost, 16, true));
        result.add(new BuildingBuyable(Buildings.LANDMINE, Buildings.BLACKSMITH, mineCost));
    }

    private static void addBaseItems(List<Buyable> result, FileConfiguration config) {
        int farmCost = config.getInt("costs.buildings." + Buildings.FARM);
        int lumbermillCost = config.getInt("costs.buildings." + Buildings.LUMBERMILL);
        int blacksmithCost = config.getInt("costs.buildings." + Buildings.BLACKSMITH);
        int magetowerCost = config.getInt("costs.buildings." + Buildings.MAGETOWER);
        int churchCost = config.getInt("costs.buildings." + Buildings.CHURCH);
        int greenhouseCost = config.getInt("costs.buildings." + Buildings.GREENHOUSE);

        result.add(new BuildingBuyable(Buildings.FARM, Buildings.BASE, farmCost));
        result.add(new BuildingBuyableWithFarm(Buildings.LUMBERMILL, Buildings.BASE, lumbermillCost));
        result.add(new BuildingBuyableWithFarm(Buildings.BLACKSMITH, Buildings.BASE, blacksmithCost));
        result.add(new BuildingBuyableWithFarm(Buildings.MAGETOWER, Buildings.BASE, magetowerCost));
        result.add(new BuildingBuyableWithFarm(Buildings.CHURCH, Buildings.BASE, churchCost));
        result.add(new BuildingBuyableWithFarm(Buildings.GREENHOUSE, Buildings.BASE, greenhouseCost));

    }

    private static void addFarmItems(List<Buyable> result, FileConfiguration config) {
        int rawPotatoAmount = config.getInt("amounts.farm.rawPotato");
        int cookieAmount = config.getInt("amounts.farm.cookie");
        int rawBeefAmount = config.getInt("amounts.farm.rawBeef");
        int bakedPotatoAmount = config.getInt("amounts.farm.bakedPotato");
        int cookedBeefAmount = config.getInt("amounts.farm.cookedBeef");
        int goldenAppleAmount = config.getInt("amounts.farm.goldenApple");

        result.add(new ItemBuyable(new ItemStack(Material.POTATO_ITEM, rawPotatoAmount), Buildings.FARM,
                config.getInt("costs.farm.rawPotato") * rawPotatoAmount, true));

        result.add(new ItemBuyable(new ItemStack(Material.COOKIE, cookieAmount), Buildings.FARM,
                config.getInt("costs.farm.cookie") * cookieAmount, true));

        result.add(new ItemBuyable(new ItemStack(Material.RAW_BEEF, rawBeefAmount), Buildings.FARM,
                config.getInt("costs.farm.rawBeef") * rawBeefAmount, true));

        result.add(new ItemBuyable(new ItemStack(Material.BAKED_POTATO, bakedPotatoAmount), Buildings.FARM,
                config.getInt("costs.farm.bakedPotato") * bakedPotatoAmount, true));

        result.add(new ItemBuyable(new ItemStack(Material.COOKED_BEEF, cookedBeefAmount), Buildings.FARM,
                config.getInt("costs.farm.cookedBeef") * cookedBeefAmount, true));

        result.add(new ItemBuyable(new ItemStack(Material.GOLDEN_APPLE, goldenAppleAmount), Buildings.FARM,
                config.getInt("costs.farm.goldenApple") * goldenAppleAmount, true));
    }

    private static void addChurchItems(List<Buyable> result, WarsPlugin plugin, FileConfiguration config) {
        result.add(new Buyable(InventoryUtils.createItemWithNameAndLore(Material.LAPIS_ORE, 1, 0, Buildings.CATHEDRAL,
                "Replace this church with a Cathedral"), Buildings.CHURCH,
                config.getInt("costs.buildings." + Buildings.CATHEDRAL), true, 1) {


            @Override
            public void onPurchase(ItemPurchaseEvent event) {
                event.getBuilding().remove();

                if (!SchematicBuilder.buildSchematic(plugin, plugin.getSchematicData(Buildings.CATHEDRAL),
                        event.getBuilding().getCenterBlock(), event.getBuilding().getRotation(),
                        event.getBuilding().getTeamColor())) {
                    event.getUser().message("We failed to build a cathedral here. Have the block yourself " +
                            "to find a better place!");

                    event.getUserInventory().addItem(InventoryUtils
                            .createItemWithNameAndLore(Material.LAPIS_ORE, 1, 0, "PlayerCathedral",
                                    "Builds a Cathedral when placed!"));
                }
            }

            @Override
            public boolean canBuy(ItemPurchaseEvent event) {
                return true;
            }
        });

        result.add(new ItemBuyable(InventoryUtils.createPotion(PotionType.INSTANT_HEAL, 1, true, false, 32),
                Buildings.CHURCH, config.getInt("costs.church.healingPotion32"), true));
    }

    private static void addCathedralItems(List<Buyable> result, FileConfiguration config) {
        result.add(new ItemBuyable(InventoryUtils.createPotion(PotionType.INSTANT_HEAL, 1, true, false, 32),
                Buildings.CATHEDRAL, config.getInt("costs.cathedral.healingPotion32"), true));
    }

    private static void addGreenhouseItems(List<Buyable> result, FileConfiguration config) {
        int axeCost = config.getInt("costs.greenhouse.axe");

        result.add(new ItemBuyable(InventoryUtils.createItemWithNameAndLore(Material.STONE_AXE, 1, 0, "The Mighty Axe"),
                Buildings.GREENHOUSE, axeCost, false));
    }

    private static class BuildingBuyableWithFarm extends BuildingBuyable {

        public BuildingBuyableWithFarm(String buildingName, String purchaseFromBuilding, int cost) {
            super(buildingName, purchaseFromBuilding, cost);
        }

        @Override
        public boolean canBuy(ItemPurchaseEvent event) {
            return super.canBuy(event) && event.getTeam().getBuildingCount(Buildings.FARM) > 0;
        }
    }
}
