package com.ithinkrok.cw.inventory;

import com.ithinkrok.cw.metadata.CWTeamStats;
import com.ithinkrok.minigames.api.Kit;
import com.ithinkrok.minigames.api.inventory.event.CalculateItemForUserEvent;
import com.ithinkrok.minigames.api.schematic.Schematic;
import com.ithinkrok.minigames.api.util.InventoryUtils;
import com.ithinkrok.minigames.util.inventory.ItemBuyable;
import com.ithinkrok.minigames.util.inventory.event.BuyablePurchaseEvent;
import com.ithinkrok.util.config.Config;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * Created by paul on 10/01/16.
 */
public class BuildingBuyable extends ItemBuyable {

    protected String buildingName;

    protected String displayLoreLocale;
    protected String itemLoreLocale;
    protected String teamPurchaseLocale;
    protected String usefulForLocale;
    protected String usefulCommaLocale;

    public BuildingBuyable(ItemStack baseDisplay, int slot) {
        super(baseDisplay, slot);
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
        usefulForLocale = config.getString("useful_for_locale", "building_buyable.useful_for");
        usefulCommaLocale = config.getString("useful_comma_locale", "building_buyable.useful_comma");

    }

    @Override
    public void onCalculateItem(CalculateItemForUserEvent event) {
        ItemStack display = event.getDisplay();

        String displayLore = event.getUser().getLanguageLookup().getLocale(displayLoreLocale, buildingName);
        display = InventoryUtils.addLore(display, displayLore);

        Schematic schem = event.getGameGroup().getSchematic(buildingName);

        List<String> useful = schem.getConfig().getStringList("useful_for");

        if(!useful.isEmpty()) {
            StringBuilder usefulMessage = new StringBuilder(event.getGameGroup().getLocale(usefulForLocale));
            usefulMessage.append(' ');

            boolean addComma = false;

            for(String kitName : useful) {
                if(!addComma) addComma = true;
                else {
                    usefulMessage.append(event.getGameGroup().getLocale(usefulCommaLocale)).append(' ');
                }

                boolean hasKit = event.getUser().getTeam().hasPlayerOfKit(kitName);

                if(hasKit) {
                    usefulMessage.append(ChatColor.GREEN);
                } else {
                    usefulMessage.append(ChatColor.RED);
                }

                Kit kit = event.getGameGroup().getKit(kitName);
                usefulMessage.append(kit.getFormattedName());
            }

            display = InventoryUtils.addLore(display, usefulMessage.toString());
        }

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

        //Increment team buildings in inventory counter
        CWTeamStats teamStats = CWTeamStats.getOrCreate(event.getUser().getTeam());
        teamStats.addBuildingInventoryCount(buildingName, item.getAmount());

        return true;
    }
}
