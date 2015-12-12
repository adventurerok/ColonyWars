package com.ithinkrok.mccw.inventory;

import com.ithinkrok.mccw.data.Building;
import com.ithinkrok.mccw.data.Team;
import com.ithinkrok.mccw.data.User;
import com.ithinkrok.mccw.event.ItemPurchaseEvent;
import com.ithinkrok.mccw.util.item.InventoryUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.*;

/**
 * Created by paul on 06/11/15.
 * <p>
 * A handler for shops that use the buyable system
 */
public class BuyableInventory implements InventoryHandler {

    private Map<ItemStack, Buyable> stackToBuyable = new LinkedHashMap<>();

    public BuyableInventory(Buyable... items) {
        this(Arrays.asList(items));
    }

    public BuyableInventory(List<Buyable> items) {
        addExtraBuyables(items);
    }

    protected void addExtraBuyables(Buyable...extra){
        addExtraBuyables(Arrays.asList(extra));
    }

    protected void addExtraBuyables(Collection<Buyable> extra){
        for(Buyable item : extra){
            stackToBuyable.put(item.getDisplayItemStack(), item);
        }
    }

    @Override
    public boolean onInventoryClick(ItemStack item, Building building, User user,
                                    Team team) {

        Buyable i = stackToBuyable.get(item);
        if (i != null) {
            if (!i.getBuildingNames().contains(building.getBuildingName())) return false;
            tryBuyItem(i, building, user, team);

            return true;
        }

        return false;
    }

    private void tryBuyItem(Buyable item, Building building, User user, Team team) {
        if (item.buyWithTeamMoney()) {
            if (!InventoryUtils.hasTeamCash(item.getCost(), team, user)) {
                user.sendLocale("purchase.team.no-money");
                return;
            }
        } else {
            if (!user.hasPlayerCash(item.getCost())) {
                user.sendLocale("purchase.no-money");
                return;
            }
        }

        int requiredSlots = item.getMinFreeSlots();

        if (requiredSlots > 0) {
            PlayerInventory inventory = user.getPlayer().getInventory();
            int freeSlots = 0;

            for (int i = 0; i < inventory.getSize(); ++i) {
                ItemStack slot = inventory.getItem(i);

                if (slot != null && slot.getType() != Material.AIR) continue;
                ++freeSlots;
            }

            if (freeSlots < requiredSlots) {
                user.sendLocale("purchase.no-slots");
                return;
            }
        }

        ItemPurchaseEvent event = new ItemPurchaseEvent(building, user, team);

        if (!item.canBuy(event)) {
            user.sendLocale("purchase.cant");
            user.redoShopInventory();
            return;
        }

        item.prePurchase(event);

        if (item.buyWithTeamMoney()) {
            InventoryUtils.payWithTeamCash(item.getCost(), team, user);
        } else user.subtractPlayerCash(item.getCost());

        InventoryUtils.playBuySound(user.getPlayer());
        item.onPurchase(event);
    }

    @Override
    public void addInventoryItems(List<ItemStack> items, Building building, User user,
                                  Team team) {
        for (Buyable buyable : stackToBuyable.values()) {
            if (!buyable.getBuildingNames().contains(building.getBuildingName())) continue;
            if (!buyable.canBuy(new ItemPurchaseEvent(building, user, team))) continue;

            items.add(buyable.getDisplayItemStack());
        }
    }
}
