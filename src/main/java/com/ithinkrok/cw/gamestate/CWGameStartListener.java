package com.ithinkrok.cw.gamestate;

import com.ithinkrok.cw.command.CWCommand;
import com.ithinkrok.cw.metadata.CWTeamStats;
import com.ithinkrok.cw.metadata.StatsHolder;
import com.ithinkrok.cw.scoreboard.CWScoreboardHandler;
import com.ithinkrok.minigames.api.GameGroup;
import com.ithinkrok.minigames.api.GameState;
import com.ithinkrok.minigames.api.event.ListenerLoadedEvent;
import com.ithinkrok.minigames.api.event.user.game.UserChangeTeamEvent;
import com.ithinkrok.minigames.api.user.UserVariableHandler;
import com.ithinkrok.minigames.api.user.User;
import com.ithinkrok.minigames.api.util.MinigamesConfigs;
import com.ithinkrok.minigames.util.gamestate.SimpleGameStartListener;
import com.ithinkrok.minigames.util.metadata.Money;
import com.ithinkrok.minigames.util.metadata.UserMoney;
import com.ithinkrok.util.config.Config;
import com.ithinkrok.util.event.CustomEventHandler;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.inventory.ItemStack;

/**
 * Created by paul on 05/01/16.
 */
public class CWGameStartListener extends SimpleGameStartListener {

    int rejoinMinMoney;
    int rejoinPotAmount;
    ItemStack rejoinChurchPot;
    ItemStack rejoinCathedralPot;

    @CustomEventHandler
    @Override
    public void onListenerLoaded(ListenerLoadedEvent<GameGroup, GameState> event) {
        super.onListenerLoaded(event);

        Config config = event.getConfigOrEmpty();
        rejoinMinMoney = config.getInt("rejoin.min_money");
        rejoinPotAmount = config.getInt("rejoin.pot_amount");
        rejoinChurchPot = MinigamesConfigs.getItemStack(config, "rejoin.church_pots");
        rejoinCathedralPot = MinigamesConfigs.getItemStack(config, "rejoin.cathedral_pots");
    }


    @CustomEventHandler
    public void onUserRejoin(CWCommand.UserRejoinEvent event) {
        event.setCancelled(false);

        setupUser(event.getUser());

        event.getGameGroup().sendLocale("user.rejoin", event.getUser().getFormattedName());

        Money money = UserMoney.getOrCreate(event.getUser());
        if(!money.hasMoney(rejoinMinMoney)) {
            money.addMoney(rejoinMinMoney - money.getMoney(), false);
        }

        CWTeamStats teamStats = CWTeamStats.getOrCreate(event.getUser().getTeam());

        ItemStack pot = null;
        if(teamStats.getBuildingCount("Cathedral") >= 1) {
            pot = rejoinCathedralPot;
        } else if(teamStats.getBuildingCount("Church") >= 1) {
            pot = rejoinChurchPot;
        }

        if(pot != null) {
            for(int count = 0; count < rejoinPotAmount; ++count) {
                event.getUser().getInventory().addItem(pot.clone());
            }
        }
    }

    protected void setupUser(User user) {
        super.setupUser(user);

        CWTeamStats teamStats = CWTeamStats.getOrCreate(user.getTeam());
        user.teleport(teamStats.getSpawnLocation());

        //Add team variable lookups
        UserVariableHandler upgradeLevels = user.getUserVariables();
        upgradeLevels.addCustomVariableHandler("built", teamStats.getBuildingCountVariablesObject());
        upgradeLevels.addCustomVariableHandler("building_now", teamStats.getBuildingNowCountVariablesObject());
        upgradeLevels.addCustomVariableHandler("buildings_in_inv", teamStats.getBuildingInventoryVariablesObject());

        upgradeLevels.addCustomVariableHandler("buildings", teamStats.getTotalBuildingsVariablesObject());

        user.setGameMode(GameMode.SURVIVAL);
        user.setAllowFlight(false);

        user.giveColoredArmor(user.getTeam().getArmorColor(), true);
        user.setDisplayName(user.getTeam().getChatColor() + user.getName());
        user.setTabListName(user.getTeam().getChatColor() + user.getName());

        user.setScoreboardHandler(new CWScoreboardHandler(user));
        user.updateScoreboard();


        StatsHolder statsHolder = StatsHolder.getOrCreate(user);
        statsHolder.addGame();

        user.showTitle(user.getTeam().getFormattedName(), user.getKit().getFormattedName(), 20, 20, 20);
    }

    @CustomEventHandler
    public void onUserChangeTeam(UserChangeTeamEvent event) {
        Color armorColor = event.getNewTeam() != null ? event.getNewTeam().getArmorColor() : null;
        event.getUser().giveColoredArmor(armorColor, true);
    }
}
