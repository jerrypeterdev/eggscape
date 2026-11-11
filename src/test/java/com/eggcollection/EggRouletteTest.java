package com.eggcollection;

import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import net.runelite.client.util.ImageUtil;
import org.junit.Test;
import static org.junit.Assert.*;

public class EggRouletteTest
{
    @Test public void reelMovesDownSlowsAndStopsOnTheCommittedEggExactlyOnce() throws Exception
    {
        SwingUtilities.invokeAndWait(() -> {
            Map<EggPet,ImageIcon> icons=new EnumMap<>(EggPet.class);
            for(EggPet pet:EggGame.PETS)
                icons.put(pet,new ImageIcon(ImageUtil.loadImageResource(EggPlugin.class,pet.eggResource)));
            AtomicLong clock=new AtomicLong();int[] requests={0},finishes={0};
            EggRoulette reel=new EggRoulette(icons,() -> requests[0]++,() -> finishes[0]++,clock::get);
            reel.setSize(195,900);reel.doLayout();
            JButton spin=(JButton)reel.getComponent(0);
            assertEquals(450,spin.getY()+spin.getHeight()/2);
            assertTrue(spin.getX()+spin.getWidth()<=reel.getWidth());
            assertEquals(Math.max(48,(reel.getWidth()-60)/2)-61,reel.getWidth()-spin.getX()-spin.getWidth());
            JToggleButton sound=(JToggleButton)reel.getComponent(1);
            assertEquals(spin.getX()+spin.getWidth()/2,sound.getX()+sound.getWidth()/2);
            assertTrue(sound.getY()>spin.getY()+spin.getHeight());
            spin.doClick(0);spin.doClick(0);assertEquals(1,requests[0]);
            reel.begin(EggPet.NEX);
            clock.set(EggRoulette.SPIN_NANOS/4);reel.tick();double firstSecond=reel.position();
            assertTrue(firstSecond>0);
            clock.set(EggRoulette.SPIN_NANOS/2);reel.tick();double atThree=reel.position();
            clock.set(EggRoulette.SPIN_NANOS*3/4);reel.tick();double lateSecond=reel.position()-atThree;
            assertTrue(lateSecond>0 && lateSecond<firstSecond);
            clock.set(EggRoulette.SPIN_NANOS);reel.tick();
            assertEquals(43*124,reel.position(),0.001);
            assertSame(EggPet.NEX,reel.centeredEgg());assertEquals(0,finishes[0]);
            assertTrue(reel.isActive());assertFalse(spin.isEnabled());
            clock.set(EggRoulette.SPIN_NANOS+EggRoulette.HOLD_NANOS);reel.tick();reel.tick();
            assertFalse(reel.isActive());assertEquals(1,finishes[0]);
        });
    }

    @Test public void soundStartsOnceMutesImmediatelyAndUnmutesAtTheCurrentOffset() throws Exception
    {
        SwingUtilities.invokeAndWait(() -> {
            Map<EggPet,ImageIcon> icons=new EnumMap<>(EggPet.class);
            for(EggPet pet:EggGame.PETS)icons.put(pet,new ImageIcon(ImageUtil.loadImageResource(EggPlugin.class,pet.eggResource)));
            AtomicLong clock=new AtomicLong();int[] plays={0},stops={0},saves={0};long[] offset={-1};
            EggSpinAudio audio=new EggSpinAudio(){
                public void play(long nanos){plays[0]++;offset[0]=nanos;}
                public void stop(){stops[0]++;}
            };
            EggRoulette reel=new EggRoulette(icons,() -> {},() -> {},clock::get,audio);
            reel.setMuteAction(muted -> saves[0]++);
            reel.begin(EggPet.NEX);assertEquals(1,plays[0]);assertEquals(0,offset[0]);
            clock.set(EggRoulette.SPIN_NANOS/3);reel.tick();assertEquals(1,plays[0]);
            JToggleButton mute=(JToggleButton)reel.getComponent(1);mute.doClick(0);
            assertTrue(reel.isMuted());assertTrue(stops[0]>0);assertEquals(1,saves[0]);
            mute.doClick(0);assertFalse(reel.isMuted());assertEquals(2,plays[0]);
            assertEquals(clock.get(),offset[0]);assertEquals(2,saves[0]);
            reel.cancel();int stopCount=stops[0];
            reel.setMuted(true);reel.begin(EggPet.MOLE);assertEquals(2,plays[0]);
            assertTrue(reel.isMuted());reel.cancel();assertTrue(stops[0]>stopCount);
        });
    }
}
