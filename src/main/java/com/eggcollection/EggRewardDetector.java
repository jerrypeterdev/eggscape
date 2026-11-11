package com.eggcollection;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/** Correlate native pet notices with confirmed personal boss loot, in either order. */
final class EggRewardDetector
{
    static boolean isJerryPrivateMessage(net.runelite.api.ChatMessageType type,String sender)
    {
        return type==net.runelite.api.ChatMessageType.PRIVATECHAT && EggJerryUnlock.authorized(sender);
    }
    private final Random random;
    private EggPet lastBoss;
    private int lastLoot=-100,pendingPet=-100;
    private final Map<String,Integer> rewarded=new HashMap<>();
    EggRewardDetector(Random random){this.random=random;}
    void reset(){lastBoss=null;lastLoot=-100;pendingPet=-100;rewarded.clear();}
    void loot(EggGame game,EggPet boss,int tick)
    {
        lastBoss=boss;lastLoot=tick;
        if(tick-pendingPet<=20 && EggBossCatalog.hasRealPet(boss))
        {reward(game,boss,EggBossCatalog.dagannoth(boss),tick);pendingPet=-100;}
    }
    void item(EggGame game,String name,int tick)
    {
        name=name.replaceFirst(" \\(\\d+\\)$","");
        if(EggBossCatalog.clean(name).equals("petchaoselemental") && tick-lastLoot>2)
        {pendingPet=tick;return;}
        EggPet pet=EggBossCatalog.petItem(name,tick-lastLoot<=20?lastBoss:null);
        if(pet!=null)reward(game,pet,EggBossCatalog.dagannoth(pet),tick);
    }
    void message(EggGame game,String message,int tick)
    {
        String text=message.replaceAll("<[^>]*>","");
        String log="New item added to your collection log: ";
        String drop="Untradeable drop: ";
        if(text.startsWith(log))item(game,text.substring(log.length()),tick);
        if(text.startsWith(drop))item(game,text.substring(drop.length()),tick);
        if(text.startsWith("You have a funny feeling like you're being followed")
            || text.startsWith("You feel something weird sneaking into your backpack")
            || text.startsWith("You have a funny feeling like you would have been followed"))
        {
            pendingPet=tick;
            if(tick-lastLoot<=2 && EggBossCatalog.hasRealPet(lastBoss))
            {reward(game,lastBoss,EggBossCatalog.dagannoth(lastBoss),tick);pendingPet=-100;}
        }
    }
    private void reward(EggGame game,EggPet boss,boolean specific,int tick)
    {
        String family=EggBossCatalog.family(boss);
        if(tick-rewarded.getOrDefault(family,-100)<=20)return;
        if(game.unlockRealPet(boss,specific,random))rewarded.put(family,tick);
    }
}
