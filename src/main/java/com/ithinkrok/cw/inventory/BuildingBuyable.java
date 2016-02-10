package com.ithinkrok.cw.inventory;

import com.ithinkrok.minigames.base.inventory.ItemBuyable;
import com.ithinkrok.minigames.base.inventory.event.BuyablePurchaseEvent;
import com.ithinkrok.minigames.base.inventory.event.CalculateItemForUserEvent;
import com.ithinkrok.minigames.base.util.InventoryUtils;
import com.ithinkrok.util.config.Config;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

/**
 * Created by paul on 10/01/16.
 */
public class BuildingBuyable extends ItemBuyable {

    protected String buildingName;

    protected String displayLoreLocale;
    protected String itemLoreLocale;
    protected String teamPurchaseLocale;

    public BuildingBuyable(ItemStack baseDisplay) {
        super(baseDisplay);
    }

    @Override
    public void configure(Config config) {
        super.configure(config);

        Material buildBlockMaterial = Material.getMaterial(config.getString("material", "LAPIS_ORE"));
        int amount = config.getInt("amount", 1);
        buildingName = config.getString("building_name");

        ItemStack item = InventoryUtils.createItemWithNameAndLore(buildBlockMaterial, amount, 0, buildingName);

        baseDisplay = item.clone();
        purchase = item.clone();

        displayLoreLocale = config.getString("display_lore_locale", "building_buyable.lore.display");
        itemLoreLocale = config.getString("item_lore_locale", "building_buyable.lore.item");
        teamPurchaseLocale = config.getString("team_purchase_locale", "building_buyable.purchase");

    }

    @Override
    public void onCalculateItem(CalculateItemForUserEvent event) {
        ItemStack display = event.getDisplay();

        String displayLore = event.getUser().getLanguageLookup().getLocale(displayLoreLocale, buildingName);
        display = InventoryUtils.addLore(display, displayLore);

        event.setDisplay(display);

        super.onCalculateItem(event);
    }

    @Override
    public boolean onPurchase(BuyablePurchaseEvent event) {
        ItemStack item = purchase.clone();

        String itemLore = event.getUser().getLanguageLookup().getLocale(itemLoreLocale, buildingName);
        item = InventoryUtils.addLore(item, itemLore);

        if(!giveUserItem(event.getUser(), item)) return false;

        event.getUser().getTeam().sendLocale(teamPurchaseLocale, event.getUser().getFormattedName(), buildingName);

        return true;
    }
}
