package com.eggcollection;

import com.google.gson.Gson;
import java.util.Random;
import org.junit.Test;
import static org.junit.Assert.*;

public class EggBossRewardsTest
{
    @Test public void testEggCanOnlyBeQueuedOnceEvenAfterHatchingReloadingAndAnotherNormalEgg()
    {
        EggGame game=game();assertTrue(game.canDebugPetDrop());assertTrue(game.debugPetDrop());
        assertFalse(game.debugPetDrop());assertEquals(1,game.shinyQueue.size());
        game=new Gson().fromJson(new Gson().toJson(game),EggGame.class);
        assertFalse(game.canDebugPetDrop());game.skipToHatch();game.hatch();
        game.spin(new Random(0));assertTrue(game.activeShiny);assertFalse(game.debugPetDrop());
        game.skipToHatch();game.hatch();game.spin(new Random(0));
        assertFalse(game.activeShiny);assertFalse(game.debugPetDrop());assertTrue(game.shinyQueue.isEmpty());
        assertTrue(game.unlockRealPet(EggPet.MOLE,false,new Random(0)));
        EggGame reset=game();assertTrue(reset.canDebugPetDrop());
    }

    @Test public void debugRewardIsNextAndKeepsPreviouslyQueuedRealRewards()
    {
        EggGame game=game();game.queueShiny(EggPet.MOLE);game.activePet=EggPet.NEX;
        assertTrue(game.debugPetDrop());assertEquals(java.util.List.of(EggPet.NEX,EggPet.MOLE),game.shinyQueue);
        assertSame(EggPet.NEX,game.activePet);assertFalse(game.activeShiny);
    }

    @Test public void onlyAnIncomingExactNamePrivateMessageQualifies()
    {
        assertTrue(EggRewardDetector.isJerryPrivateMessage(net.runelite.api.ChatMessageType.PRIVATECHAT,"jerrypeter"));
        assertTrue(EggRewardDetector.isJerryPrivateMessage(net.runelite.api.ChatMessageType.PRIVATECHAT,"Mouldy"));
        assertFalse(EggRewardDetector.isJerryPrivateMessage(net.runelite.api.ChatMessageType.PRIVATECHAT,"Moldy"));
        assertTrue(EggRewardDetector.isJerryPrivateMessage(net.runelite.api.ChatMessageType.PRIVATECHAT,"<img=1>jerrypeter"));
        assertFalse(EggRewardDetector.isJerryPrivateMessage(net.runelite.api.ChatMessageType.PRIVATECHATOUT,"jerrypeter"));
        assertFalse(EggRewardDetector.isJerryPrivateMessage(net.runelite.api.ChatMessageType.PUBLICCHAT,"jerrypeter"));
        assertFalse(EggRewardDetector.isJerryPrivateMessage(net.runelite.api.ChatMessageType.PRIVATECHAT,"Jerry Peter"));
        assertFalse(EggRewardDetector.isJerryPrivateMessage(net.runelite.api.ChatMessageType.PRIVATECHAT,"jerrypeter1"));
    }

    @Test public void duplicateLootCallbacksDoNotBecomeExtraKills()
    {
        EggLootGuard guard=new EggLootGuard();
        assertTrue(guard.accept("mole|526:1",100));assertFalse(guard.accept("mole|526:1",100));
        assertFalse(guard.accept("mole|526:1",101));assertTrue(guard.accept("mole|526:2",101));
        assertTrue(guard.accept("mole|526:1",104));guard.reset();assertTrue(guard.accept("mole|526:1",104));
    }

    private EggGame game()
    {EggGame game=new EggGame();game.migrateCollection();game.assignEgg();return game;}

    @Test public void secretUsesTheMomentProgressCrossesRatherThanTheLaterActivityState()
    {
        EggGame game=game();game.activeGameplay=false;game.advance(EggGame.HATCH_MILLIS);
        game.activeGameplay=true;assertTrue(game.readyWasIdle());
        game.advance(EggGame.HATCH_MILLIS);game.hatch();assertFalse(game.readyWasIdle());
        game=game();game.activeGameplay=true;game.advance(EggGame.HATCH_MILLIS);
        game.activeGameplay=false;assertFalse(game.readyWasIdle());
        game=game();game.skipToHatch();assertFalse(game.readyWasIdle());
    }

    @Test public void matchingFightTimeIsPaidOnlyOnConfirmedLootAndOnlyOnce()
    {
        EggBossTracker tracker=new EggBossTracker();
        for(int i=0;i<120;i++)tracker.observe(EggPet.NEX,1000,true,EggPet.NEX,false);
        assertEquals(0,tracker.claim(EggPet.NEX,EggPet.GRAARDOR));
        assertEquals(120_000,tracker.claim(EggPet.NEX,EggPet.NEX));
        assertEquals(0,tracker.claim(EggPet.NEX,EggPet.NEX));
        for(int i=0;i<60;i++)tracker.observe(EggPet.GRAARDOR,1000,true,EggPet.GRAARDOR,false);
        assertEquals(60_000,tracker.claim(EggPet.GRAARDOR,EggPet.GRAARDOR));
    }

    @Test public void barrowsRunIncludesActiveTravelButNeverIdleBankTimeOrFailedAttempts()
    {
        EggBossTracker tracker=new EggBossTracker();
        for(int i=0;i<480;i++)tracker.observe(EggPet.AHRIM,1000,true,null,true);
        for(int i=0;i<60;i++)tracker.observe(EggPet.AHRIM,1000,false,null,true);
        assertEquals(480_000,tracker.claim(EggPet.AHRIM,EggBossCatalog.source("Barrows")));
        tracker.observe(EggPet.AHRIM,1000,true,EggPet.DHAROK,false);tracker.reset();
        assertEquals(0,tracker.claim(EggPet.AHRIM,EggPet.AHRIM));
    }

    @Test public void bossBonusCannotOverflowIntoAnUnrelatedNextEgg()
    {
        EggGame game=game();game.activePet=EggPet.NEX;game.activeGameplay=true;game.advance(EggGame.HATCH_MILLIS-1000);
        game.awardBossBonus(120_000);assertEquals(EggGame.HATCH_MILLIS,game.hatchProgress);
        assertFalse(game.readyWasIdle());game.hatch();assertEquals(0,game.hatchProgress);
    }

    @Test public void exactJerryPmQueuesOnceAndNeverReplacesTheCurrentEgg()
    {
        EggGame game=game();
        for(String name:new String[]{"Jerry Peter","Jerrypeter","jerrypeter ","jerrypeter1","jerry"})
            assertFalse(game.receiveJerryMessage(name));
        assertTrue(game.receiveJerryMessage("jerrypeter"));assertFalse(game.receiveJerryMessage("jerrypeter"));
        assertFalse(game.receiveJerryMessage("Mouldy"));
        assertSame(EggPet.JERRY,game.activePet);assertFalse(game.activeShiny);
        game.skipToHatch();game.hatch();assertSame(EggPet.JERRY,game.spin(new Random(1)));assertTrue(game.activeShiny);
    }

    @Test public void aShinyOnlyPetCountsOnceAndLaterNormalHatchesNeverDowngradeIt()
    {
        EggGame game=game();game.shinyCollection.put(EggPet.MOLE.key,1);
        assertEquals(1,game.uniquePets());assertTrue(game.hasShiny(EggPet.MOLE));
        game.collection.put(EggPet.MOLE.key,2);
        assertEquals(1,game.uniquePets());assertEquals(3,game.collectedCount(EggPet.MOLE));
        assertTrue(game.hasShiny(EggPet.MOLE));
    }

    @Test public void petDropQueueSurvivesReloadAndHatchesIntoASeparateShinyLog()
    {
        EggGame game=game();assertTrue(game.unlockRealPet(EggPet.MOLE,false,new Random(1)));
        game=new Gson().fromJson(new Gson().toJson(game),EggGame.class);
        game.skipToHatch();game.hatch();assertSame(EggPet.MOLE,game.spin(new Random(1)));assertTrue(game.activeShiny);
        game.skipToHatch();assertSame(EggPet.MOLE,game.hatch());
        assertEquals(Integer.valueOf(1),game.shinyCollection.get(EggPet.MOLE.key));
        assertFalse(game.collection.containsKey(EggPet.MOLE.key));assertEquals(1,game.shinyUniquePets());
    }

    @Test public void sharedPetPhasesUnlockDifferentShiniesWithoutDuplicates()
    {
        EggGame game=game();
        for(int i=0;i<4;i++)assertTrue(game.unlockRealPet(EggPet.MUSPAH_MELEE,false,new Random(0)));
        assertEquals(4,new java.util.HashSet<>(game.shinyQueue).size());
        assertTrue(game.unlockRealPet(EggPet.MUSPAH_MELEE,false,new Random(0)));assertEquals(5,game.shinyQueue.size());
        assertFalse(game.unlockRealPet(EggPet.OBOR,false,new Random(0)));
    }

    @Test public void normalGroupCompletionQueuesAllSixBrothersAndThreeMoonsOnce()
    {
        EggGame game=game();
        for(EggPet pet:EggBossCatalog.members(EggPet.AHRIM))game.collection.put(pet.key,1);
        game.evaluateLogUnlocks();assertEquals(6,game.shinyQueue.size());
        for(EggPet pet:EggBossCatalog.members(EggPet.BLOOD_MOON))game.collection.put(pet.key,1);
        game.evaluateLogUnlocks();game.evaluateLogUnlocks();assertEquals(9,game.shinyQueue.size());
    }

    @Test public void noPetBossesUnlockAtOneHundredPersonalKillsOnly()
    {
        EggGame game=game();for(int i=0;i<99;i++)game.recordBossKill(EggPet.OBOR);
        assertTrue(game.shinyQueue.isEmpty());game.recordBossKill(EggPet.OBOR);
        assertEquals(EggPet.OBOR,game.shinyQueue.get(0));game.recordBossKill(EggPet.OBOR);assertEquals(1,game.shinyQueue.size());
    }

    @Test public void lootAndPetNoticesWorkInBothOrdersWithoutDoubleUnlocks()
    {
        EggGame game=game();EggRewardDetector detector=new EggRewardDetector(new Random(0));
        detector.message(game,"You have a funny feeling like you're being followed.",100);
        assertTrue(game.shinyQueue.isEmpty());detector.loot(game,EggPet.MOLE,101);
        detector.message(game,"New item added to your collection log: Baby mole",102);
        assertEquals(1,game.shinyQueue.size());assertSame(EggPet.MOLE,game.shinyQueue.get(0));
        detector.loot(game,EggPet.NEX,150);detector.message(game,"You feel something weird sneaking into your backpack.",151);
        assertEquals(2,game.shinyQueue.size());
        detector.message(game,"Other player received a drop: Vorki",180);assertEquals(2,game.shinyQueue.size());
    }
}
