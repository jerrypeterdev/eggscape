package com.eggcollection;

/** Accumulate actual active encounter time; pay it once, only on confirmed loot. */
final class EggBossTracker
{
    private EggPet egg;
    private long earned;
    private long gap;
    void reset(){egg=null;earned=0;gap=0;}
    void observe(EggPet current,long elapsed,boolean active,EggPet target,boolean inBarrows)
    {
        if(current!=egg){egg=current;earned=0;gap=0;}
        if(current!=null && active && (EggBossCatalog.same(current,target)
            || (inBarrows && EggBossCatalog.same(current,EggPet.AHRIM))))
        {earned+=Math.max(0,Math.min(2000,elapsed));gap=0;}
        else if((gap+=Math.max(0,Math.min(2000,elapsed)))>120_000
            && !EggBossCatalog.groupLogUnlock(current) && !EggBossCatalog.raid(current))earned=0;
    }
    long claim(EggPet current,EggPet killed)
    {
        if(current!=egg || !EggBossCatalog.same(current,killed))return 0;
        long result=earned;earned=0;return result;
    }
}
