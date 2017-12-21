package com.ithinkrok.cw;

import com.ithinkrok.cw.database.UserCategoryStats;
import com.ithinkrok.minigames.api.SpecificPlugin;
import com.ithinkrok.minigames.api.database.DatabaseObject;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by paul on 31/12/15.
 */
public class CWPlugin extends SpecificPlugin {

    @Override
    public List<Class<? extends DatabaseObject>> getDatabaseClasses() {
        return Collections.singletonList(UserCategoryStats.class);
    }
}
