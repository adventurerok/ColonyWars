package com.ithinkrok.cw;

import com.ithinkrok.cw.database.UserCategoryStats;
import com.ithinkrok.minigames.Game;
import com.ithinkrok.minigames.SpecificPlugin;

import java.util.List;

/**
 * Created by paul on 31/12/15.
 */
public class CWPlugin extends SpecificPlugin {

    @Override
    public List<Class<?>> getDatabaseClasses() {
        List<Class<?>> result = super.getDatabaseClasses();

        result.add(UserCategoryStats.class);

        return result;
    }
}
