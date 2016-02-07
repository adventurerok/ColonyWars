package com.ithinkrok.cw.lobbygames;

import com.ithinkrok.minigames.base.GameGroup;
import com.ithinkrok.minigames.base.User;
import com.ithinkrok.minigames.base.event.ListenerLoadedEvent;
import com.ithinkrok.minigames.base.event.user.world.UserInteractWorldEvent;
import com.ithinkrok.minigames.base.map.GameMap;
import com.ithinkrok.minigames.base.metadata.Money;
import com.ithinkrok.msm.common.util.ConfigUtils;
import com.ithinkrok.util.event.CustomEventHandler;
import com.ithinkrok.util.event.CustomListener;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by paul on 23/01/16.
 */
public class ParkourMinigame implements CustomListener {

    private int maxParkourMoney;
    private final Map<Vector, Integer> parkourRuns = new HashMap<>();

    @CustomEventHandler
    public void onListenerLoaded(ListenerLoadedEvent<GameGroup, GameMap> event) {
        ConfigurationSection config = event.getConfig();

        maxParkourMoney = config.getInt("max_parkour_money");

        for(ConfigurationSection parkourRun : ConfigUtils.getConfigList(config, "runs")) {
            Vector loc = ConfigUtils.getVector(parkourRun, "");

            parkourRuns.put(loc, parkourRun.getInt("reward"));
        }
    }

    @CustomEventHandler
    public void onUserInteractWorld(UserInteractWorldEvent event) {
        if(!event.hasBlock()) return;

        Vector location = event.getClickedBlock().getLocation().toVector();

        Integer reward = parkourRuns.get(location);
        if(reward == null || reward == 0) return;

        addParkourMoney(event.getUser(), reward);
    }

    private void addParkourMoney(User user, int amount) {
        Money userMoney = Money.getOrCreate(user);

        amount = Math.min(amount, (maxParkourMoney - userMoney.getMoney()) / 2);

        if (amount <= 0) user.sendLocale("parkour.capped");
        else {
            userMoney.addMoney(amount, false);
            user.getGameGroup().sendLocale("parkour.winner", user.getFormattedName(), amount);
        }

        user.teleport(user.getMap().getSpawn());
    }
}
