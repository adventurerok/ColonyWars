package com.ithinkrok.mccw.command.executors;

import com.ithinkrok.mccw.command.CommandUtils;
import com.ithinkrok.mccw.command.WarsCommandExecutor;
import com.ithinkrok.mccw.command.WarsCommandSender;
import com.ithinkrok.mccw.data.Team;
import com.ithinkrok.mccw.data.User;
import org.bukkit.command.Command;

/**
 * Created by paul on 10/12/15.
 *
 * Handles the /transfer command
 */
public class TransferExecutor implements WarsCommandExecutor {
    @Override
    public boolean onCommand(WarsCommandSender sender, Command command, String label, String[] args) {
        if(!CommandUtils.checkUser(sender)) return true;

        if (args.length < 1) return false;

        User user = (User) sender;
        if (user.getTeam() == null) {
            user.sendLocale("commands.transfer.not-in-game");
            return true;
        }

        User target = null;
        if (args.length > 1) {
            String targetName = args[1];

            for (User other : user.getTeam().getUsers()) {
                if (!other.getName().equals(targetName)) continue;
                target = other;
            }

            if (target == null) {
                user.sendLocale("commands.transfer.no-player", targetName);
                return true;
            }
        }

        try {
            int amount = Integer.parseInt(args[0]);

            if (!user.subtractPlayerCash(amount, false)) {
                user.sendLocale("money.exchange.too-expensive");
                return true;
            }

            if (target == null) {
                Team team = user.getTeam();
                team.addTeamCash(amount);

                team.messageLocale("money.exchange.team-transfer", user.getFormattedName(), amount);
                team.messageLocale("money.balance.team.new", team.getTeamCash());
            } else {
                target.addPlayerCash(amount, false);

                user.getTeam().messageLocale("money.exchange.user-transfer", user.getFormattedName(), amount,
                        target.getFormattedName());

                target.sendLocale("money.balance.user.new", target.getPlayerCash());
            }

            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
