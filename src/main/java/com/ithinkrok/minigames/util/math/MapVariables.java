package com.ithinkrok.minigames.util.math;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by paul on 04/01/16.
 */
public class MapVariables implements Variables {

    private Map<String, Double> variables;

    public MapVariables() {
        this(new HashMap<>());
    }

    public MapVariables(Map<String, Double> variables) {
        this.variables = variables;
    }

    public void setVariable(String name, double value) {
        variables.put(name, value);
    }

    @Override
    public double getVariable(String name) {
        Double d = variables.get(name);

        return d == null ? 0 : d;
    }
}
