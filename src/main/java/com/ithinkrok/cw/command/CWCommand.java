
package com.ithinkrok.cw.command;

import com.ithinkrok.minigames.api.Kit;
import com.ithinkrok.minigames.api.command.MinigamesCommand;
import com.ithinkrok.minigames.api.command.MinigamesCommandSender;
import com.ithinkrok.minigames.api.event.MinigamesCommandEvent;
import com.ithinkrok.minigames.api.event.user.UserEvent;
import com.ithinkrok.minigames.api.item.CustomItem;
import com.ithinkrok.minigames.api.team.Team;
import com.ithinkrok.minigames.api.user.User;
import com.ithinkrok.minigames.api.util.InventoryUtils;
import com.ithinkrok.util.math.ExpressionCalculator;
import com.ithinkrok.minigames.util.metadata.Money;
import com.ithinkrok.util.event.CustomEventHandler;
import com.ithinkrok.util.event.CustomListener;
import org.bukkit.Material;
import org.bukkit.event.Cancellable;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by paul on 13/01/16.
 */
public class CWCommand implements CustomListener {

    private final Map<String, SubCommandExecutor> subExecutors = new HashMap<>();


    public CWCommand() {
        subExecutors.put("money", this::moneyCommand);
        subExecutors.put("building", this::buildingCommand);
        subExecutors.put("team", this::teamCommand);
        subExecutors.put("custom", this::customCommand);
        subExecutors.put("level", this::levelCommand);
        subExecutors.put("kit", this::kitCommand);
        subExecutors.put("rejoin", this::rejoinCommand);
    }

    @CustomEventHandler
    public void onCommand(MinigamesCommandEvent event) {
        MinigamesCommandSender sender = event.getCommandSender();
        MinigamesCommand command = event.getCommand();

        if(!command.requireArgumentCount(sender, 1)){
            event.setValidCommand(false);
            return;
        }

        MinigamesCommand subCommand = command.subCommand();

        if(!subExecutors.containsKey(subCommand.getCommand())) {
            sender.sendLocale("command.cw.unknown", subCommand.getCommand());
            return;
        }

        if(!MinigamesCommand.requirePermission(sender, "command.cw." + subCommand.getCommand())) return;

        event.setValidCommand(subExecutors.get(subCommand.getCommand()).onCommand(sender, subCommand));
    }

    private boolean moneyCommand(MinigamesCommandSender sender, MinigamesCommand command) {
        if(!command.requireUser(sender)) return true;

        int amount = command.getIntArg(0, 10000);

        Money userMoney = Money.getOrCreate(command.getUser());
        userMoney.addMoney(amount, true);

        if(command.getUser().getTeam() == null) return true;
        int teamAmount = command.getIntArg(1, amount);

        Money teamMoney = Money.getOrCreate(command.getUser().getTeam());
        teamMoney.addMoney(teamAmount, true);

        return true;
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

    private boolean teamCommand(MinigamesCommandSender sender, MinigamesCommand command) {
        if(!command.requireUser(sender)) return true;
        if(!command.requireArgumentCount(sender, 1)) return false;

        Team team = command.getGameGroup().getTeam(command.getStringArg(0, null));
        if(team == null) {
            sender.sendLocale("command.cw.team.unknown", command.getStringArg(0, null));
            return true;
        }

        command.getUser().setTeam(team);
        sender.sendLocale("command.cw.team.success", command.getUser().getFormattedName(), team.getFormattedName());

        return true;
    }

    private boolean customCommand(MinigamesCommandSender sender, MinigamesCommand command) {
        if(!command.requireUser(sender)) return true;
        if(!command.requireArgumentCount(sender, 1)) return false;

        CustomItem item = command.getUser().getGameGroup().getCustomItem(command.getStringArg(0, null));
        if(item == null) {
            sender.sendLocale("command.cw.custom.unknown", command.getStringArg(0, null));
            return true;
        }

        command.getUser().getInventory().addItem(item.createForUser(command.getUser()));
        sender.sendLocale("command.cw.custom.success", command.getUser().getFormattedName(), command.getStringArg(0,
                null));

        return true;
    }

    private boolean levelCommand(MinigamesCommandSender sender, MinigamesCommand command) {
        if(!command.requireUser(sender)) return true;
        if(!command.requireArgumentCount(sender, 2)) return false;

        String upgrade = command.getStringArg(0, null);
        int level = (int) new ExpressionCalculator(command.getStringArg(1, "0")).calculate(command.getUser()
                .getUserVariables());

        command.getUser().setUserVariable(upgrade, level);
        sender.sendLocale("command.cw.level.success", command.getUser().getFormattedName(), upgrade, level);

        return true;
    }

    private boolean kitCommand(MinigamesCommandSender sender, MinigamesCommand command) {
        if(!command.requireUser(sender)) return true;
        if(!command.requireArgumentCount(sender, 1)) return false;

        Kit kit = command.getGameGroup().getKit(command.getStringArg(0, null));
        if(kit == null) {
            sender.sendLocale("command.cw.kit.unknown", command.getStringArg(0, null));
            return true;
        }

        command.getUser().setKit(kit);

        sender.sendLocale("command.cw.kit.success", command.getUser().getFormattedName(), kit.getFormattedName());

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

    public static class UserRejoinEvent extends UserEvent implements Cancellable {

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
