package com.eggcollection;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Delayed;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import net.runelite.client.audio.AudioPlayer;
import org.junit.Test;
import static org.junit.Assert.*;

public class EggAudioCompatibilityTest
{
    @Test public void everyRecordingReassemblesWithoutDroppedOrRepeatedFrames() throws Exception
    {
        List<String> resources=new ArrayList<>(List.of(EggJerrySoundPack.RECORDINGS));
        resources.add(EggReadySound.RESOURCE);resources.add("sounds/wheel-spin.wav");
        for(String resource:resources)
        {
            ManualExecutor executor=new ManualExecutor();CapturingAudio audio=new CapturingAudio();
            try
            {
                EggPcmSound player=new EggPcmSound(audio,executor);player.play(resource,0);
                while(!executor.tasks.isEmpty())executor.next();
                ByteArrayOutputStream result=new ByteArrayOutputStream();
                for(byte[] segment:audio.segments)
                    try(AudioInputStream input=AudioSystem.getAudioInputStream(new ByteArrayInputStream(segment)))
                    {
                        assertTrue(input.getFrameLength()/input.getFormat().getFrameRate()<=0.251);
                        result.write(input.readAllBytes());
                    }
                try(AudioInputStream original=AudioSystem.getAudioInputStream(EggWave.class.getResource(resource)))
                {assertArrayEquals(resource,original.readAllBytes(),result.toByteArray());}
            }
            finally{executor.shutdownNow();}
        }
    }

    @Test public void muteCancelsFurtherPlaybackAndResumeStartsAtTheRequestedFrame() throws Exception
    {
        ManualExecutor executor=new ManualExecutor();CapturingAudio audio=new CapturingAudio();
        try
        {
            EggPcmSound player=new EggPcmSound(audio,executor);
            player.play("sounds/wheel-spin.wav",0);executor.next();assertEquals(1,audio.segments.size());
            player.stop();executor.next();assertEquals(1,audio.segments.size());
            long offset=1_234_567_890L;
            player.play("sounds/wheel-spin.wav",offset);executor.next();
            EggWave wave=EggWave.load("sounds/wheel-spin.wav");
            assertArrayEquals(wave.slice(wave.frameAt(offset),wave.segmentFrames()),audio.segments.get(1));
            player.stop();while(!executor.tasks.isEmpty())executor.next();
            assertEquals(2,audio.segments.size());
            player.play("sounds/wheel-spin.wav",Long.MAX_VALUE);executor.next();
            assertEquals(2,audio.segments.size());
            player.play("sounds/wheel-spin.wav",0);player.stop();executor.next();
            assertEquals(2,audio.segments.size());
        }
        finally{executor.shutdownNow();}
    }

    private static class CapturingAudio extends AudioPlayer
    {
        final List<byte[]> segments=new ArrayList<>();
        @Override public void play(InputStream stream,float gain) throws IOException
        {assertEquals(0,gain,0);segments.add(stream.readAllBytes());}
    }

    private static class ManualExecutor extends ScheduledThreadPoolExecutor
    {
        final Queue<Task> tasks=new ArrayDeque<>();
        ManualExecutor(){super(1);}
        @Override public ScheduledFuture<?> schedule(Runnable action,long delay,TimeUnit unit)
        {Task task=new Task(action);tasks.add(task);return task;}
        void next(){tasks.remove().run();}
    }

    private static class Task extends FutureTask<Void> implements ScheduledFuture<Void>
    {
        Task(Runnable action){super(action,null);}
        @Override public long getDelay(TimeUnit unit){return 0;}
        @Override public int compareTo(Delayed other){return 0;}
    }
}
