package com.eggcollection;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import javax.sound.sampled.*;
import org.slf4j.LoggerFactory;

/** All audio-device work stays off the game/UI threads, with one live clip per player. */
final class EggPcmSound
{
    private static final ExecutorService AUDIO=Executors.newSingleThreadExecutor(task -> {
        Thread thread=new Thread(task,"egg-collection-recordings");thread.setDaemon(true);return thread;
    });
    private final AtomicLong generation=new AtomicLong();
    private Clip clip;

    void play(String resource,long offsetNanos)
    {
        long request=generation.incrementAndGet();
        AUDIO.execute(() -> {
            if(request!=generation.get())return;
            closeClip();
            try(AudioInputStream input=AudioSystem.getAudioInputStream(EggPcmSound.class.getResource(resource)))
            {
                Clip opened=AudioSystem.getClip();
                try
                {
                    opened.open(input);
                    if(request!=generation.get()){opened.close();return;}
                    opened.setMicrosecondPosition(Math.max(0,Math.min(opened.getMicrosecondLength(),offsetNanos/1000)));
                    opened.addLineListener(event -> {
                        if(event.getType()==LineEvent.Type.STOP)
                            AUDIO.execute(() -> {if(clip==opened)closeClip();});
                    });
                    clip=opened;opened.start();
                }
                catch(LineUnavailableException | IOException | RuntimeException ex){opened.close();throw ex;}
            }
            catch(UnsupportedAudioFileException | IOException | LineUnavailableException | IllegalArgumentException | SecurityException ex)
            {LoggerFactory.getLogger(EggPcmSound.class).debug("Egg Collection sound unavailable",ex);}
        });
    }

    void stop(){generation.incrementAndGet();AUDIO.execute(this::closeClip);}

    private void closeClip()
    {
        Clip closing=clip;clip=null;
        if(closing!=null){closing.stop();closing.close();}
    }
}
