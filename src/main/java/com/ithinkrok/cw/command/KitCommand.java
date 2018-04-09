package com.ithinkrok.cw.command;

import com.ithinkrok.minigames.api.Kit;
import com.ithinkrok.minigames.api.command.MinigamesCommand;
import com.ithinkrok.minigames.api.command.MinigamesCommandSender;
import com.ithinkrok.minigames.api.event.MinigamesCommandEvent;
import com.ithinkrok.minigames.api.user.User;
import com.ithinkrok.util.event.CustomEventHandler;
import com.ithinkrok.util.event.CustomListener;

public class KitCommand implements CustomListener {

    @CustomEventHandler
    public void onCommand(MinigamesCommandEvent event) {
        MinigamesCommandSender sender = event.getCommandSender();
        MinigamesCommand command = event.getCommand();

        if (!command.requireArgumentCount(sender, 1)) return;
        if(!command.requireUser(sender) || !command.requireOthersPermission(sender, "mccw.kit.others")) return;

        User user = command.getUser();
        String kitName = command.getStringArg(0, null);

        Kit kit = user.getGameGroup().getKit(kitName);
        if (kit == null) {
            user.sendLocale("command.debug.kit.invalid", kitName);
            return;
        }

        user.setKit(kit);
        user.sendLocale("command.debug.kit.success", kitName);
    }

}
