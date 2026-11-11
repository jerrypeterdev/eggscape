package com.eggcollection;

import java.util.concurrent.ScheduledExecutorService;
import javax.inject.Inject;
import net.runelite.client.audio.AudioPlayer;

/** Default hatch-ready cue supplied by the user. */
final class EggReadySound
{
    static final String RESOURCE="sounds/ready-horn.wav";
    private final EggPcmSound player;

    @Inject EggReadySound(AudioPlayer audio,ScheduledExecutorService executor)
    {player=new EggPcmSound(audio,executor);}

    void play(){player.play(RESOURCE,0);}
    void stop(){player.stop();}
}
