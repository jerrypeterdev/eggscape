package com.eggcollection;

import com.google.gson.Gson;
import java.util.Set;
import org.junit.Test;
import static org.junit.Assert.*;

public class EggTimeProgressTest
{
    @Test public void xpSaveConvertsOnceWithoutLosingPetsOrBankedEggs()
    {
        Gson gson=new Gson();EggGame game=gson.fromJson("{\"collectionVersion\":3,\"hatchProgress\":2500,\"activePet\":\"JAD\",\"collection\":{\"Tzrek-jad\":4}}",EggGame.class);
        assertTrue(game.migrateCollection());assertEquals(9_000_000,game.hatchProgress);
        assertEquals(Integer.valueOf(4),game.collection.get(EggPet.JAD.key));assertSame(EggPet.JAD,game.activePet);
        game=gson.fromJson(gson.toJson(game),EggGame.class);assertFalse(game.migrateCollection());
        assertEquals(9_000_000,game.hatchProgress);
    }

    @Test public void questBonusMakesFortyFiveMinutesEnoughAndSurplusBonusesCarryForward()
    {
        EggGame game=new EggGame();game.migrateCollection();game.assignEgg();game.activePet=EggPet.JAD;game.awardQuestBonus(2);
        assertEquals(900_000,game.hatchProgress);assertEquals(900_000,game.pendingQuestBonus);
        game.advance(2_699_999);assertFalse(game.canHatch());game.advance(1);assertTrue(game.canHatch());
        assertSame(EggPet.JAD,game.hatch());assertEquals(900_000,game.hatchProgress);
        assertEquals(0,game.pendingQuestBonus);assertEquals(900_000,game.questBonusUsed);
    }

    @Test public void readyEggsBankQuestBonusesAndLogoutRetainsThem()
    {
        EggGame game=new EggGame();game.migrateCollection();game.assignEgg();game.skipToHatch();
        game.awardQuestBonus(1);assertEquals(900_000,game.pendingQuestBonus);
        game=new Gson().fromJson(new Gson().toJson(game),EggGame.class);
        game.hatch();assertEquals(900_000,game.hatchProgress);assertEquals(0,game.pendingQuestBonus);
    }

    @Test public void questBaselineAndRepeatedSnapshotsCannotAwardOldCompletions()
    {
        EggQuestTracker tracker=new EggQuestTracker();assertEquals(0,tracker.update(Set.of(1,2,3)));
        assertEquals(1,tracker.update(Set.of(1,2,3,4)));assertEquals(0,tracker.update(Set.of(1,2,3,4)));
        assertEquals(0,tracker.update(Set.of(1)));assertEquals(0,tracker.update(Set.of(1,2,3,4)));
        tracker.reset();assertEquals(0,tracker.update(Set.of(1,2,3,4,5)));
    }
}
