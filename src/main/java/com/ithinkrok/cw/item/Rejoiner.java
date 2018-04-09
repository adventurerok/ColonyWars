package com.ithinkrok.cw.item;

import com.ithinkrok.cw.command.CWCommand;
import com.ithinkrok.minigames.api.event.user.world.UserInteractEvent;
import com.ithinkrok.util.event.CustomEventHandler;
import com.ithinkrok.util.event.CustomListener;

public class Rejoiner implements CustomListener {

    @CustomEventHandler
    public void onInteract(UserInteractEvent event) {
        CWCommand.UserRejoinEvent rejoin = new CWCommand.UserRejoinEvent(event.getUser());
        rejoin.setCancelled(true);
        rejoin.getGameGroup().userEvent(rejoin);
    }

}
