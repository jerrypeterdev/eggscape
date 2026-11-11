package com.eggcollection;

/** Monotonic online time, independent of XP rates and account levels. */
class EggActivityTracker
{
    static boolean isCombat(net.runelite.api.Player player)
    {
        if(!(player.getInteracting() instanceof net.runelite.api.NPC))return false;
        net.runelite.api.NPC npc=(net.runelite.api.NPC)player.getInteracting();
        return npc.getCombatLevel()>0 && !npc.isDead()
            && (npc.getInteracting()==player || player.getHealthRatio()>=0);
    }
    static final long GRACE_NANOS=60_000_000_000L;
    private long previous=-1,activeUntil=-1;
    private boolean active;
    private long halfRemainder;

    void reset(){previous=-1;activeUntil=-1;active=false;halfRemainder=0;}
    void pause(){previous=-1;}
    void action(long now){activeUntil=now+GRACE_NANOS;}
    boolean isActive(){return active;}

    long update(long now,boolean evidence)
    {
        if(evidence)action(now);
        active=now<activeUntil;
        if(previous<0){previous=now;return 0;}
        long from=previous;previous=now;
        // Never credit a stalled client or a gap between login sessions.
        long elapsed=Math.max(0,Math.min(2_000_000_000L,now-from));
        long activeNanos=Math.max(0,Math.min(elapsed,activeUntil-(now-elapsed)));
        long weighted=activeNanos+(elapsed-activeNanos)/2+halfRemainder;
        long millis=weighted/1_000_000L;halfRemainder=weighted%1_000_000L;
        return millis;
    }
}
