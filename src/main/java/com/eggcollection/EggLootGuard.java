package com.eggcollection;

import java.util.HashMap;
import java.util.Map;

/** The raw NPC and Loot Tracker callbacks may describe the same personal drop. */
final class EggLootGuard
{
    private final Map<String,Integer> recent=new HashMap<>();
    void reset(){recent.clear();}
    boolean accept(String key,int tick)
    {
        if(tick-recent.getOrDefault(key,-100)<=2)return false;
        recent.entrySet().removeIf(entry -> tick-entry.getValue()>20);
        recent.put(key,tick);return true;
    }
}
