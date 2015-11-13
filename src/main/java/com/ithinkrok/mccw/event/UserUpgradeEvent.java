package com.ithinkrok.mccw.event;

import com.ithinkrok.mccw.data.User;
import org.bukkit.inventory.PlayerInventory;

/**
 * Created by paul on 13/11/15.
 *
 * An event for when a player gets an upgrade
 */
public class UserUpgradeEvent {

    private User user;
    private String upgradeName;
    private int upgradeLevel;

    public UserUpgradeEvent(User user, String upgradeName, int upgradeLevel) {
        this.user = user;
        this.upgradeName = upgradeName;
        this.upgradeLevel = upgradeLevel;
    }

    public User getUser() {
        return user;
    }

    public String getUpgradeName() {
        return upgradeName;
    }

    public int getUpgradeLevel() {
        return upgradeLevel;
    }

    public PlayerInventory getUserInventory(){
        return user.getPlayer().getInventory();
    }
}
