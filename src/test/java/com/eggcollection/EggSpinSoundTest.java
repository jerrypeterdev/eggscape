package com.eggcollection;

import javax.sound.sampled.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class EggSpinSoundTest
{
    @Test public void defaultReadyHornIsBundledAndDecodable() throws Exception
    {
        try(AudioInputStream stream=AudioSystem.getAudioInputStream(EggReadySound.class.getResource(EggReadySound.RESOURCE)))
        {
            assertEquals(AudioFormat.Encoding.PCM_SIGNED,stream.getFormat().getEncoding());
            assertEquals(44100,stream.getFormat().getSampleRate(),0);
            assertTrue(stream.getFrameLength()>0);
        }
    }

    @Test public void packagedSoundIsPcmAndWheelDurationMatchesItsActualLength() throws Exception
    {
        try(AudioInputStream stream=AudioSystem.getAudioInputStream(EggSpinSound.class.getResource("sounds/wheel-spin.wav")))
        {
            assertEquals(AudioFormat.Encoding.PCM_SIGNED,stream.getFormat().getEncoding());
            assertTrue(stream.getFrameLength()>0);
            long duration=Math.round(stream.getFrameLength()*1_000_000_000.0/stream.getFormat().getFrameRate());
            assertEquals(duration,EggRoulette.SPIN_NANOS);assertTrue(duration>2_000_000_000L);
        }
    }
}
