package com.ithinkrok.mccw.command;

import com.ithinkrok.mccw.WarsPlugin;
import com.ithinkrok.mccw.util.io.LangFile;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;

/**
 * Created by paul on 10/12/15.
 *
 * A special WarsCommandSender for the console
 */
public class WarsConsoleSender implements WarsCommandSender {

    private static ConsoleCommandSender consoleCommandSender = Bukkit.getConsoleSender();

    private LangFile langFile;

    public WarsConsoleSender(LangFile langFile) {
        this.langFile = langFile;
    }

    @Override
    public void sendLocale(String locale, Object... args) {
        consoleCommandSender.sendMessage(WarsPlugin.CHAT_PREFIX + langFile.getLocale(locale, args));
    }

    @Override
    public void sendMessageDirect(String message) {
        consoleCommandSender.sendMessage(message);
    }

    @Override
    public void sendLocaleDirect(String locale, Object... args) {
        consoleCommandSender.sendMessage(langFile.getLocale(locale, args));
    }

    @Override
    public void sendMessage(String message) {
        consoleCommandSender.sendMessage(WarsPlugin.CHAT_PREFIX + message);
    }

    @Override
    public String getName() {
        return consoleCommandSender.getName();
    }
}
