package com.eggcollection;

import java.awt.*;
import java.awt.event.HierarchyEvent;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.LongSupplier;
import java.util.function.Consumer;
import javax.swing.*;

/** Visual reel only: the client thread chooses and saves the result before begin(). */
class EggRoulette extends JPanel
{
    static final long SPIN_NANOS = EggSpinSound.DURATION_NANOS;
    static final long HOLD_NANOS = 800_000_000L;
    private static final int PITCH=124, START_INDEX=54, WIN_INDEX=11;
    private static final Color GOLD=new Color(235,190,75);
    private static final Color BACKGROUND=new Color(34,34,34);
    private final Map<EggPet,ImageIcon> artwork;
    private final List<EggPet> designs=new ArrayList<>();
    private final List<EggPet> strip=new ArrayList<>();
    private final Random visualRandom=new Random();
    private final JButton spin=new JButton("Spin");
    private final JToggleButton sound=new JToggleButton();
    private final EggSpinAudio audio;
    private Consumer<Boolean> onMute=muted -> {};
    private boolean muted;
    private final Runnable onSpin, onFinished;
    private final LongSupplier clock;
    private final Timer timer=new Timer(16,event -> tick());
    private boolean active, pending, prepared;
    private boolean shinyWinner;
    void setShinyWinner(boolean shiny){shinyWinner=shiny;}
    private long started;
    private double position;

    EggRoulette(Map<EggPet,ImageIcon> artwork,Runnable onSpin,Runnable onFinished)
    { this(artwork,onSpin,onFinished,System::nanoTime); }

    EggRoulette(Map<EggPet,ImageIcon> artwork,Runnable onSpin,Runnable onFinished,LongSupplier clock)
    {this(artwork,onSpin,onFinished,clock,EggSpinAudio.SILENT);}

    EggRoulette(Map<EggPet,ImageIcon> artwork,Runnable onSpin,Runnable onFinished,LongSupplier clock,EggSpinAudio audio)
    {
        this.artwork=artwork; this.onSpin=onSpin; this.onFinished=onFinished; this.clock=clock;
        this.audio=audio;
        Map<String,EggPet> unique=new LinkedHashMap<>();
        for(EggPet pet:EggGame.PETS) if(!pet.tutorialOnly)unique.putIfAbsent(pet.eggResource,pet);
        designs.addAll(unique.values());
        setLayout(null); setBackground(BACKGROUND); setName("egg-roulette");
        setPreferredSize(new Dimension(0,480)); setMinimumSize(new Dimension(0,320));
        spin.setName("spin-egg"); spin.setFont(new Font(Font.SANS_SERIF,Font.BOLD,14));
        spin.setForeground(GOLD); spin.setBackground(BACKGROUND);
        spin.setBorder(BorderFactory.createLineBorder(GOLD)); spin.setFocusPainted(false);
        spin.addActionListener(event -> {
            if(active || pending) return;
            pending=true; spin.setEnabled(false); onSpin.run();
        });
        add(spin);
        sound.setName("spin-sound-toggle");sound.setOpaque(false);sound.setContentAreaFilled(false);
        sound.setBorderPainted(false);sound.setFocusPainted(false);sound.setBorder(BorderFactory.createEmptyBorder());
        sound.addActionListener(event -> {setMuted(sound.isSelected());onMute.accept(muted);});
        add(sound);setMuted(false);
        addHierarchyListener(event -> {
            if((event.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED)!=0)
            {
                if(active && isShowing())
                {
                    tick();if(active)timer.start();
                    long elapsed=Math.max(0,clock.getAsLong()-started);
                    if(active && elapsed<SPIN_NANOS && !muted)audio.play(elapsed);
                }
                else {timer.stop();audio.stop();}
            }
        });
        prepare();
    }

    void prepare()
    {
        if(prepared || active || pending) return;
        strip.clear();
        for(int i=0;i<80;i++) strip.add(designs.get(visualRandom.nextInt(designs.size())));
        position=0; prepared=true; spin.setEnabled(true); repaint();
    }

    void begin(EggPet winner)
    {
        if(active) return;
        prepare(); strip.set(WIN_INDEX,winner);
        pending=false; active=true; soundStopped=false; spin.setEnabled(false);
        started=clock.getAsLong(); position=0;
        if(isShowing())timer.start();
        if(!muted)audio.play(0);
        repaint();
    }

    void tick()
    {
        if(!active)return;
        long elapsed=Math.max(0,clock.getAsLong()-started);
        double t=Math.min(1,(double)elapsed/SPIN_NANOS);
        position=(START_INDEX-WIN_INDEX)*PITCH*(1-Math.pow(1-t,4));
        if(elapsed>=SPIN_NANOS && !soundStopped){audio.stop();soundStopped=true;}
        repaint();
        if(elapsed>=SPIN_NANOS+HOLD_NANOS)
        {
            active=false; timer.stop(); onFinished.run();
        }
    }

    void cancel()
    {
        audio.stop();soundStopped=true;
        timer.stop(); active=false; pending=false; prepared=false; position=0;
    }

    private boolean soundStopped;
    void setMuteAction(Consumer<Boolean> onMute){this.onMute=onMute;}
    boolean isMuted(){return muted;}
    void setMuted(boolean muted)
    {
        this.muted=muted;sound.setSelected(muted);sound.setIcon(new SpinSpeakerIcon(muted));
        sound.getAccessibleContext().setAccessibleName(muted ? "Unmute spin sound" : "Mute spin sound");
        if(muted)audio.stop();
        else if(active)
        {
            long elapsed=Math.max(0,clock.getAsLong()-started);
            if(elapsed<SPIN_NANOS)audio.play(elapsed);
        }
    }

    boolean isActive(){return active;}
    boolean isPending(){return pending;}
    double position(){return position;}
    EggPet centeredEgg()
    {
        int index=START_INDEX-(int)Math.round(position/PITCH);
        return strip.get(index);
    }

    private int laneCenter(){return Math.max(48,(getWidth()-60)/2);}

    @Override public void doLayout()
    {
        int margin=Math.max(0,laneCenter()-61);
        spin.setBounds(Math.max(0,getWidth()-margin-56),getHeight()/2-17,56,34);
        sound.setBounds(spin.getX()+13,spin.getY()+spin.getHeight()+10,30,30);
    }

    @Override protected void paintComponent(Graphics graphics)
    {
        super.paintComponent(graphics);
        Graphics2D g=(Graphics2D)graphics.create();
        int centerX=laneCenter(), centerY=getHeight()/2;
        g.setColor(new Color(235,190,75,14)); g.fillRect(centerX-51,centerY-53,102,106);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        for(int i=0;i<strip.size();i++)
        {
            int y=(int)Math.round(centerY+(i-START_INDEX)*PITCH+position);
            if(y+48<0 || y-48>getHeight())continue;
            ImageIcon icon=artwork.get(strip.get(i));
            g.drawImage(icon.getImage(),centerX-46,y-46,92,92,null);
            if(shinyWinner && i==WIN_INDEX)EggShinyStyle.paint(g,centerX-46,y-46,92,92);
        }
        g.setColor(GOLD); g.drawRect(centerX-51,centerY-53,102,106);
        int[] xs={centerX-61,centerX-61,centerX-54};
        int[] ys={centerY-6,centerY+6,centerY}; g.fillPolygon(xs,ys,3);
        // Fade the entering/leaving eggs, leaving the middle marker stationary.
        g.setPaint(new GradientPaint(0,0,BACKGROUND,0,35,new Color(34,34,34,0)));
        g.fillRect(0,0,getWidth(),35);
        g.setPaint(new GradientPaint(0,getHeight()-35,new Color(34,34,34,0),0,getHeight(),BACKGROUND));
        g.fillRect(0,getHeight()-35,getWidth(),35); g.dispose();
    }
}
