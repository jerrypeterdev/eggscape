package com.eggcollection;

import java.util.EnumMap;
import java.util.Map;
import net.runelite.api.Skill;

/** Compare complete, post-update skill snapshots; never credit login totals. */
class EggXpTracker
{
    private final Map<Skill, Integer> baseline = new EnumMap<>(Skill.class);
    private boolean initialized;

    void reset()
    {
        baseline.clear(); initialized = false;
    }

    long update(Map<Skill, Integer> current)
    {
        if (!initialized)
        {
            baseline.putAll(current); initialized = true; return 0;
        }
        long gained = 0;
        for (Map.Entry<Skill, Integer> entry : current.entrySet())
        {
            if (entry.getKey() == Skill.OVERALL || entry.getValue() < 0) continue;
            Integer previous = baseline.get(entry.getKey());
            int xp = entry.getValue();
            if (previous == null) baseline.put(entry.getKey(), xp);
            else if (xp > previous)
            {
                gained += (long) xp - previous;
                baseline.put(entry.getKey(), xp);
            }
            // A stale/lower reading must never lower the baseline and re-credit XP.
        }
        return gained;
    }
}
