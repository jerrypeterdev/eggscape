package com.eggcollection;

import java.util.Random;
import java.util.concurrent.ScheduledExecutorService;
import javax.inject.Inject;
import net.runelite.client.audio.AudioPlayer;
import java.util.function.Consumer;

/** One uniformly chosen recording per ready event; a new recording replaces the old one. */
final class EggJerrySoundPack implements EggReadyAudio
{
    static final String[] RECORDINGS={"sounds/jerry-1.wav","sounds/jerry-2.wav","sounds/jerry-3.wav","sounds/jerry-4.wav","sounds/jerry-5.wav"};
    static final String[] NORMAL={RECORDINGS[0],RECORDINGS[1],RECORDINGS[2],RECORDINGS[4]};
    private final Random random;
    private final Consumer<String> play;
    private final Runnable stop;

    @Inject EggJerrySoundPack(AudioPlayer audio,ScheduledExecutorService executor)
    {this(new Random(),new EggPcmSound(audio,executor));}
    private EggJerrySoundPack(Random random,EggPcmSound player)
    {this(random,path -> player.play(path,0),player::stop);}

    EggJerrySoundPack(Random random,Consumer<String> play,Runnable stop)
    {this.random=random;this.play=play;this.stop=stop;}

    @Override public void play(){play(false);}
    @Override public void play(boolean idle){play.accept(idle?RECORDINGS[3]:NORMAL[random.nextInt(NORMAL.length)]);}
    @Override public void stop(){stop.run();}
}
