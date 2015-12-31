package com.ithinkrok.minigames;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by paul on 31/12/15.
 */
public class Minigame<U extends User, G extends GameGroup, M extends Minigame> {

    private List<U> usersInServer = new ArrayList<>();
    private List<G> gameGroups = new ArrayList<>();

}
