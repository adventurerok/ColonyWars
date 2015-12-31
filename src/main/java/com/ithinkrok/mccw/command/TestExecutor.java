package com.ithinkrok.mccw.command;

import com.ithinkrok.mccw.data.Team;
import com.ithinkrok.mccw.data.User;
import com.ithinkrok.mccw.enumeration.PlayerClass;
import com.ithinkrok.minigames.TeamColor;
import com.ithinkrok.mccw.util.item.InventoryUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

/**
 * Created by paul on 11/12/15.
 * <p>
 * Handles the /test command
 */
public class TestExecutor implements WarsCommandExecutor {

    @Override
    public boolean onCommand(WarsCommandSender sender, Command command, String label, String[] args) {
        String playerName = "Console";
        User user = null;

        if (args.length > 0 && args[args.length - 1].startsWith("-p")) {
            playerName = args[args.length - 1].substring(2);
            Player targetPlayer = Bukkit.getPlayer(playerName);

            if (targetPlayer != null) user = sender.getPlugin().getUser(targetPlayer);
        } else if(sender instanceof User) {
            user = (User) sender;
        }

        if (user == null) {
            sender.sendLocale("commands.test.unknown-player", playerName);
            return true;
        }

        switch (args[0]) {
            case "team":
                return handleTeamSubcommand(user, args);
            case "class":
                return handleClassSubcommand(user, args);
            case "money":
                return handleMoneySubcommand(user);
            case "build":
                return handleBuildSubcommand(user, args);
            case "base_location":
                return handleBaseLocationSubcommand(user);
            case "shrink":
                return handleShrinkSubcommand(user, args);
            case "rejoin":
                return handleRejoinSubcommand(user);

        }

        return true;
    }

    private boolean handleTeamSubcommand(User user, String[] args) {
        if (args.length < 2) return false;

        TeamColor teamColor = TeamColor.fromName(args[1]);
        if (teamColor == null) {
            user.sendLocale("commands.test.invalid-team", args[1]);
            return true;
        }
        user.setTeamColor(teamColor);

        user.sendLocale("commands.test.team-change", teamColor.getFormattedName());

        return true;
    }

    private boolean handleClassSubcommand(User user, String[] args) {
        if (args.length < 2) return false;

        PlayerClass playerClass = PlayerClass.fromName(args[1]);
        if (playerClass == null) {
            user.sendLocale("commands.test.invalid-class", args[1]);
            return true;
        }
        user.setPlayerClass(playerClass);

        user.sendLocale("commands.test.class-change", playerClass.getFormattedName());

        return true;
    }

    private boolean handleMoneySubcommand(User user) {
        user.addPlayerCash(10000, true);
        user.getTeam().addTeamCash(10000, true);

        return true;
    }

    private boolean handleBuildSubcommand(User user, String[] args) {
        if (args.length < 2) return false;

        user.getPlayerInventory().addItem(InventoryUtils.createItemWithNameAndLore(Material.LAPIS_ORE, 16, 0, args[1]));

        user.sendLocale("commands.test.build-blocks", 16, args[1]);
        return true;
    }

    private boolean handleBaseLocationSubcommand(User user) {
        Team team = user.getTeam();
        if (team == null) {
            user.sendLocale("commands.test.base.no-team");
            return true;
        }

        user.sendLocale("commands.test.base.loc", team.getBaseLocation());
        return true;
    }

    private boolean handleShrinkSubcommand(User user, String[] args) {
        if (user.getPlugin().getShowdownArena() == null) return true;
        if (args.length < 2) return false;

        try {
            int amount = Integer.parseInt(args[1]);

            if (amount < 1 || amount > 30) {
                user.sendLocale("commands.shrink.bad-size", args[1]);
                return true;
            }

            for (int count = 0; count < amount; ++count) {
                user.getPlugin().getShowdownArena().shrinkArena(user.getPlugin());
            }

            return true;
        } catch (NumberFormatException e) {
            user.sendLocale("commands.shrink.bad-size", args[1]);
            return true;
        }
    }

    private boolean handleRejoinSubcommand(User user) {
        user.getPlugin().getGameInstance().rejoinUser(user);
        return true;
    }
}
