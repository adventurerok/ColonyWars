package com.ithinkrok.mccw.listener;

import com.ithinkrok.mccw.WarsPlugin;
import com.ithinkrok.mccw.command.WarsCommandExecutor;
import com.ithinkrok.mccw.command.WarsCommandSender;
import com.ithinkrok.mccw.command.WarsConsoleSender;
import com.ithinkrok.mccw.command.executors.*;
import com.ithinkrok.mccw.data.Team;
import com.ithinkrok.mccw.data.User;
import com.ithinkrok.mccw.data.UserCategoryStats;
import com.ithinkrok.mccw.enumeration.GameState;
import com.ithinkrok.mccw.enumeration.PlayerClass;
import com.ithinkrok.mccw.enumeration.TeamColor;
import com.ithinkrok.mccw.handler.CountdownHandler;
import com.ithinkrok.mccw.util.item.InventoryUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.util.Vector;

import java.text.DecimalFormat;
import java.util.*;

/**
 * Created by paul on 13/11/15.
 * <p>
 * Handles commands for Colony Wars
 */
public class CommandListener implements CommandExecutor {

    private HashMap<String, WarsCommandExecutor> executorHashMap = new HashMap<>();
    private WarsPlugin plugin;
    private WarsConsoleSender consoleSender;

    public CommandListener(WarsPlugin plugin) {
        this.plugin = plugin;
        this.consoleSender = new WarsConsoleSender(plugin);

        executorHashMap.put("members", new MembersExecutor());
        executorHashMap.put("fix", new FixExecutor());
        executorHashMap.put("transfer", new TransferExecutor());
        executorHashMap.put("list", new ListExecutor());
        executorHashMap.put("gamestate", new GameStateExecutor());
        executorHashMap.put("leaderboard", new LeaderboardExecutor());
        executorHashMap.put("spawn", new SpawnExecutor());
        executorHashMap.put("countdown", new CountdownExecutor());
        executorHashMap.put("teamchat", new TeamChatExecutor());
        executorHashMap.put("stats", new StatsExecutor());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String commandName = command.getName().toLowerCase();

        if (executorHashMap.containsKey(commandName)) {
            WarsCommandSender warsCommandSender;
            if (sender instanceof Player) warsCommandSender = plugin.getUser((Player) sender);
            else warsCommandSender = consoleSender;

            return executorHashMap.get(commandName).onCommand(warsCommandSender, command, label, args);
        } else return onCommandOld(sender, command, label, args);
    }

    public boolean onCommandOld(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getLocale("command.no-console"));
            return true;
        }

        Player player = (Player) sender;
        User user = plugin.getUser(player);

        switch (command.getName().toLowerCase()) {
            case "test":
                return args.length >= 1 && onTestCommand(user, args);
            default:
                return false;
        }

    }

    private boolean onTestCommand(User user, String[] args) {
        if (args.length > 0 && args[args.length - 1].startsWith("-p")) {
            String playerName = args[args.length - 1].substring(2);
            Player targetPlayer = Bukkit.getPlayer(playerName);

            if (targetPlayer != null) user = plugin.getUser(targetPlayer);
            else {
                user.sendLocale("commands.test.unknown-player", playerName);
                return true;
            }
        }

        switch (args[0]) {
            case "team":
                if (args.length < 2) return false;

                TeamColor teamColor = TeamColor.fromName(args[1]);
                if (teamColor == null) {
                    user.sendLocale("commands.test.invalid-team", args[1]);
                    return true;
                }
                user.setTeamColor(teamColor);

                user.sendLocale("commands.test.team-change", teamColor.getFormattedName());

                break;
            case "class":
                if (args.length < 2) return false;

                PlayerClass playerClass = PlayerClass.fromName(args[1]);
                if (playerClass == null) {
                    user.sendLocale("commands.test.invalid-class", args[1]);
                    return true;
                }
                user.setPlayerClass(playerClass);

                user.sendLocale("commands.test.class-change", playerClass.getFormattedName());

                break;
            case "money":

                user.addPlayerCash(10000);
                user.getTeam().addTeamCash(10000);

                user.sendLocale("commands.test.money", 10000);
                break;
            case "build":
                if (args.length < 2) return false;

                user.getPlayerInventory()
                        .addItem(InventoryUtils.createItemWithNameAndLore(Material.LAPIS_ORE, 16, 0, args[1]));

                user.sendLocale("commands.test.build-blocks", 16, args[1]);
                break;
            case "base_location":
                Team team = user.getTeam();
                if (team == null) {
                    user.sendLocale("commands.test.base.no-team");
                    break;
                }

                user.sendLocale("commands.test.base.loc", team.getBaseLocation());
                break;
            case "shrink":
                if (plugin.getShowdownArena() == null) return true;
                if (args.length < 2) return false;

                try {
                    int amount = Integer.parseInt(args[1]);

                    if (amount < 1 || amount > 30) {
                        user.sendLocale("commands.shrink.bad-size", args[1]);
                        return true;
                    }

                    for (int count = 0; count < amount; ++count) {
                        plugin.getShowdownArena().shrinkArena(plugin);
                    }

                    return true;
                } catch (NumberFormatException e) {
                    user.sendLocale("commands.shrink.bad-size", args[1]);
                    return true;
                }
            case "rejoin":
                plugin.getGameInstance().setupUser(user);
                return true;

        }

        return true;
    }


}
