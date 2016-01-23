package com.ithinkrok.cw.lobbygames;

import com.ithinkrok.minigames.GameGroup;
import com.ithinkrok.minigames.User;
import com.ithinkrok.minigames.event.ListenerLoadedEvent;
import com.ithinkrok.minigames.event.MinigamesEventHandler;
import com.ithinkrok.minigames.event.user.world.UserInteractWorldEvent;
import com.ithinkrok.minigames.map.GameMap;
import com.ithinkrok.minigames.metadata.Money;
import com.ithinkrok.minigames.util.ConfigUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.Listener;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by paul on 23/01/16.
 */
public class ParkourMinigame implements Listener {

    private int maxParkourMoney;
    private Map<Vector, Integer> parkourRuns = new HashMap<>();

    @MinigamesEventHandler
    public void onListenerLoaded(ListenerLoadedEvent<GameGroup, GameMap> event) {
        ConfigurationSection config = event.getConfig();

        maxParkourMoney = config.getInt("max_parkour_money");

        for(ConfigurationSection parkourRun : ConfigUtils.getConfigList(config, "runs")) {
            Vector loc = ConfigUtils.getVector(parkourRun, "");

            parkourRuns.put(loc, config.getInt("reward"));
        }
    }

    @MinigamesEventHandler
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
