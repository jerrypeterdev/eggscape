package com.eggcollection;

import org.junit.Test;
import static org.junit.Assert.*;

public class EggActivityTrackerTest
{
    @Test public void aBankerFacingThePlayerDoesNotKeepThemActiveForever()
    {
        int[] level={0};boolean[] dead={false};net.runelite.api.Player[] player={null};
        net.runelite.api.NPC npc=(net.runelite.api.NPC)java.lang.reflect.Proxy.newProxyInstance(
            getClass().getClassLoader(),new Class<?>[]{net.runelite.api.NPC.class},(proxy,method,args)-> {
                switch(method.getName()){
                    case "getCombatLevel":return level[0];case "isDead":return dead[0];
                    case "getInteracting":return player[0];default:return null;
                }
            });
        player[0]=(net.runelite.api.Player)java.lang.reflect.Proxy.newProxyInstance(
            getClass().getClassLoader(),new Class<?>[]{net.runelite.api.Player.class},(proxy,method,args)-> {
                if(method.getName().equals("getInteracting"))return npc;
                if(method.getName().equals("getHealthRatio"))return -1;
                return null;
            });
        assertFalse(EggActivityTracker.isCombat(player[0]));
        level[0]=100;assertTrue(EggActivityTracker.isCombat(player[0]));
        dead[0]=true;assertFalse(EggActivityTracker.isCombat(player[0]));
    }

    @Test public void activeHourAndIdleTwoHoursBothProduceOneEgg()
    {
        for(boolean active:new boolean[]{true,false})
        {
            EggActivityTracker tracker=new EggActivityTracker();EggGame game=new EggGame();
            game.migrateCollection();game.assignEgg();game.activePet=EggPet.JAD;tracker.update(0,active);
            int seconds=active?3600:7200;
            for(int second=1;second<=seconds;second++)game.advance(tracker.update(second*1_000_000_000L,active));
            assertEquals(EggGame.HATCH_MILLIS,game.hatchProgress);assertTrue(game.canHatch());
        }
    }

    @Test public void afkCombatCanRemainFullyActiveWithoutAnyFurtherClicks()
    {
        EggActivityTracker tracker=new EggActivityTracker();tracker.update(0,false);tracker.action(0);
        long total=0;
        for(int second=1;second<=600;second++)
            total+=tracker.update(second*1_000_000_000L,second%5==0);
        assertEquals(600_000,total);assertTrue(tracker.isActive());
    }

    @Test public void finishingAnActivityDropsToHalfRateAfterGraceAndMovementResumesFullRate()
    {
        EggActivityTracker tracker=new EggActivityTracker();tracker.update(0,true);
        long total=0;
        for(int second=1;second<=120;second++)total+=tracker.update(second*1_000_000_000L,false);
        assertEquals(90_000,total);assertFalse(tracker.isActive());
        assertEquals(1000,tracker.update(121_000_000_000L,true));assertTrue(tracker.isActive());
    }

    @Test public void offlineTimeAndClientStallsCannotBankFreeHours()
    {
        EggActivityTracker tracker=new EggActivityTracker();tracker.update(0,true);
        assertTrue(tracker.update(3_600_000_000_000L,false)<=2000);
        tracker.pause();assertEquals(0,tracker.update(7_200_000_000_000L,false));
        assertEquals(500,tracker.update(7_201_000_000_000L,false));
    }

    @Test public void fractionalIdleTicksDoNotLoseTime()
    {
        EggActivityTracker tracker=new EggActivityTracker();tracker.update(0,false);long total=0;
        for(int step=1;step<=1000;step++)total+=tracker.update(step*1_500_000L,false);
        assertEquals(750,total);
    }
}
