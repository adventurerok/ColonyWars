package com.ithinkrok.minigames.metadata;

import com.ithinkrok.minigames.User;
import com.ithinkrok.minigames.event.game.GameStateChangedEvent;
import org.bukkit.configuration.ConfigurationSection;

/**
 * Created by paul on 06/01/16.
 */
public class UserMoney extends Money {

    private final User user;

    private String removeGameState;
    private String addMoneyLocale;
    private String subtractMoneyLocale;
    private String newMoneyLocale;

    private int messageLevel = 0;

    public UserMoney(User user) {
        this.user = user;

        ConfigurationSection config = user.getGameGroup().getSharedObject("user_money_metadata");

        removeGameState = config.getString("remove_gamestate");
        addMoneyLocale = config.getString("add_locale");
        subtractMoneyLocale = config.getString("subtract_locale");
        newMoneyLocale = config.getString("new_locale");
    }

    private int money;

    @Override
    public int getMoney() {
        return money;
    }

    public void setMessageLevel(int messageLevel) {
        this.messageLevel = messageLevel;
    }

    @Override
    public boolean hasMoney(int amount) {
        return money >= amount;
    }

    private boolean messageUser(boolean message) {
        return (message ? 1 : 0) + messageLevel >= 2;
    }

    @Override
    public void addMoney(int amount, boolean message) {
        money += amount;

        //TODO update scoreboard
        if(!messageUser(message)) return;
        user.sendLocale(addMoneyLocale, amount);
        user.sendLocale(newMoneyLocale, amount);
    }

    @Override
    public boolean subtractMoney(int amount, boolean message) {
        if(!hasMoney(amount)) return false;

        money -= amount;

        //TODO update scoreboard
        if(!messageUser(message)) return true;
        user.sendLocale(subtractMoneyLocale, amount);
        user.sendLocale(newMoneyLocale, amount);

        return true;
    }

    @Override
    public boolean removeOnGameStateChange(GameStateChangedEvent event) {
        return false;
    }
}
