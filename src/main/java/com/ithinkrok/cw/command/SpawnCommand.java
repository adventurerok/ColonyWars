package com.ithinkrok.cw.command;

import com.ithinkrok.minigames.base.event.MinigamesCommandEvent;
import com.ithinkrok.util.event.CustomEventHandler;
import com.ithinkrok.util.event.CustomListener;
import org.bukkit.Location;

/**
 * Created by paul on 19/02/16.
 */
public class SpawnCommand implements CustomListener {

    @CustomEventHandler
    public void onCommand(MinigamesCommandEvent event) {
        if(!event.getCommand().requireUser(event.getCommandSender())) return;

        if(!event.getCommand().getGameGroup().getCurrentGameState().getName().equals("lobby")) {
            event.getCommandSender().sendLocale("command.spawn.not_lobby");
            return;
        }

        Location spawn = event.getCommand().getGameGroup().getCurrentMap().getSpawn();

        event.getCommand().getUser().teleport(spawn);

        event.getCommandSender().sendLocale("command.spawn.teleported");
    }
}
