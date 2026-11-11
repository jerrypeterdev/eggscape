package com.eggcollection;

import java.io.ByteArrayInputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import net.runelite.client.audio.AudioPlayer;
import org.slf4j.LoggerFactory;

/** RuneLite owns the audio device; short segments keep mute and seek responsive. */
final class EggPcmSound
{
    private static final Map<String,EggWave> WAVES=new ConcurrentHashMap<>();
    private final AudioPlayer audio;
    private final ScheduledExecutorService executor;
    private final AtomicLong generation=new AtomicLong();
    private ScheduledFuture<?> pending;

    EggPcmSound(AudioPlayer audio,ScheduledExecutorService executor)
    {this.audio=audio;this.executor=executor;}

    synchronized void play(String resource,long offsetNanos)
    {
        stop();long request=generation.get();
        pending=executor.schedule(() -> {
            try
            {
                EggWave wave=WAVES.get(resource);
                if(wave==null){wave=EggWave.load(resource);WAVES.put(resource,wave);}
                playSegment(request,wave,wave.frameAt(offsetNanos));
            }
            catch(Exception ex){unavailable(ex);}
        },0,TimeUnit.NANOSECONDS);
    }

    private void playSegment(long request,EggWave wave,long first)
    {
        if(request!=generation.get() || first>=wave.frames())return;
        long count=Math.min(wave.segmentFrames(),wave.frames()-first);
        try
        {
            byte[] segment=wave.slice(first,count);
            if(request!=generation.get())return;
            audio.play(new ByteArrayInputStream(segment),0);
            synchronized(this)
            {
                if(request==generation.get() && first+count<wave.frames())
                    pending=executor.schedule(() -> playSegment(request,wave,first+count),wave.nanosFor(count),TimeUnit.NANOSECONDS);
            }
        }
        catch(Exception ex){unavailable(ex);}
    }

    synchronized void stop()
    {
        generation.incrementAndGet();
        if(pending!=null){pending.cancel(false);pending=null;}
    }

    private static void unavailable(Exception ex)
    {LoggerFactory.getLogger(EggPcmSound.class).debug("EggScape sound unavailable",ex);}
}
