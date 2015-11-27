package com.ithinkrok.mccw.listener;

import com.ithinkrok.mccw.WarsPlugin;
import com.ithinkrok.mccw.data.Team;
import com.ithinkrok.mccw.data.User;
import com.ithinkrok.mccw.enumeration.GameState;
import com.ithinkrok.mccw.enumeration.PlayerClass;
import com.ithinkrok.mccw.enumeration.TeamColor;
import com.ithinkrok.mccw.util.InventoryUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Created by paul on 13/11/15.
 * <p>
 * Handles commands for Colony Wars
 */
public class CommandListener implements CommandExecutor {

    private WarsPlugin plugin;

    public CommandListener(WarsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getLocale("command.no-console"));
            return true;
        }

        Player player = (Player) sender;
        User user = plugin.getUser(player);

        switch (command.getName().toLowerCase()) {
            case "transfer":
                return onTransferCommand(user, args);
            case "test":
                return args.length >= 1 && onTestCommand(user, args);
            case "gamestate":
                return onGameStateCommand(user, args);
            case "members":
                return onMembersCommand(user);
            case "spawn":
                return onSpawnCommand(user);
            case "fix":
                return onFixCommand(user);
            default:
                return false;
        }

    }

    private boolean onFixCommand(User user) {
        user.teleport(user.getPlayer().getLocation().add(0, 0.5d, 0));
        return true;
    }

    private boolean onTransferCommand(User user, String[] args) {
        if (args.length < 1) return false;

        try {
            int amount = Integer.parseInt(args[0]);

            if (!user.subtractPlayerCash(amount)) {
                user.messageLocale("money.exchange.too-expensive");
                return true;
            }

            Team team = user.getTeam();
            team.addTeamCash(amount);

            team.messageLocale("money.exchange.team-transfer", user.getFormattedName(), amount);
            team.messageLocale("money.balance.team.new", team.getTeamCash());

            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean onTestCommand(User user, String[] args) {

        switch (args[0]) {
            case "team":
                if (args.length < 2) return false;

                TeamColor teamColor = TeamColor.fromName(args[1]);
                if(teamColor == null) {
                    user.messageLocale("commands.test.invalid-team", args[1]);
                    return true;
                }
                user.setTeamColor(teamColor);

                user.messageLocale("commands.test.team-change", teamColor.getFormattedName());

                break;
            case "class":
                if (args.length < 2) return false;

                PlayerClass playerClass = PlayerClass.fromName(args[1]);
                if(playerClass == null){
                    user.messageLocale("commands.test.invalid-class", args[1]);
                    return true;
                }
                user.setPlayerClass(playerClass);

                user.messageLocale("commands.test.class-change", playerClass.getFormattedName());

                break;
            case "money":

                user.addPlayerCash(10000);
                user.getTeam().addTeamCash(10000);

                user.messageLocale("commands.test.money", 10000);
                break;
            case "build":
                if (args.length < 2) return false;

                user.getPlayerInventory()
                        .addItem(InventoryUtils.createItemWithNameAndLore(Material.LAPIS_ORE, 16, 0, args[1]));

                user.messageLocale("commands.test.build-blocks", 16, args[1]);
                break;
            case "base_location":
                Team team = user.getTeam();
                if (team == null) {
                    user.messageLocale("commands.test.base.no-team");
                    break;
                }

                user.messageLocale("commands.test.base.loc", team.getBaseLocation());
                break;

        }

        return true;
    }

    private boolean onGameStateCommand(User user, String[] args) {
        try {
            GameState gameState = GameState.valueOf(args[0].toUpperCase());
            plugin.changeGameState(gameState);
            user.messageLocale("commands.gamestate.changed", gameState);
            return true;
        } catch (Exception e) {
            user.messageLocale("commands.gamestate.invalid");
            return false;
        }
    }

    private boolean onMembersCommand(User user) {
        if (user.getTeamColor() == null) {
            user.messageLocale("commands.members.no-team");
            return true;
        }

        user.messageLocale("commands.members.title");

        Team team = user.getTeam();

        for (Player player : team.getPlayers()) {
            User other = plugin.getUser(player);

            String name = other.getFormattedName();
            String playerClass =
                    other.getPlayerClass() == null ? plugin.getLocale("team.none") : other.getPlayerClass().getFormattedName();

            String nearestBase = null;
            if (plugin.isInGame()) {
                double smallestDistSquared = 99999999;

                for (TeamColor teamColor : TeamColor.values()) {
                    Location loc = plugin.getMapSpawn(teamColor);
                    double distSquared = loc.distanceSquared(player.getLocation());

                    if (distSquared < smallestDistSquared) {
                        smallestDistSquared = distSquared;
                        nearestBase = teamColor.getFormattedName();
                    }
                }

                if (plugin.getMapSpawn(null).distanceSquared(player.getLocation()) < smallestDistSquared) {
                    nearestBase = plugin.getLocale("commands.members.near-showdown", nearestBase);
                }
            } else {
                nearestBase = "Not in game";
            }

            //Send the player a message directly to avoid the chat prefix
            user.getPlayer()
                    .sendMessage(plugin.getLocale("commands.members.player-info", name, playerClass, nearestBase));
        }

        return true;
    }

    private boolean onSpawnCommand(User user) {
        if (user.isInGame()) {
            user.messageLocale("commands.spawn.not-in-game");
            return true;
        }

        user.teleport(plugin.getMapSpawn(null));
        return true;
    }
}
