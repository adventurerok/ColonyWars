package com.ithinkrok.minigames.user;

import com.ithinkrok.minigames.User;
import com.ithinkrok.minigames.event.user.game.UserUpgradeEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by paul on 03/01/16.
 */
public class UpgradeHandler<U extends User> {

    private final U user;
    private final Map<String, Integer> upgradeLevels = new HashMap<>();

    public UpgradeHandler(U user) {
        this.user = user;
    }

    public int getUpgradeLevel(String upgrade) {
        Integer level = upgradeLevels.get(upgrade);

        return level == null ? 0 : level;
    }

    @SuppressWarnings("unchecked")
    public void setUpgradeLevel(String upgrade, int level) {
        int oldLevel = getUpgradeLevel(upgrade);
        if (oldLevel == level && upgradeLevels.containsKey(upgrade)) return;

        upgradeLevels.put(upgrade, level);

        UserUpgradeEvent<U> event = new UserUpgradeEvent<>(user, upgrade, oldLevel, level);

        user.getGameGroup().userEvent(event);
    }

    public void clearUpgrades() {
        upgradeLevels.clear();
    }

}
