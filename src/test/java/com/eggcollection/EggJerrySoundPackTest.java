package com.eggcollection;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.sound.sampled.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class EggJerrySoundPackTest
{
    @Test public void normalPoolExcludesTheSecretAndIdleAlwaysSelectsIt()
    {
        List<String> played=new ArrayList<>();int[] stopped={0};
        Random choices=new Random(){int next;
            @Override public int nextInt(int bound){assertEquals(4,bound);return next++%bound;}
        };
        EggJerrySoundPack pack=new EggJerrySoundPack(choices,played::add,() -> stopped[0]++);
        for(int i=0;i<4;i++){pack.play();assertEquals(i+1,played.size());assertEquals(EggJerrySoundPack.NORMAL[i],played.get(i));}
        assertFalse(played.contains("sounds/jerry-4.wav"));
        pack.play(true);assertEquals("sounds/jerry-4.wav",played.get(4));
        pack.stop();assertEquals(1,stopped[0]);
    }

    @Test public void allRecordingsAreBundledAsNonemptyPortablePcmWav() throws Exception
    {
        assertEquals(5,EggJerrySoundPack.RECORDINGS.length);
        for(String resource:EggJerrySoundPack.RECORDINGS)
        {
            assertNotNull(EggJerrySoundPack.class.getResource(resource));
            try(AudioInputStream stream=AudioSystem.getAudioInputStream(EggJerrySoundPack.class.getResource(resource)))
            {
                assertEquals(AudioFormat.Encoding.PCM_SIGNED,stream.getFormat().getEncoding());
                assertEquals(44100,stream.getFormat().getSampleRate(),0);
                assertTrue(stream.getFrameLength()>0);
            }
        }
    }
}
