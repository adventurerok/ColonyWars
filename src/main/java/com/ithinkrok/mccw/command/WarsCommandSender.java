package com.ithinkrok.mccw.command;

/**
 * Created by paul on 10/12/15.
 *
 * A command sender with message locale support
 */
public interface WarsCommandSender {

    void sendLocale(String locale, Object...args);
    void sendMessageDirect(String message);
    void sendLocaleDirect(String locale, Object...args);
    void sendMessage(String message);
    String getName();

}
