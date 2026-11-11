package com.eggcollection;

import net.runelite.api.Player;
import net.runelite.api.PlayerComposition;
import net.runelite.api.gameval.AnimationID;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.kit.KitType;

/** Exact organiser identities and an observed, directed pink-glove punch. */
final class EggJerryUnlock
{
    static String bareName(String name){return name==null?"":name.replaceAll("<[^>]*>","");}
    static boolean authorized(String name)
    {String plain=bareName(name);return plain.equals("jerrypeter") || plain.equals("Mouldy");}

    static boolean punchedLocalPlayer(Player attacker,Player local)
    {
        if(attacker==null || local==null || attacker==local || !authorized(attacker.getName()))return false;
        PlayerComposition equipment=attacker.getPlayerComposition();
        // The cache calls the pink item PURPLE; 11705 is the yellow version.
        if(equipment==null || equipment.getEquipmentId(KitType.WEAPON)!=ItemID.BEACHPARTY_BOXINGGLOVES_PURPLE)return false;
        int animation=attacker.getAnimation();
        if(animation!=AnimationID.HUMAN_BOXING_JAB && animation!=AnimationID.HUMAN_UNARMEDPUNCH
            && animation!=AnimationID.HUMAN_UNARMEDPUNCH_QUICK)return false;
        return attacker.getInteracting()==local && attacker.getWorldLocation()!=null && local.getWorldLocation()!=null
            && attacker.getWorldLocation().distanceTo(local.getWorldLocation())<=2;
    }
}
