package com.ithinkrok.cw;

import com.ithinkrok.cw.command.*;
import com.ithinkrok.cw.database.UserCategoryStats;
import com.ithinkrok.minigames.Game;
import com.ithinkrok.minigames.MinigamesPlugin;
import com.ithinkrok.minigames.command.GameCommandHandler;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.List;

/**
 * Created by paul on 31/12/15.
 */
public class CWPlugin extends MinigamesPlugin {

    Game minigame;
    GameCommandHandler commandHandler;

    @Override
    public void onEnable() {
        super.onEnable();

        minigame = new Game(this);

        minigame.reloadConfig();
        minigame.registerListeners();

        commandHandler = new GameCommandHandler(minigame);
        commandHandler.addExecutor(new GameStateCommand(), "gamestate");
        commandHandler.addExecutor(new CWCommand(), "colonywars");
        commandHandler.addExecutor(new ToggleMoneyMessageCommand(), "togglemoneymessage");
        commandHandler.addExecutor(new StatsCommand(), "stats");
        commandHandler.addExecutor(new CountdownCommand(), "countdown");
        commandHandler.addExecutor(new TeamChatCommand(), "teamchat");
        commandHandler.addExecutor(new TransferCommand(), "transfer");
    }

    @Override
    public void onDisable() {
        minigame.unload();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return commandHandler.onCommand(sender, command, label, args);
    }

    @Override
    public List<Class<?>> getDatabaseClasses() {
        List<Class<?>> result = super.getDatabaseClasses();

        result.add(UserCategoryStats.class);

        return result;
    }
}
