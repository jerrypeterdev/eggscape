package com.eggcollection;

import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import javax.imageio.ImageIO;
import javax.swing.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class EggPanelTest
{
    @Test public void normalPlayerLaunchOmitsAllThreeCheatsButKeepsNormalHatching() throws Exception
    {
        String previous=System.getProperty("eggscape.developerTools");
        System.clearProperty("eggscape.developerTools");
        try
        {
            SwingUtilities.invokeAndWait(() -> {
                try
                {
                    assertFalse(EggDevelopment.enabled());
                    EggPanel panel=new EggPanel(() -> {},System::nanoTime,
                        EggSpinAudio.SILENT,EggReadyAudio.SILENT,EggDevelopment.enabled());
                    EggGame game=new EggGame();game.migrateCollection();game.assignEgg();
                    panel.showGame(game);
                    assertNull(button(panel,"debug-pet-drop"));
                    assertNull(button(panel,"skip-to-hatch"));
                    assertNull(button(panel,"reset-progress"));
                    assertNotNull(button(panel,"egg-choice"));
                    render(panel,"release-overview-padding",950);
                    game.advance(EggGame.TUTORIAL_MILLIS);
                    panel.showGame(game,game::hatch);
                    assertEquals("Hatch",button(panel,"egg-progress").getText());
                    button(panel,"egg-progress").doClick(0);
                    assertEquals(1,game.uniquePets());
                    panel.showGame(null);
                }
                catch(Exception ex){throw new RuntimeException(ex);}
            });
        }
        finally
        {
            if(previous==null)System.clearProperty("eggscape.developerTools");
            else System.setProperty("eggscape.developerTools",previous);
        }
    }

    @Test public void secretCueUsesSavedIdleCrossingAndDoesNotRepeat() throws Exception
    {
        SwingUtilities.invokeAndWait(() -> {
            java.util.List<Boolean> heard=new java.util.ArrayList<>();
            EggReadyAudio audio=new EggReadyAudio()
            {public void play(){throw new AssertionError("Missing idle context");}
             public void play(boolean idle){heard.add(idle);}public void stop(){}};
            EggPanel panel=new EggPanel(() -> {},System::nanoTime,EggSpinAudio.SILENT,audio);
            panel.setJerrySoundPackEnabled(true);EggGame game=new EggGame();game.migrateCollection();game.assignEgg();
            panel.showGame(game);game.activeGameplay=false;game.advance(EggGame.HATCH_MILLIS);
            game.activeGameplay=true;panel.showGame(game);panel.showGame(game);
            assertEquals(java.util.List.of(true),heard);
            game.hatch();panel.showGame(game);game.spin(new Random(0));game.activeGameplay=true;
            game.advance(EggGame.HATCH_MILLIS);game.activeGameplay=false;panel.showGame(game);
            assertEquals(java.util.List.of(true,false),heard);panel.showGame(null);
        });
    }

    @Test public void debugPetDropQueuesNextEggAndShinyUpgradesTheSameCollectionEntry() throws Exception
    {
        SwingUtilities.invokeAndWait(() -> {
            try
            {
                Harness h=new Harness();assertFalse(button(h.panel,"debug-pet-drop").isEnabled());h.choose();
                h.game.activePet=EggPet.MOLE;h.show();button(h.panel,"debug-pet-drop").doClick(0);
                JButton queue=button(h.panel,"debug-pet-drop"),skip=button(h.panel,"skip-to-hatch");
                render(h.panel,"queue-shiny-footer",1050);
                assertEquals("Queue Shiny Egg",queue.getText());assertFalse(queue.isEnabled());
                assertEquals(skip.getFont(),queue.getFont());assertEquals(skip.getForeground(),queue.getForeground());
                assertEquals(skip.getBackground(),queue.getBackground());assertEquals(skip.getSize(),queue.getSize());
                assertTrue(SwingUtilities.convertPoint(queue,0,0,h.panel).y<SwingUtilities.convertPoint(skip,0,0,h.panel).y);
                assertFalse(SwingUtilities.isDescendingFrom(queue,find(h.panel,JTabbedPane.class).getComponentAt(0)));
                queue.doClick(0);assertEquals(1,h.game.shinyQueue.size());
                assertSame(EggPet.MOLE,h.game.activePet);assertFalse(h.game.activeShiny);assertEquals(1,h.game.shinyQueue.size());
                h.game.skipToHatch();h.hatch();h.panel.finishReveal();button(h.panel,"spin-egg").doClick(0);
                assertTrue(h.game.activeShiny);assertSame(EggPet.MOLE,h.game.activePet);
                h.clock.set(EggRoulette.SPIN_NANOS+EggRoulette.HOLD_NANOS);find(h.panel,EggRoulette.class).tick();
                render(h.panel,"shiny-mole-egg",850);
                h.game.skipToHatch();h.hatch();h.panel.finishReveal();
                assertEquals(1,h.game.shinyUniquePets());assertEquals(1,h.game.uniquePets());
                assertEquals(2,h.game.collectedCount(EggPet.MOLE));
                find(h.panel,JTabbedPane.class).setSelectedIndex(1);
                assertNull(namedToggle(h.panel,"shiny-collection"));
                assertNotNull(tooltip(h.panel,"Shiny Giant Mole (hatched: 2)"));
                assertNull(tooltip(h.panel,"Giant Mole (hatched: 2)"));
                checkbox(h.panel,"hide-unobtained").doClick(0);
                assertNull(tooltip(h.panel,"Shiny Ahrim Egg (not yet hatched)"));
                render(h.panel,"shiny-collection",850);h.panel.showGame(null);
            }
            catch(Exception ex){throw new RuntimeException(ex);}
        });
    }

    private static JToggleButton namedToggle(Container root,String name)
    {
        for(Component c:root.getComponents())
        {
            if(c instanceof JToggleButton && name.equals(c.getName()))return (JToggleButton)c;
            if(c instanceof Container){JToggleButton found=namedToggle((Container)c,name);if(found!=null)return found;}
        }
        return null;
    }

    @Test public void collectionFiltersAndDragScrollingStayInsideThePanel() throws Exception
    {
        SwingUtilities.invokeAndWait(() -> {
            try
            {
                Harness h=new Harness();h.game.collection.put(EggPet.JAD.key,1);h.show();
                JTabbedPane tabs=find(h.panel,JTabbedPane.class);tabs.setSelectedIndex(1);
                render(h.panel,"collection-inset-scroll",850);
                EggCollectionScroll scroll=find(h.panel,EggCollectionScroll.class);
                assertTrue(scroll.getViewport().getViewSize().height>scroll.getViewport().getExtentSize().height);
                assertEquals(6,scroll.getVerticalScrollBar().getWidth());
                assertTrue(SwingUtilities.convertPoint(scroll,0,0,tabs.getSelectedComponent()).x>=0);
                JLabel art=tooltip(h.panel,"Ahrim Egg (not yet hatched)");
                drag(art,30,-130);assertEquals(160,scroll.getViewport().getViewPosition().y);
                h.show();capture(h.panel,850);assertEquals(160,scroll.getViewport().getViewPosition().y);
                assertSame(art,tooltip(h.panel,"Ahrim Egg (not yet hatched)"));
                drag(scroll.getViewport(),30,300);assertEquals(0,scroll.getViewport().getViewPosition().y);
                JCheckBox unobtained=checkbox(h.panel,"hide-unobtained"),collected=checkbox(h.panel,"hide-collected");
                unobtained.doClick(0);capture(h.panel,850);
                assertNull(tooltip(h.panel,"Ahrim Egg (not yet hatched)"));
                assertNotNull(tooltip(h.panel,"Jad (hatched: 1)"));
                assertEquals(1,h.game.uniquePets());
                collected.doClick(0);capture(h.panel,850);assertNull(tooltip(h.panel,"Jad (hatched: 1)"));
                unobtained.doClick(0);capture(h.panel,850);
                assertNotNull(tooltip(h.panel,"Ahrim Egg (not yet hatched)"));
                assertNull(tooltip(h.panel,"Jad (hatched: 1)"));
                assertTrue(collected.isSelected());
                java.awt.Point bottom=SwingUtilities.convertPoint(collected,0,collected.getHeight(),tabs.getSelectedComponent());
                assertTrue(bottom.y<=tabs.getSelectedComponent().getHeight());
                render(h.panel,"collection-hide-collected",850);
                collected.doClick(0);render(h.panel,"collection-filters-off",850);
                h.game.activePet=EggPet.REVENANT_MALEDICTUS;h.show();tabs.setSelectedIndex(0);
                render(h.panel,"overview-runescape-font",1050);
                assertEquals(net.runelite.client.ui.FontManager.getRunescapeFont().getFamily(),
                    checkbox(h.panel,"jerry-sound-pack").getFont().getFamily());
                h.panel.showGame(null);
            }
            catch(Exception ex){throw new RuntimeException(ex);}
        });
    }

    private static void drag(Component target,int fromY,int toY)
    {
        long time=System.currentTimeMillis();
        target.dispatchEvent(new java.awt.event.MouseEvent(target,java.awt.event.MouseEvent.MOUSE_PRESSED,time,
            java.awt.event.InputEvent.BUTTON1_DOWN_MASK,20,fromY,1,false,java.awt.event.MouseEvent.BUTTON1));
        target.dispatchEvent(new java.awt.event.MouseEvent(target,java.awt.event.MouseEvent.MOUSE_DRAGGED,time+1,
            java.awt.event.InputEvent.BUTTON1_DOWN_MASK,20,toY,0,false,java.awt.event.MouseEvent.NOBUTTON));
        target.dispatchEvent(new java.awt.event.MouseEvent(target,java.awt.event.MouseEvent.MOUSE_RELEASED,time+2,
            0,20,toY,1,false,java.awt.event.MouseEvent.BUTTON1));
    }

    private static JCheckBox checkbox(Container root,String name)
    {
        for(Component c:root.getComponents())
        {
            if(c instanceof JCheckBox && name.equals(c.getName()))return (JCheckBox)c;
            if(c instanceof Container){JCheckBox result=checkbox((Container)c,name);if(result!=null)return result;}
        }
        return null;
    }

    private static class Harness
    {
        EggGame game=new EggGame();
        int dings;
        final AtomicLong clock=new AtomicLong();
        final EggPanel panel=new EggPanel(() -> dings++,clock::get);
        final Random random=new Random(){@Override public int nextInt(int bound){return 0;}};
        Harness(){game.migrateCollection();panel.setDebugPetDropAction(()->{game.debugPetDrop();show();});show();}
        void show(){panel.showGame(game,this::hatch,this::choose,this::reset,this::spin,this::skip,null,null);}
        void choose(){game.assignEgg();show();}
        void hatch(){EggPet reward=game.hatch();if(reward!=null)panel.showGame(game,this::hatch,this::choose,this::reset,this::spin,this::skip,reward,null);}
        void spin(){EggPet selected=game.spin(random);if(selected!=null)panel.showGame(game,this::hatch,this::choose,this::reset,this::spin,this::skip,null,selected);}
        void skip(){game.skipToHatch();show();}
        void reset(){game=new EggGame();game.migrateCollection();show();}
    }

    @Test public void mysteryEggRequiresOneClickAndDoesNotBankPreChoiceXp() throws Exception
    {
        SwingUtilities.invokeAndWait(() -> {
            try
            {
                Harness h=new Harness();
                JButton mystery=button(h.panel,"egg-choice");
                assertNull(mystery.getToolTipText());
                assertEquals(java.awt.Cursor.DEFAULT_CURSOR,mystery.getCursor().getType());
                assertTrue(mystery.isEnabled()); assertFalse(button(h.panel,"egg-progress").isEnabled());
                assertEquals("Hatch Progress: 0%",button(h.panel,"egg-progress").getText());
                h.game.advance(18000000L); assertEquals(0L,h.game.hatchProgress);
                assertEquals(0,h.game.uniquePets()); assertTrue(h.game.collection.isEmpty());
                render(h.panel,"new-player-mystery",850);
                mystery.doClick(0); mystery.doClick(0);
                assertSame(EggPet.JERRY,h.game.activePet);
                assertFalse(button(h.panel,"egg-choice").isEnabled());
                assertEquals(0L,h.game.hatchProgress); assertEquals(0,h.dings);
                render(h.panel,"new-player-assigned-egg",850);
            }
            catch(Exception ex){throw new RuntimeException(ex);}
        });
    }

    @Test public void thresholdDingsOnceThenClickHatchesAssignedPetAndCarriesXp() throws Exception
    {
        SwingUtilities.invokeAndWait(() -> {
            try
            {
                Harness h=new Harness(); h.choose();
                h.game.advance(899100L);h.show();
                assertEquals("Hatch Progress: 99%",button(h.panel,"egg-progress").getText());
                assertEquals(0,h.dings); assertFalse(h.panel.isReadyToHatch());
                h.game.advance(900L);h.show();
                assertEquals("Hatch",button(h.panel,"egg-progress").getText());
                assertEquals(1,h.dings);assertTrue(h.panel.isReadyToHatch());
                h.game.advance(273600L);h.show();h.show();assertEquals(1,h.dings);
                render(h.panel,"overview-ready-to-hatch",850);
                JButton hatch=button(h.panel,"egg-progress");hatch.doClick(0);hatch.doClick(0);
                assertTrue(h.panel.isRevealing());assertFalse(h.panel.isReadyToHatch());
                assertEquals(Integer.valueOf(1),h.game.collection.get(EggPet.JERRY.key));
                assertEquals(273600L,h.game.hatchProgress);
                assertEquals("Hatch Progress: 7%",button(h.panel,"egg-progress").getText());
                h.panel.finishReveal();assertFalse(h.panel.isRevealing());
                assertNull(button(h.panel,"egg-choice"));
                assertNotNull(button(h.panel,"spin-egg"));
                assertNull(button(h.panel,"egg-progress"));
                render(h.panel,"roulette-ready",850);
                JToggleButton sound=find(h.panel,JToggleButton.class);
                sound.doClick(0);assertTrue(sound.isSelected());
                render(h.panel,"roulette-muted",850);
                sound.doClick(0);assertFalse(sound.isSelected());
                writeRouletteDemo(h);
                find(h.panel,JTabbedPane.class).setSelectedIndex(1);
                assertNotNull(tooltip(h.panel,"Jerry (hatched: 1)"));
                render(h.panel,"collection-first-pet",1240);
            }
            catch(Exception ex){throw new RuntimeException(ex);}
        });
    }

    @Test public void moleRevealLeadsToWheelThenSavedSpinResultAndXpKeepsBanking() throws Exception
    {
        Harness[] holder=new Harness[1];
        SwingUtilities.invokeAndWait(() -> {
            Harness h=new Harness();holder[0]=h;
            h.game.activePet=EggPet.MOLE;h.game.advance(7560000L);h.show();
            button(h.panel,"egg-progress").doClick(0);
            assertEquals(Integer.valueOf(1),h.game.collection.get(EggPet.MOLE.key));
            assertNull(h.game.activePet);assertTrue(h.game.awaitingSpin);
            assertTrue(h.panel.isRevealing());assertFalse(h.panel.isReadyToHatch());
            h.game.advance(126000L);h.show();
            assertEquals(4086000L,h.game.hatchProgress);assertEquals(1,h.dings);
            assertEquals("Hatch Progress: 100%",button(h.panel,"egg-progress").getText());
        });
        Thread.sleep(650);
        SwingUtilities.invokeAndWait(() -> {
            try{render(holder[0].panel,"overview-mole-reveal",850);}
            catch(Exception ex){throw new RuntimeException(ex);}
        });
        Thread.sleep(2000);
        SwingUtilities.invokeAndWait(() -> {
            try
            {
                Harness h=holder[0];assertTrue(h.panel.isRevealing());
                JButton reveal=button(h.panel,"egg-choice");reveal.setSize(150,190);
                BufferedImage actual=new BufferedImage(150,190,BufferedImage.TYPE_INT_ARGB);
                Graphics2D g=actual.createGraphics();reveal.paint(g);g.dispose();
                BufferedImage expected=new BufferedImage(150,150,BufferedImage.TYPE_INT_ARGB);
                g=expected.createGraphics();g.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION,java.awt.RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                g.drawImage(ImageIO.read(EggPlugin.class.getResource(EggPet.MOLE.petResource)),0,0,150,150,null);g.dispose();
                for(int y=0;y<150;y++)for(int x=0;x<150;x++)assertEquals(expected.getRGB(x,y),actual.getRGB(x,y));
                render(h.panel,"pet-reveal-before-wheel",850);
            }
            catch(Exception ex){throw new RuntimeException(ex);}
        });
        Thread.sleep(600);
        SwingUtilities.invokeAndWait(() -> {
            Harness h=holder[0];assertFalse(h.panel.isRevealing());
            assertFalse(h.panel.isReadyToHatch());assertEquals(1,h.dings);
            assertNull(h.game.activePet);assertNull(button(h.panel,"egg-progress"));
            button(h.panel,"spin-egg").doClick(0);
            assertSame(EggPet.JAD,h.game.activePet);
            EggRoulette reel=find(h.panel,EggRoulette.class);assertTrue(reel.isActive());
            h.game.advance(144000L);h.show();assertTrue(reel.isActive());
            h.clock.set(EggRoulette.SPIN_NANOS);reel.tick();
            assertSame(EggPet.JAD,reel.centeredEgg());
            try{render(h.panel,"roulette-landed",850);}catch(Exception ex){throw new RuntimeException(ex);}
            h.clock.set(EggRoulette.SPIN_NANOS+EggRoulette.HOLD_NANOS);reel.tick();
            assertFalse(reel.isActive());assertTrue(h.panel.isReadyToHatch());assertEquals(2,h.dings);
            assertEquals("Hatch",button(h.panel,"egg-progress").getText());
            assertEquals(4230000L,h.game.hatchProgress);
            h.panel.showGame(null);assertFalse(h.panel.isReadyToHatch());
            assertFalse(button(h.panel,"egg-choice").isEnabled());
        });
    }

    @Test public void collectionShowsGreyEggsAndAllOneHundredFourBossSpritesWithoutClipping() throws Exception
    {
        SwingUtilities.invokeAndWait(() -> {
            try
            {
                Harness h=new Harness();JTabbedPane tabs=find(h.panel,JTabbedPane.class);
                tabs.setSelectedIndex(1);render(h.panel,"collection-locked",8300);
                BufferedImage egg=(BufferedImage)((ImageIcon)tooltip(h.panel,"Jad Egg (not yet hatched)").getIcon()).getImage();
                for(int y=0;y<72;y++)for(int x=0;x<72;x++)
                {
                    int p=egg.getRGB(x,y);assertEquals((p>>16)&255,(p>>8)&255);assertEquals((p>>8)&255,p&255);
                }
                for(EggPet pet:EggGame.PETS)h.game.collection.put(pet.key,1);
                h.show();render(h.panel,"collection-hatched",8300);
                for(EggPet pet:EggGame.PETS)assertNotNull(tooltip(h.panel,pet.displayName+" (hatched: 1)"));
                Container grid=tooltip(h.panel,"Ahrim (hatched: 1)").getParent().getParent();
                for(int i=0;i<EggPet.COLLECTION_ORDER.size();i++)
                {
                    EggPet pet=EggPet.COLLECTION_ORDER.get(i);
                    assertSame(tooltip(h.panel,pet.displayName+" (hatched: 1)").getParent(),grid.getComponent(i));
                }
                JLabel last=tooltip(h.panel,"Zulrah (Tanzanite) (hatched: 1)");
                assertTrue(SwingUtilities.convertPoint(last,0,last.getHeight(),h.panel.getWrappedPanel()).y<8300);
                tabs.setSelectedIndex(0);
                h.game.activePet=EggPet.DERANGED_ARCHAEOLOGIST;h.show();
                render(h.panel,"overview-long-egg-name",1050);
                h.panel.showGame(null);
                render(h.panel,"overview-logged-out",850);
            }
            catch(Exception ex){throw new RuntimeException(ex);}
        });
    }

    @Test public void resetButtonClearsProgressAndCancelsAnActiveReveal() throws Exception
    {
        SwingUtilities.invokeAndWait(() -> {
            try
            {
                Harness h=new Harness();h.choose();h.game.advance(4086000L);h.show();
                button(h.panel,"egg-progress").doClick(0);
                assertTrue(h.panel.isRevealing());assertEquals(1,h.game.uniquePets());
                button(h.panel,"reset-progress").doClick(0);
                assertNull(h.game.activePet);assertEquals(0L,h.game.hatchProgress);
                assertTrue(h.game.collection.isEmpty());assertFalse(h.panel.isRevealing());
                assertFalse(h.panel.isReadyToHatch());
                assertTrue(button(h.panel,"egg-choice").isEnabled());
                assertEquals("Hatch Progress: 0%",button(h.panel,"egg-progress").getText());
                render(h.panel,"after-reset",850);
                button(h.panel,"egg-choice").doClick(0);
                assertSame(EggPet.JERRY,h.game.activePet);
                assertEquals("Jerry Egg",h.game.activePet.eggName());
                h.panel.showGame(null);
                assertFalse(button(h.panel,"reset-progress").isEnabled());
            }
            catch(Exception ex){throw new RuntimeException(ex);}
        });
    }

    @Test public void resetCancelsWheelAndItsAnimationCannotAssignAnEggLater() throws Exception
    {
        SwingUtilities.invokeAndWait(() -> {
            Harness h=new Harness();h.choose();h.game.advance(3600000L);h.show();
            button(h.panel,"egg-progress").doClick(0);h.panel.finishReveal();
            button(h.panel,"spin-egg").doClick(0);
            EggRoulette reel=find(h.panel,EggRoulette.class);assertTrue(reel.isActive());
            button(h.panel,"reset-progress").doClick(0);
            h.clock.set(EggRoulette.SPIN_NANOS+EggRoulette.HOLD_NANOS);reel.tick();
            assertFalse(reel.isActive());assertNull(h.game.activePet);assertFalse(h.game.awaitingSpin);
            assertTrue(h.game.collection.isEmpty());assertTrue(button(h.panel,"egg-choice").isEnabled());
        });
    }

    @Test public void jerryPackIsOptInOncePerReadyAndStaysAtBottomInsideOverview() throws Exception
    {
        SwingUtilities.invokeAndWait(() -> {
            try
            {
                int[] dings={0},voices={0},stops={0},saves={0};
                EggReadyAudio jerry=new EggReadyAudio(){public void play(){voices[0]++;}public void stop(){stops[0]++;}};
                EggPanel panel=new EggPanel(() -> dings[0]++,System::nanoTime,EggSpinAudio.SILENT,jerry);
                panel.setJerrySoundPackAction(enabled -> saves[0]++);
                JCheckBox option=find(panel,JCheckBox.class);assertFalse(option.isSelected());
                EggGame game=new EggGame();game.migrateCollection();game.assignEgg();
                panel.showGame(game);game.skipToHatch();panel.showGame(game);
                assertEquals(1,dings[0]);assertEquals(0,voices[0]);
                game.hatch();panel.showGame(game);option.doClick(0);
                assertEquals(1,saves[0]);assertEquals(0,voices[0]);
                game.spin(new Random(1));game.skipToHatch();panel.showGame(game);
                assertEquals(1,voices[0]);assertEquals(1,dings[0]);
                game.advance(273600L);panel.showGame(game);panel.showGame(game);assertEquals(1,voices[0]);
                render(panel,"overview-jerry-sound-pack",1050);
                JTabbedPane tabs=find(panel,JTabbedPane.class);Component overview=tabs.getComponentAt(0);
                assertTrue(SwingUtilities.isDescendingFrom(option,overview));
                java.awt.Point location=SwingUtilities.convertPoint(option,0,option.getHeight(),overview);
                assertTrue(location.y>overview.getHeight()-60 && location.y<overview.getHeight());
                int before=stops[0];option.doClick(0);assertTrue(stops[0]>before);assertEquals(2,saves[0]);
                panel.setJerrySoundPackEnabled(true);
                game=new EggGame();game.migrateCollection();panel.showGame(game);
                assertTrue(option.isSelected());assertEquals(2,saves[0]);
                panel.showGame(null);
            }
            catch(Exception ex){throw new RuntimeException(ex);}
        });
    }

    private static void writeRouletteDemo(Harness h)throws Exception
    {
        writeDemo(h);
    }

    @Test public void skipButtonSitsAboveResetAndEnablesTheRealTutorialHatch() throws Exception
    {
        SwingUtilities.invokeAndWait(() -> {
            try
            {
                Harness h=new Harness();
                assertFalse(button(h.panel,"skip-to-hatch").isEnabled());
                button(h.panel,"egg-choice").doClick(0);
                assertSame(EggPet.JERRY,h.game.activePet);
                JButton skip=button(h.panel,"skip-to-hatch");assertTrue(skip.isEnabled());
                render(h.panel,"tutorial-jerry-assigned",950);
                JButton reset=button(h.panel,"reset-progress");
                assertTrue(SwingUtilities.convertPoint(skip,0,0,h.panel).y
                    < SwingUtilities.convertPoint(reset,0,0,h.panel).y);
                skip.doClick(0);
                assertEquals("Hatch",button(h.panel,"egg-progress").getText());
                assertTrue(h.game.collection.isEmpty());assertFalse(skip.isEnabled());
                button(h.panel,"egg-progress").doClick(0);
                assertTrue(h.panel.isRevealing());
                assertEquals(Integer.valueOf(1),h.game.collection.get(EggPet.JERRY.key));
                assertFalse(button(h.panel,"skip-to-hatch").isEnabled());
                h.panel.finishReveal();assertFalse(button(h.panel,"skip-to-hatch").isEnabled());
                button(h.panel,"spin-egg").doClick(0);
                assertNotSame(EggPet.JERRY,h.game.activePet);
                EggRoulette reel=find(h.panel,EggRoulette.class);
                h.clock.set(EggRoulette.SPIN_NANOS+EggRoulette.HOLD_NANOS);reel.tick();
                assertTrue(button(h.panel,"skip-to-hatch").isEnabled());
                button(h.panel,"skip-to-hatch").doClick(0);
                assertEquals("Hatch",button(h.panel,"egg-progress").getText());
                h.panel.showGame(null);
            }
            catch(Exception ex){throw new RuntimeException(ex);}
        });
    }

    private static void writeDemo(Harness h)throws Exception
    {
        File dir=new File("build/previews");dir.mkdirs();
        try(GifPreview gif=new GifPreview(new File(dir,"egg-roulette-demo.gif")))
        {
            gif.frame(capture(h.panel,850),80);
            button(h.panel,"spin-egg").doClick(0);
            EggRoulette reel=find(h.panel,EggRoulette.class);
            for(int frame=1;frame<=107;frame++)
            {
                h.clock.set(frame*50_000_000L);reel.tick();gif.frame(capture(h.panel,850),5);
            }
            gif.frame(capture(h.panel,850),100);
        }
    }

    private static BufferedImage capture(EggPanel panel,int height)
    {
        Container root=panel.getWrappedPanel();root.setSize(242,height);
        for(int i=0;i<3;i++)layout(root);
        BufferedImage image=new BufferedImage(242,height,BufferedImage.TYPE_INT_ARGB);
        Graphics2D g=image.createGraphics();root.printAll(g);g.dispose();
        return image;
    }

    private static void render(EggPanel panel,String name,int height)throws Exception
    {
        BufferedImage image=capture(panel,height);
        File dir=new File("build/previews");dir.mkdirs();ImageIO.write(image,"png",new File(dir,name+".png"));
    }
    private static void layout(Container root)
    {root.doLayout();for(Component c:root.getComponents())if(c instanceof Container)layout((Container)c);}
    private static <T>T find(Container root,Class<T> type)
    {
        for(Component c:root.getComponents())
        {
            if(type.isInstance(c))return type.cast(c);
            if(c instanceof Container){T result=find((Container)c,type);if(result!=null)return result;}
        }
        return null;
    }
    private static JButton button(Container root,String name)
    {
        for(Component c:root.getComponents())
        {
            if(c instanceof JButton&&name.equals(c.getName()))return(JButton)c;
            if(c instanceof Container){JButton result=button((Container)c,name);if(result!=null)return result;}
        }
        return null;
    }
    private static JLabel tooltip(Container root,String text)
    {
        for(Component c:root.getComponents())
        {
            if(c instanceof JLabel&&text.equals(((JLabel)c).getToolTipText()))return(JLabel)c;
            if(c instanceof Container){JLabel result=tooltip((Container)c,text);if(result!=null)return result;}
        }
        return null;
    }
}
