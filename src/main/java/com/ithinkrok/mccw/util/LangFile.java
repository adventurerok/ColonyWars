package com.ithinkrok.mccw.util;

import java.util.Properties;

/**
 * Created by paul on 20/11/15.
 *
 * Handles a language properties file
 */
public class LangFile {

    private Properties properties;

    public LangFile(Properties properties) {
        this.properties = properties;
    }

    public String getLocale(String locale){
        return properties.getProperty(locale, locale);
    }

    public String getLocale(String locale, Object...args){
        return String.format(getLocale(locale), args);
    }
}
