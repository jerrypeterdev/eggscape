package com.eggcollection;

import java.io.IOException;
import java.net.URL;
import javax.sound.sampled.*;

/** The supplied MP3, decoded once to a packaged PCM WAV for standard Java playback. */
final class EggSpinSound implements EggSpinAudio
{
    private static final URL RESOURCE=EggSpinSound.class.getResource("sounds/wheel-spin.wav");
    static final long DURATION_NANOS=duration();
    private final EggPcmSound player=new EggPcmSound();

    private static long duration()
    {
        try(AudioInputStream input=AudioSystem.getAudioInputStream(RESOURCE))
        {return Math.round(input.getFrameLength()*1_000_000_000.0/input.getFormat().getFrameRate());}
        catch(UnsupportedAudioFileException | IOException ex)
        {throw new ExceptionInInitializerError(ex);}
    }

    @Override public void play(long offsetNanos){player.play("sounds/wheel-spin.wav",offsetNanos);}
    @Override public void stop(){player.stop();}
}
