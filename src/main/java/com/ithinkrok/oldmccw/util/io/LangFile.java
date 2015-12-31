package com.ithinkrok.oldmccw.util.io;

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
            try{
                //print the stack trace
                throw new RuntimeException("Missing language string for: " + locale);
            } catch(RuntimeException e){
                e.printStackTrace();
            }

            languageStrings.put(locale, locale);
            return locale;
        }

        return result;
    }

    public String getLocale(String locale, Object...args){
        return String.format(getLocale(locale), args);
    }
}
