package com.ithinkrok.mccw.inventory;

import com.ithinkrok.mccw.WarsPlugin;
import com.ithinkrok.mccw.strings.Buildings;
import com.ithinkrok.mccw.util.InventoryUtils;
import com.ithinkrok.mccw.util.SchematicBuilder;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by paul on 05/11/15.
 */
public class ChurchInventory extends BuyableInventory {

    public ChurchInventory(WarsPlugin plugin, FileConfiguration config) {
        super(getBuyables(plugin, config));
    }

    private static List<Buyable> getBuyables(WarsPlugin plugin, FileConfiguration config) {
        List<Buyable> result = new ArrayList<>();

        result.add(new Buyable(InventoryUtils.createItemWithNameAndLore(Material.LAPIS_ORE, 1, 0, Buildings.CATHEDRAL,
                "Replace this church with a Cathedral"), Buildings.CHURCH,
                config.getInt("costs.buildings." + Buildings.CATHEDRAL), true, 1) {


            @Override
            public void onPurchase(ItemPurchaseEvent event) {
                event.getBuildingInfo().remove();

                if (!SchematicBuilder.buildSchematic(plugin, plugin.getSchematicData(Buildings.CATHEDRAL),
                        event.getBuildingInfo().getCenterBlock(), event.getBuildingInfo().getRotation(),
                        event.getBuildingInfo().getTeamColor())) {
                    event.getPlayer().sendMessage("We failed to build a cathedral here. Have the block yourself " +
                            "to find a better place!");

                    event.getPlayerInventory().addItem(InventoryUtils
                            .createItemWithNameAndLore(Material.LAPIS_ORE, 1, 0, Buildings.CATHEDRAL,
                                    "Builds a cathedral when placed!"));
                }
            }

            @Override
            public boolean canBuy(ItemPurchaseEvent event) {
                return true;
            }
        });

        return result;
    }

}
