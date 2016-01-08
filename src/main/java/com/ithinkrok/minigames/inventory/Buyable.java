package com.ithinkrok.minigames.inventory;

import com.ithinkrok.minigames.inventory.event.BuyablePurchaseEvent;
import com.ithinkrok.minigames.inventory.event.CalculateItemForUserEvent;
import com.ithinkrok.minigames.inventory.event.UserClickItemEvent;
import com.ithinkrok.minigames.metadata.Money;
import com.ithinkrok.minigames.util.SoundEffect;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

/**
 * Created by paul on 08/01/16.
 */
public abstract class Buyable extends ClickableItem {

    private int cost;
    private boolean team;

    private static String teamNoMoneyLocale;
    private static String userNoMoneyLocale;
    private static String cannotBuyLocale;

    public Buyable(ItemStack baseDisplay) {
        super(baseDisplay);
    }

    @Override
    public void configure(ConfigurationSection config) {
        cost = config.getInt("cost");
        team = config.getBoolean("team_purchase");

        teamNoMoneyLocale = config.getString("team_no_money_locale", "buyable.team.no_money");
        userNoMoneyLocale = config.getString("user_no_money_locale", "buyable.user.no_money");
        cannotBuyLocale = config.getString("cannot_buy_locale", "buyable.cannot_buy");
    }

    @Override
    public void onCalculateItem(CalculateItemForUserEvent event) {
        BuyablePurchaseEvent purchaseEvent = new BuyablePurchaseEvent(event.getUser(), event.getInventory(), this);

        if(!canBuy(purchaseEvent)) event.setDisplay(null);
    }

    @Override
    public void onClick(UserClickItemEvent event) {
        Money userMoney = Money.getOrCreate(event.getUser());
        Money teamMoney = null;

        if(team) {
            teamMoney = Money.getOrCreate(event.getUser().getTeam());
            if(userMoney.getMoney() + teamMoney.getMoney() < cost) {
                event.getUser().sendLocale(teamNoMoneyLocale);
                return;
            }
        } else if(!userMoney.hasMoney(cost)) {
            event.getUser().sendLocale(userNoMoneyLocale);
            return;
        }

        BuyablePurchaseEvent purchaseEvent = new BuyablePurchaseEvent(event.getUser(), event.getInventory(), this);

        if(!canBuy(purchaseEvent)) {
            event.getUser().sendLocale(cannotBuyLocale);
            event.getUser().redoInventory();
            return;
        }

        if(!onPurchase(purchaseEvent)) return;

        if(team && teamMoney != null) {
            int teamAmount = Math.min(cost, teamMoney.getMoney());
            int userAmount = cost - teamAmount;

            if(teamAmount > 0) teamMoney.subtractMoney(teamAmount, true);
            if(userAmount > 0) userMoney.subtractMoney(userAmount, true);
        } else {
            userMoney.subtractMoney(cost, true);
        }

        event.getUser().playSound(event.getUser().getLocation(), new SoundEffect(Sound.BLAZE_HIT, 1.0f, 1.0f));
    }

    public boolean buyWithTeamMoney() {
        return team;
    }

    public int getCost() {
        return cost;
    }

    public boolean canBuy(BuyablePurchaseEvent event) {
        return true;
    }


    public abstract boolean onPurchase(BuyablePurchaseEvent event);

}
