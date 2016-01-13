package com.ithinkrok.cw.command;

import com.ithinkrok.minigames.GameState;
import com.ithinkrok.minigames.command.Command;
import com.ithinkrok.minigames.command.CommandSender;
import com.ithinkrok.minigames.command.GameCommandExecutor;

/**
 * Created by paul on 13/01/16.
 */
public class GameStateCommand implements GameCommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command) {
        if(!command.requireGameGroup(sender) || !command.requireArgumentCount(sender, 1)) return false;
        String gameStateName = command.getStringArg(0, null);

        GameState gameState = command.getGameGroup().getGameState(gameStateName);
        if(gameState == null) {
            sender.sendLocale("command.gamestate.unknown", gameStateName);
            return false;
        }

        command.getGameGroup().changeGameState(gameState);
        sender.sendLocale("command.gamestate.changed", gameStateName);
        return true;
    }
}
