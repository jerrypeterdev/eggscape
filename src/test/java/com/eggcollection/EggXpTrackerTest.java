package com.eggcollection;

import com.google.gson.Gson;
import java.util.EnumMap;
import java.util.Map;
import net.runelite.api.Skill;
import org.junit.Test;
import static org.junit.Assert.*;

public class EggXpTrackerTest
{
    private Map<Skill,Integer> woodcutting(int xp)
    {
        Map<Skill,Integer> map = new EnumMap<>(Skill.class);
        map.put(Skill.WOODCUTTING,xp); return map;
    }

    @Test public void historicalXpDoesNotCountAndEveryChopAddsExactly76()
    {
        EggXpTracker tracker = new EggXpTracker();
        assertEquals(0,tracker.update(woodcutting(51_000_000)));
        for (int chop=1; chop<=14; chop++)
        {
            long gained = tracker.update(woodcutting(51_000_000+76*chop));
            assertEquals(76,gained);
        }
    }

    @Test public void staleAndDuplicateSnapshotsNeverRecreditHistoricalXp()
    {
        EggXpTracker tracker = new EggXpTracker();
        assertEquals(0,tracker.update(woodcutting(51_000_000)));
        assertEquals(0,tracker.update(woodcutting(0)));
        assertEquals(0,tracker.update(woodcutting(51_000_000)));
        assertEquals(76,tracker.update(woodcutting(51_000_076)));
        assertEquals(0,tracker.update(woodcutting(51_000_076)));
        tracker.reset();
        assertEquals(0,tracker.update(woodcutting(51_500_000)));
        assertEquals(76,tracker.update(woodcutting(51_500_076)));
    }

    @Test public void multipleSkillsCountOnlyTheirNewGainsAndIgnoreOverall()
    {
        EggXpTracker tracker = new EggXpTracker();
        Map<Skill,Integer> map=woodcutting(51_000_000);
        map.put(Skill.MINING,1_000_000); tracker.update(map);
        map.put(Skill.WOODCUTTING,51_000_076); map.put(Skill.MINING,1_000_050);
        // Recent RuneLite versions no longer expose OVERALL as a real skill.
        if (Skill.OVERALL != null) map.put(Skill.OVERALL,52_000_126);
        assertEquals(126,tracker.update(map));
    }

    @Test public void freshFlowResetsOldXpAndCollectionOnlyOnce()
    {
        Gson gson = new Gson();
        EggGame game = gson.fromJson("{\"hatchProgress\":51000000,\"collection\":{\"Tzrek-jad\":2}}",EggGame.class);
        assertTrue(game.migrateCollection());
        assertEquals(0,game.hatchProgress);
        assertTrue(game.collection.isEmpty());
        game.assignEgg();
        game.advance(76);
        game = gson.fromJson(gson.toJson(game),EggGame.class);
        assertFalse(game.migrateCollection()); assertEquals(76,game.hatchProgress);
    }
}
