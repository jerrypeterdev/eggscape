package com.eggcollection;

/** Default hatch-ready cue supplied by the user, played off the UI/client threads. */
final class EggReadySound
{
    static final String RESOURCE="sounds/ready-horn.wav";
    private static final EggPcmSound PLAYER=new EggPcmSound();

    private EggReadySound() {}

    static void play(){PLAYER.play(RESOURCE,0);}
}
