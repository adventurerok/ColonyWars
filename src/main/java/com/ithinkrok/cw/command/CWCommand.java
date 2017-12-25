
package com.ithinkrok.cw.command;

import com.ithinkrok.minigames.api.command.MinigamesCommand;
import com.ithinkrok.minigames.api.command.MinigamesCommandSender;
import com.ithinkrok.minigames.api.event.MinigamesCommandEvent;
import com.ithinkrok.minigames.api.event.user.BaseUserEvent;
import com.ithinkrok.minigames.api.user.User;
import com.ithinkrok.minigames.api.util.InventoryUtils;
import com.ithinkrok.minigames.util.command.DebugCommand;
import com.ithinkrok.util.event.CustomEventHandler;
import org.bukkit.Material;
import org.bukkit.event.Cancellable;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by paul on 13/01/16.
 */
public class CWCommand extends DebugCommand {

    public CWCommand() {
        addSubExecutor("building", "command.cw.building", this::buildingCommand);
        addSubExecutor("rejoin", "command.cw.rejoin", this::rejoinCommand);
    }


    private boolean buildingCommand(MinigamesCommandSender sender, MinigamesCommand command) {
        if(!command.requireUser(sender)) return true;
        if(!command.requireArgumentCount(sender, 1)) return true;

        String buildingName = command.getStringArg(0, null);
        if(command.getGameGroup().getSchematic(buildingName) == null) {
            sender.sendLocale("command.cw.building.unknown", buildingName);
            return true;
        }

        int amount = command.getIntArg(1, 16);

        boolean instaBuild = command.getBooleanParam("insta", false) || command.getBooleanParam("i", false);

        ItemStack item;
        if (!instaBuild) {
            item = InventoryUtils.createItemWithNameAndLore(Material.LAPIS_ORE, amount, 0, buildingName);
        } else {
            item = InventoryUtils.createItemWithNameAndLore(Material.LAPIS_ORE, amount, 0, buildingName, "Instabuild");
        }

        command.getUser().getInventory().addItem(item);

        return true;
    }



    private boolean rejoinCommand(MinigamesCommandSender sender, MinigamesCommand command) {
        if(!command.requireUser(sender)) return true;

        UserRejoinEvent event = new UserRejoinEvent(command.getUser());
        event.setCancelled(true);
        command.getGameGroup().userEvent(event);

        if(event.isCancelled()) sender.sendLocale("command.cw.rejoin.failure");
        else sender.sendLocale("command.cw.rejoin.success", command.getUser().getFormattedName());

        return true;
    }

    public static class UserRejoinEvent extends BaseUserEvent implements Cancellable {

        private boolean cancelled;

        public UserRejoinEvent(User user) {
            super(user);
        }

        @Override
        public boolean isCancelled() {
            return cancelled;
        }

        @Override
        public void setCancelled(boolean cancel) {
            cancelled = cancel;
        }
    }

    /**
     * Created by paul on 12/01/16.
     */
    private interface SubCommandExecutor {

        boolean onCommand(MinigamesCommandSender sender, MinigamesCommand command);
    }
}
