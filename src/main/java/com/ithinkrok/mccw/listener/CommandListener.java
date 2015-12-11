package com.ithinkrok.mccw.listener;

import com.ithinkrok.mccw.WarsPlugin;
import com.ithinkrok.mccw.command.TestExecutor;
import com.ithinkrok.mccw.command.WarsCommandExecutor;
import com.ithinkrok.mccw.command.WarsCommandSender;
import com.ithinkrok.mccw.command.WarsConsoleSender;
import com.ithinkrok.mccw.command.executors.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;

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
        executorHashMap.put("test", new TestExecutor());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String commandName = command.getName().toLowerCase();

        if (executorHashMap.containsKey(commandName)) {
            WarsCommandSender warsCommandSender;
            if (sender instanceof Player) warsCommandSender = plugin.getUser((Player) sender);
            else warsCommandSender = consoleSender;

            return executorHashMap.get(commandName).onCommand(warsCommandSender, command, label, args);
        } else return false;
    }

}
