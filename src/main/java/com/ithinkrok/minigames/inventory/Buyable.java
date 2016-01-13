package com.ithinkrok.minigames.inventory;

import com.ithinkrok.minigames.User;
import com.ithinkrok.minigames.inventory.event.BuyablePurchaseEvent;
import com.ithinkrok.minigames.inventory.event.CalculateItemForUserEvent;
import com.ithinkrok.minigames.inventory.event.UserClickItemEvent;
import com.ithinkrok.minigames.lang.LanguageLookup;
import com.ithinkrok.minigames.metadata.Money;
import com.ithinkrok.minigames.util.InventoryUtils;
import com.ithinkrok.minigames.util.SoundEffect;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

/**
 * Created by paul on 08/01/16.
 */
public abstract class Buyable extends ClickableItem {

    private static String teamNoMoneyLocale;
    private static String userNoMoneyLocale;
    private static String cannotBuyLocale;
    private static String userPayTeamLocale;
    private static String teamDescriptionLocale;
    private static String userDescriptionLocale;
    private int cost;
    private boolean team;

    public Buyable(ItemStack baseDisplay) {
        super(baseDisplay);
    }

    @Override
    public void configure(ConfigurationSection config) {
        cost = config.getInt("cost");
        team = config.getBoolean("team");

        teamNoMoneyLocale = config.getString("team_no_money_locale", "buyable.team.no_money");
        userNoMoneyLocale = config.getString("user_no_money_locale", "buyable.user.no_money");
        cannotBuyLocale = config.getString("cannot_buy_locale", "buyable.cannot_buy");
        userPayTeamLocale = config.getString("user_pay_team_locale", "buyable.user.pay_team");
        teamDescriptionLocale = config.getString("team_description_locale", "buyable.team.description");
        userDescriptionLocale = config.getString("user_description_locale", "buyable.user.description");
    }

    @Override
    public void onCalculateItem(CalculateItemForUserEvent event) {
        BuyablePurchaseEvent purchaseEvent = new BuyablePurchaseEvent(event.getUser(), event.getInventory(), this);

        if (!canBuy(purchaseEvent)) {
            event.setDisplay(null);
            return;
        }

        Money userMoney = Money.getOrCreate(event.getUser());

        int cost = getCost(event.getUser());
        boolean hasMoney = true;

        if (team) {
            Money teamMoney = Money.getOrCreate(event.getUser().getTeam());
            if (userMoney.getMoney() + teamMoney.getMoney() < cost) hasMoney = false;
        } else if (!userMoney.hasMoney(cost)) hasMoney = false;

        String costString = (hasMoney ? ChatColor.GREEN : ChatColor.RED) + Integer.toString(cost);
        LanguageLookup lookup = event.getUser().getLanguageLookup();

        ItemStack display = event.getDisplay();

        if(team) {
            display = InventoryUtils.addLore(display, lookup.getLocale(teamDescriptionLocale, costString));
        } else {
            display = InventoryUtils.addLore(display, lookup.getLocale(userDescriptionLocale, costString));
        }

        event.setDisplay(display);
    }

    public boolean canBuy(BuyablePurchaseEvent event) {
        return true;
    }

    public int getCost(User user) {
        return cost;
    }

    @Override
    public void onClick(UserClickItemEvent event) {
        Money userMoney = Money.getOrCreate(event.getUser());
        Money teamMoney = null;

        int cost = getCost(event.getUser());

        if (team) {
            teamMoney = Money.getOrCreate(event.getUser().getTeam());
            if (userMoney.getMoney() + teamMoney.getMoney() < cost) {
                event.getUser().sendLocale(teamNoMoneyLocale);
                return;
            }
        } else if (!userMoney.hasMoney(cost)) {
            event.getUser().sendLocale(userNoMoneyLocale);
            return;
        }

        BuyablePurchaseEvent purchaseEvent = new BuyablePurchaseEvent(event.getUser(), event.getInventory(), this);

        if (!canBuy(purchaseEvent)) {
            event.getUser().sendLocale(cannotBuyLocale);
            event.getUser().redoInventory();
            return;
        }

        if (!onPurchase(purchaseEvent)) return;

        if (team && teamMoney != null) {
            int teamAmount = Math.min(cost, teamMoney.getMoney());
            int userAmount = cost - teamAmount;

            if (teamAmount > 0) teamMoney.subtractMoney(teamAmount, true);
            if (userAmount > 0) {
                userMoney.subtractMoney(userAmount, true);
                event.getUser().sendLocale(userPayTeamLocale, userAmount);
            }
        } else {
            userMoney.subtractMoney(cost, true);
        }

        event.getUser().playSound(event.getUser().getLocation(), new SoundEffect(Sound.BLAZE_HIT, 1.0f, 1.0f));
    }

    public abstract boolean onPurchase(BuyablePurchaseEvent event);

    public boolean buyWithTeamMoney() {
        return team;
    }

}
