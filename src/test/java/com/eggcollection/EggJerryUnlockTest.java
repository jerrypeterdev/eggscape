package com.eggcollection;

import java.lang.reflect.Proxy;
import net.runelite.api.Player;
import net.runelite.api.PlayerComposition;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.gameval.AnimationID;
import net.runelite.api.gameval.ItemID;
import org.junit.Test;
import static org.junit.Assert.*;

public class EggJerryUnlockTest
{
    private static class Punch
    {
        String name="Mouldy";
        int weapon=ItemID.BEACHPARTY_BOXINGGLOVES_PURPLE,animation=AnimationID.HUMAN_BOXING_JAB,distance=1;
        Player local=(Player)Proxy.newProxyInstance(getClass().getClassLoader(),new Class<?>[]{Player.class},
            (p,m,a)->m.getName().equals("getWorldLocation")?new WorldPoint(3200,3200,0):null);
        Player target=local;
        PlayerComposition equipment=(PlayerComposition)Proxy.newProxyInstance(getClass().getClassLoader(),new Class<?>[]{PlayerComposition.class},
            (p,m,a)->m.getName().equals("getEquipmentId")?weapon:null);
        Player attacker=(Player)Proxy.newProxyInstance(getClass().getClassLoader(),new Class<?>[]{Player.class},(p,m,a)-> {
            switch(m.getName())
            {
                case "getName":return name;case "getPlayerComposition":return equipment;
                case "getAnimation":return animation;case "getInteracting":return target;
                case "getWorldLocation":return new WorldPoint(3200+distance,3200,0);
                default:return null;
            }
        });
        boolean qualifies(){return EggJerryUnlock.punchedLocalPlayer(attacker,local);}
    }

    @Test public void eitherApprovedPlayerCanPunchTheLocalPlayerWithPinkGloves()
    {
        Punch p=new Punch();assertTrue(p.qualifies());p.name="jerrypeter";assertTrue(p.qualifies());
        assertFalse(EggJerryUnlock.punchedLocalPlayer(p.local,p.local));
    }
    @Test public void yellowAndHouseGlovesNeverQualify()
    {
        Punch p=new Punch();p.weapon=ItemID.BEACHPARTY_BOXINGGLOVES_YELLOW;assertFalse(p.qualifies());
        p.weapon=ItemID.POH_BOXING_GLOVES_RED;assertFalse(p.qualifies());
    }
    @Test public void standingNearbyOrPunchingSomebodyElseDoesNotUnlockJerry()
    {
        Punch p=new Punch();p.animation=AnimationID.HUMAN_BOXING_READY;assertFalse(p.qualifies());
        p.animation=AnimationID.HUMAN_BOXING_JAB;p.target=null;assertFalse(p.qualifies());
        p.target=p.attacker;assertFalse(p.qualifies());p.target=p.local;p.distance=10;assertFalse(p.qualifies());
    }
    @Test public void wrongNamesAndNonPunchAnimationsAreRejected()
    {
        Punch p=new Punch();for(String name:new String[]{"Moldy","Jerry Peter","Mouldy ","Someone else"})
        {p.name=name;assertFalse(p.qualifies());}
        p.name="Mouldy";p.animation=AnimationID.HUMAN_BOXING_BLOCK;assertFalse(p.qualifies());
        p.animation=-1;assertFalse(p.qualifies());
    }
}
