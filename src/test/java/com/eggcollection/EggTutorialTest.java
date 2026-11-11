package com.eggcollection;

import com.google.gson.Gson;
import java.util.Random;
import org.junit.Test;
import static org.junit.Assert.*;

public class EggTutorialTest
{
    @Test public void firstJerryTakesFifteenMinutesAndAllFollowingEggsTakeAnHour()
    {
        EggGame game=new EggGame();game.migrateCollection();game.assignEgg();
        assertEquals(900_000,game.hatchRequirement());game.advance(899_999);assertFalse(game.canHatch());
        game.advance(1);assertSame(EggPet.JERRY,game.hatch());
        game.queueShiny(EggPet.JERRY);game.spin(new Random(0));assertTrue(game.activeShiny);
        assertEquals(3_600_000,game.hatchRequirement());game.advance(900_000);assertFalse(game.canHatch());
        game.advance(2_700_000);assertSame(EggPet.JERRY,game.hatch());
        game.spin(new Random(0));assertEquals(3_600_000,game.hatchRequirement());
    }

    @Test public void idleTutorialTakesThirtyMinutesAndRestartsRetainItsProgress()
    {
        EggGame game=new EggGame();game.migrateCollection();game.assignEgg();
        EggActivityTracker tracker=new EggActivityTracker();tracker.update(0,false);
        for(int second=1;second<=1800;second++)game.advance(tracker.update(second*1_000_000_000L,false));
        game=new Gson().fromJson(new Gson().toJson(game),EggGame.class);
        assertFalse(game.migrateCollection());assertEquals(900_000,game.hatchProgress);
        assertTrue(game.canHatch());assertTrue(game.readyWasIdle());
    }

    @Test public void bankedTimeAndIdleFlagsUseFifteenMinutesThenOneHourBoundaries()
    {
        EggGame game=new EggGame();game.migrateCollection();game.assignEgg();game.activeGameplay=false;
        game.advance(900_000);game.activeGameplay=true;game.advance(3_600_000);
        assertEquals(2,game.readyIdleFlags.size());assertTrue(game.readyWasIdle());
        game.hatch();assertEquals(3_600_000,game.hatchProgress);assertFalse(game.readyWasIdle());
        game.spin(new Random(0));assertTrue(game.canHatch());game.hatch();assertEquals(0,game.hatchProgress);
    }
}
