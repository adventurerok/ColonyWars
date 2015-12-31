package com.ithinkrok.oldmccw.command;

import com.ithinkrok.oldmccw.WarsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;

/**
 * Created by paul on 10/12/15.
 *
 * A special WarsCommandSender for the console
 */
public class WarsConsoleSender implements WarsCommandSender {

    private static ConsoleCommandSender consoleCommandSender = Bukkit.getConsoleSender();

    private WarsPlugin plugin;

    public WarsConsoleSender(WarsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void sendLocale(String locale, Object... args) {
        consoleCommandSender.sendMessage(WarsPlugin.CHAT_PREFIX + plugin.getLocale(locale, args));
    }

    @Override
    public void sendMessageDirect(String message) {
        consoleCommandSender.sendMessage(message);
    }

    @Override
    public void sendLocaleDirect(String locale, Object... args) {
        consoleCommandSender.sendMessage(plugin.getLocale(locale, args));
    }

    @Override
    public void sendMessage(String message) {
        consoleCommandSender.sendMessage(WarsPlugin.CHAT_PREFIX + message);
    }

    @Override
    public String getName() {
        return consoleCommandSender.getName();
    }

    @Override
    public WarsPlugin getPlugin() {
        return plugin;
    }
}
