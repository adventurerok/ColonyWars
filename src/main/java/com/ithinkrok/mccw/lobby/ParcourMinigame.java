package com.ithinkrok.mccw.lobby;

import com.ithinkrok.mccw.WarsPlugin;
import com.ithinkrok.mccw.data.User;
import com.ithinkrok.mccw.event.UserInteractEvent;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by paul on 18/11/15.
 * <p>
 * Handles the parcour minigame
 */
public class ParcourMinigame extends LobbyMinigameAdapter {

    WarsPlugin plugin;
    private int maxParkourMoney;
    private Map<Vector, Integer> parkourRuns = new HashMap<>();

    public ParcourMinigame(WarsPlugin plugin) {
        this.plugin = plugin;

        maxParkourMoney = plugin.getWarsConfig().getParkourMoneyCap();

        for(ConfigurationSection config : plugin.getWarsConfig().getParkourConfigs()) {
            Vector loc = new Vector(config.getInt("x"), config.getInt("y"), config.getInt("z"));

            parkourRuns.put(loc, config.getInt("reward"));
        }
    }

    @Override
    public boolean onUserInteract(UserInteractEvent event) {
        return event.hasBlock() && event.isRightClick() &&
                onUserInteractWorld(event.getUser(), event.getClickedBlock());

    }

    public boolean onUserInteractWorld(User user, Block block) {
        Vector location = block.getLocation().toVector();

        Integer reward = parkourRuns.get(location);
        if(reward == null || reward == 0) return false;

        addParkourMoney(user, reward);
        return true;
    }

    private void addParkourMoney(User user, int amount) {
        amount = Math.min(amount, (maxParkourMoney - user.getPlayerCash()) / 2);

        if (amount <= 0) user.sendLocale("minigames.parcour.capped");
        else {
            user.addPlayerCash(amount, false);
            plugin.messageAllLocale("minigames.parcour.winner", user.getFormattedName(), amount);
        }

        user.teleport(plugin.getLobbySpawn());
    }
}
