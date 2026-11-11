package com.eggcollection;

import java.io.IOException;
import java.util.concurrent.ScheduledExecutorService;
import javax.inject.Inject;
import net.runelite.client.audio.AudioPlayer;

/** The supplied wheel recording, kept in sync when returning to the sidebar. */
final class EggSpinSound implements EggSpinAudio
{
    static final long DURATION_NANOS=duration();
    private final EggPcmSound player;

    @Inject EggSpinSound(AudioPlayer audio,ScheduledExecutorService executor)
    {player=new EggPcmSound(audio,executor);}

    private static long duration()
    {
        try{return EggWave.load("sounds/wheel-spin.wav").durationNanos();}
        catch(IOException ex){throw new ExceptionInInitializerError(ex);}
    }

    @Override public void play(long offsetNanos){player.play("sounds/wheel-spin.wav",offsetNanos);}
    @Override public void stop(){player.stop();}
}
