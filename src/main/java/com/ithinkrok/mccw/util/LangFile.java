package com.ithinkrok.mccw.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Created by paul on 20/11/15.
 *
 * Handles a language properties file
 */
public class LangFile {

    private Map<Object, String> languageStrings = new HashMap<>();

    public LangFile(Properties properties) {
        for(Object key : properties.keySet()){
            String value = properties.getProperty(key.toString());
            value = value.replace('&', '§');
            languageStrings.put(key, value);
        }
    }

    public String getLocale(String locale){
        String result = languageStrings.get(locale);

        if(result == null){
            System.out.println("Missing language string for: " + locale);

            languageStrings.put(locale, locale);
            return locale;
        }

        return result;
    }

    public String getLocale(String locale, Object...args){
        return String.format(getLocale(locale), args);
    }
}
