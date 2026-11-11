package com.eggcollection;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.event.HierarchyEvent;
import java.awt.image.BufferedImage;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.LongSupplier;
import java.util.function.Consumer;
import javax.inject.Inject;
import javax.swing.*;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.FontManager;
import net.runelite.client.util.ImageUtil;

class EggPanel extends PluginPanel
{
    private static final Color BACKGROUND = new Color(34, 34, 34);
    private static final Color INSET = new Color(25, 25, 25);
    private static final Color GOLD = new Color(235, 190, 75);
    private static final Color MUTED = new Color(170, 170, 170);
    private static final Font HEADING_FONT = FontManager.getRunescapeBoldFont().deriveFont(16f);
    private static final BufferedImage SOURCE = ImageUtil.loadImageResource(EggPlugin.class, "egg.png");
    private static final ImageIcon MYSTERY_EGG = scaled(SOURCE, 150, true);
    private final Map<EggPet, ImageIcon> largeEggs = new EnumMap<>(EggPet.class);
    private final Map<EggPet, ImageIcon> eggs = new EnumMap<>(EggPet.class);
    private final Map<EggPet, ImageIcon> pets = new EnumMap<>(EggPet.class);
    private final JPanel overview = new JPanel(new BorderLayout());
    private final JPanel overviewBody = column();
    private final boolean developerTools;
    private final JCheckBox jerryPack = new JCheckBox("Jerry Sound Pack");
    private final JPanel collection = column();
    private final JPanel collectionPage = new JPanel(new BorderLayout());
    private final EggCollectionScroll collectionScroll = new EggCollectionScroll(collection);
    private final JCheckBox hideUnobtained = new JCheckBox("Hide unobtained");
    private final JCheckBox hideCollected = new JCheckBox("Hide collected");
    private boolean collectionSelected;
    private Map<String,Integer> shownCollection;
    private boolean shownHideUnobtained,shownHideCollected;
    private final JLabel hint = label("Log in and play to begin.", MUTED);
    private final JButton resetButton = new JButton("Reset All Progress");
    private final JButton skipButton = new JButton("Skip to Hatch");
    private final JButton debugPetButton = new JButton("Queue Shiny Egg");
    private Runnable debugPetDropAction=() -> {};
    private final EggRoulette roulette;
    private EggGame displayedGame;
    private Runnable hatchAction;
    private Runnable chooseAction;
    private Runnable resetAction;
    private Runnable spinAction;
    private Runnable skipAction;
    private ImageIcon revealEgg;
    private ImageIcon revealedPet;
    private String revealedName;
    private long revealStarted;
    private final Runnable readySound;
    private final EggReadyAudio jerryAudio;
    private Consumer<Boolean> onJerryPack=enabled -> {};
    private boolean readyToHatch;
    private boolean hatchPending;
    private boolean choosingEgg;
    private long readyStarted;
    private final Timer shakeTimer = new Timer(30, e -> overviewBody.repaint());
    private final Timer revealTimer = new Timer(30, e -> {
        if (System.nanoTime() - revealStarted >= 3_000_000_000L) finishReveal();
        else repaint();
    });

    @Inject EggPanel()
    {
        this(EggReadySound::play,System::nanoTime,new EggSpinSound(),new EggJerrySoundPack(),EggDevelopment.enabled());
    }

    EggPanel(Runnable readySound)
    {
        this(readySound,System::nanoTime);
    }

    EggPanel(Runnable readySound, LongSupplier rouletteClock)
    {
        this(readySound,rouletteClock,EggSpinAudio.SILENT);
    }

    EggPanel(Runnable readySound, LongSupplier rouletteClock, EggSpinAudio spinAudio)
    {
        this(readySound,rouletteClock,spinAudio,EggReadyAudio.SILENT);
    }

    EggPanel(Runnable readySound, LongSupplier rouletteClock, EggSpinAudio spinAudio, EggReadyAudio jerryAudio)
    {
        this(readySound,rouletteClock,spinAudio,jerryAudio,true);
    }

    EggPanel(Runnable readySound, LongSupplier rouletteClock, EggSpinAudio spinAudio, EggReadyAudio jerryAudio, boolean developerTools)
    {
        this.developerTools=developerTools;
        this.readySound = readySound;
        this.jerryAudio = jerryAudio;
        addHierarchyListener(event -> {
            if ((event.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0) updateShakeTimer();
        });
        setLayout(new BorderLayout()); setBackground(BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(16, 8, 12, 8));
        getScrollPane().setBorder(BorderFactory.createEmptyBorder());
        getScrollPane().getViewport().setBackground(BACKGROUND);
        EggCollectionScroll.styleBar(getScrollPane().getVerticalScrollBar());
        // Fill the viewport so the footer reaches the bottom; short windows scroll.
        JPanel scrollContent = (JPanel) getParent();
        scrollContent.remove(this); scrollContent.add(this, BorderLayout.CENTER);
        for (EggPet pet : EggGame.PETS)
        {
            largeEggs.put(pet, scaled(ImageUtil.loadImageResource(EggPlugin.class, pet.eggResource),150,false));
            eggs.put(pet, scaled(ImageUtil.loadImageResource(EggPlugin.class, pet.eggResource), 72, true));
            pets.put(pet, scaled(ImageUtil.loadImageResource(EggPlugin.class, pet.petResource), 72, false));
        }
        roulette=new EggRoulette(largeEggs,() -> spinAction.run(),this::finishSpin,rouletteClock,spinAudio);
        overview.setBackground(BACKGROUND);overview.add(overviewBody,BorderLayout.CENTER);
        JPanel options=column();options.setOpaque(false);
        options.setBorder(BorderFactory.createEmptyBorder(10,0,4,0));
        jerryPack.setName("jerry-sound-pack");jerryPack.setOpaque(false);jerryPack.setForeground(GOLD);
        jerryPack.setFont(FontManager.getRunescapeFont());jerryPack.setFocusPainted(false);
        Icon check=new Icon()
        {
            public int getIconWidth(){return 14;}
            public int getIconHeight(){return 14;}
            public void paintIcon(Component c,Graphics graphics,int x,int y)
            {
                Graphics2D g=(Graphics2D)graphics.create();g.translate(x,y);
                g.setColor(INSET);g.fillRect(0,0,13,13);g.setColor(GOLD);g.drawRect(0,0,13,13);
                if(((AbstractButton)c).isSelected())
                {g.setStroke(new BasicStroke(2));g.drawLine(3,7,6,10);g.drawLine(6,10,11,3);}
                g.dispose();
            }
        };
        jerryPack.setIcon(check);jerryPack.setSelectedIcon(check);
        jerryPack.addActionListener(event -> {
            if(!jerryPack.isSelected())jerryAudio.stop();
            onJerryPack.accept(jerryPack.isSelected());
        });
        JPanel soundOption=new JPanel(new FlowLayout(FlowLayout.CENTER,0,0));soundOption.setOpaque(false);
        soundOption.add(jerryPack);fullWidth(soundOption,24);options.add(soundOption);
        overview.add(options,BorderLayout.SOUTH);
        collectionPage.setBackground(BACKGROUND);
        collectionPage.setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
        collectionPage.add(collectionScroll,BorderLayout.CENTER);
        JPanel filters=column();filters.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));
        configureFilter(hideUnobtained,"hide-unobtained");configureFilter(hideCollected,"hide-collected");
        filters.add(hideUnobtained);filters.add(Box.createVerticalStrut(4));filters.add(hideCollected);
        collectionPage.add(filters,BorderLayout.SOUTH);
        JPanel header = column();
        JLabel title = label("EggScape – Pet Collection", GOLD); title.setFont(title.getFont().deriveFont(Font.BOLD, 13f));
        fullWidth(title, 22); header.add(title); header.add(Box.createVerticalStrut(10));
        JLabel subtitle = label("Play. Hatch eggs. Collect pets.", MUTED);
        subtitle.setFont(subtitle.getFont().deriveFont(11f));
        fullWidth(subtitle, 20); header.add(subtitle); header.add(Box.createVerticalStrut(18));
        add(header, BorderLayout.NORTH);
        JTabbedPane tabs = new JTabbedPane()
        {
            @Override public Dimension getPreferredSize()
            {
                Dimension size = super.getPreferredSize();
                Component page = getSelectedComponent();
                if (page != null) size.height = page.getPreferredSize().height + 30;
                return size;
            }
        };
        tabs.setBackground(BACKGROUND); tabs.setForeground(GOLD);
        tabs.setUI(new BasicTabbedPaneUI()
        {
            @Override protected void paintTabBackground(Graphics g, int placement, int index, int x, int y, int width, int height, boolean selected)
            { g.setColor(selected ? GOLD : INSET); g.fillRect(x, y, width, height); }
            @Override protected void installDefaults()
            { super.installDefaults(); focus = GOLD; lightHighlight = GOLD; highlight = GOLD; shadow = GOLD; darkShadow = GOLD; }
            @Override protected void paintContentBorder(Graphics g, int placement, int selectedIndex)
            {
                Component page = tabPane.getSelectedComponent();
                if (page == null) return;
                Rectangle bounds = page.getBounds();
                g.setColor(GOLD);
                g.drawRect(bounds.x-1, bounds.y-1, bounds.width+1, bounds.height+1);
            }
        });
        tabs.addTab("Overview", overview); tabs.addTab("Collection", collectionPage);
        tabs.addChangeListener(e -> {
            collectionSelected=tabs.getSelectedIndex()==1;
            getScrollPane().setVerticalScrollBarPolicy(collectionSelected?JScrollPane.VERTICAL_SCROLLBAR_NEVER:JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            getScrollPane().getViewport().setViewPosition(new Point(0,0));
            for (int i=0; i<tabs.getTabCount(); i++) tabs.setForegroundAt(i, i==tabs.getSelectedIndex() ? INSET : GOLD);
            revalidate();
        });
        tabs.setForegroundAt(0, INSET); tabs.setForegroundAt(1, GOLD); add(tabs, BorderLayout.CENTER);
        hint.setFont(hint.getFont().deriveFont(11f));
        fullWidth(hint,30);
        JPanel footer=column(); footer.setBorder(BorderFactory.createEmptyBorder(14,0,0,0));
        footer.add(hint); footer.add(Box.createVerticalStrut(8));
        skipButton.setName("skip-to-hatch");
        skipButton.setFont(skipButton.getFont().deriveFont(Font.BOLD,12f));
        skipButton.setForeground(GOLD); skipButton.setBackground(INSET);
        skipButton.setBorder(BorderFactory.createLineBorder(GOLD)); skipButton.setFocusPainted(false);
        skipButton.addActionListener(event -> { skipButton.setEnabled(false); skipAction.run(); });
        debugPetButton.setName("debug-pet-drop");debugPetButton.setFont(skipButton.getFont());
        debugPetButton.setBackground(skipButton.getBackground());debugPetButton.setForeground(skipButton.getForeground());
        debugPetButton.setBorder(skipButton.getBorder());debugPetButton.setFocusPainted(false);
        debugPetButton.setToolTipText("Queue one shiny test egg. Available once until Reset All Progress.");
        debugPetButton.addActionListener(event -> {debugPetButton.setEnabled(false);debugPetDropAction.run();});
        if(developerTools)
        {
            footer.add(centeredRow(debugPetButton,32));footer.add(Box.createVerticalStrut(8));
            footer.add(centeredRow(skipButton,32)); footer.add(Box.createVerticalStrut(8));
        }
        resetButton.setName("reset-progress");
        resetButton.setFont(resetButton.getFont().deriveFont(Font.BOLD,12f));
        resetButton.setForeground(GOLD); resetButton.setBackground(INSET);
        resetButton.setBorder(BorderFactory.createLineBorder(GOLD));
        resetButton.setFocusPainted(false);
        resetButton.addActionListener(event -> { resetButton.setEnabled(false); resetAction.run(); });
        if(developerTools)footer.add(centeredRow(resetButton,32));
        add(footer,BorderLayout.SOUTH);
        showGame(null);
    }

    void showGame(EggGame game)
    {
        showGame(game, () -> {});
    }

    void showGame(EggGame game, Runnable onHatch)
    {
        showGame(game, onHatch, null);
    }

    void showGame(EggGame game, Runnable onHatch, EggPet hatchedPet)
    {
        showGame(game, onHatch, () -> {}, hatchedPet);
    }

    void showGame(EggGame game, Runnable onHatch, Runnable onChoose, EggPet hatchedPet)
    {
        showGame(game,onHatch,onChoose,() -> {},hatchedPet);
    }

    void showGame(EggGame game, Runnable onHatch, Runnable onChoose, Runnable onReset, EggPet hatchedPet)
    {
        showGame(game,onHatch,onChoose,onReset,() -> {},hatchedPet,null);
    }

    void showGame(EggGame game, Runnable onHatch, Runnable onChoose, Runnable onReset,
        Runnable onSpin, EggPet hatchedPet, EggPet spunEgg)
    {
        showGame(game,onHatch,onChoose,onReset,onSpin,() -> {},hatchedPet,spunEgg);
    }

    void showGame(EggGame game, Runnable onHatch, Runnable onChoose, Runnable onReset,
        Runnable onSpin, Runnable onSkip, EggPet hatchedPet, EggPet spunEgg)
    {
        displayedGame = game; hatchAction = onHatch; chooseAction = onChoose; resetAction = onReset; spinAction=onSpin; skipAction=onSkip;
        resetButton.setEnabled(developerTools && game != null);
        if (game == null || (game.activePet == null && !game.awaitingSpin))
        {
            jerryAudio.stop();
            revealTimer.stop(); revealedPet = null; hatchPending = false; choosingEgg = false;
            roulette.cancel();
        }
        else if (hatchedPet != null)
        {
            hatchPending = false;
            revealedPet = scaled(ImageUtil.loadImageResource(EggPlugin.class, hatchedPet.petResource),150,false);
            revealEgg = largeEggs.get(hatchedPet);
            if(game.lastHatchedShiny){revealedPet=EggShinyStyle.icon(revealedPet);revealEgg=EggShinyStyle.icon(revealEgg);}
            revealedName = (game.lastHatchedShiny?"Shiny ":"")+hatchedPet.displayName;
            revealStarted = System.nanoTime(); revealTimer.restart();
        }
        roulette.setShinyWinner(game!=null && game.activeShiny);
        if(spunEgg!=null)roulette.begin(spunEgg);
        if (game != null && game.activePet != null) choosingEgg = false;
        boolean showWheel=game!=null && revealedPet==null
            && (game.awaitingSpin || roulette.isActive() || roulette.isPending());
        skipButton.setEnabled(developerTools && game!=null && game.activePet!=null && !game.canHatch()
            && revealedPet==null && !showWheel && !hatchPending);
        debugPetButton.setEnabled(developerTools && game!=null && game.canDebugPetDrop() && revealedPet==null && !showWheel && !hatchPending);
        setReady(game != null && game.canHatch() && revealedPet == null && !hatchPending && !showWheel);
        overview.setBorder(BorderFactory.createEmptyBorder(18, 6, 12, 6));
        collection.setBorder(BorderFactory.createEmptyBorder(18, 6, 12, 6));
        if(showWheel)
        {
            roulette.prepare();
            if(overviewBody.getComponentCount()!=1 || overviewBody.getComponent(0)!=roulette)
            {
                overviewBody.removeAll(); overviewBody.setLayout(new BorderLayout());
                overviewBody.add(roulette,BorderLayout.CENTER);
            }
        }
        else
        {
        roulette.cancel();
        overviewBody.removeAll(); overviewBody.setLayout(new BoxLayout(overviewBody,BoxLayout.Y_AXIS));
        overviewBody.add(Box.createVerticalStrut(30));
        EggReveal egg = new EggReveal(); egg.setAlignmentX(CENTER_ALIGNMENT); overviewBody.add(egg);
        overviewBody.add(Box.createVerticalStrut(20));
        CapsuleBar bar = new CapsuleBar(game == null ? 0 : game.hatchProgress,game==null?EggGame.HATCH_MILLIS:game.hatchRequirement(), () -> {
            hatchPending = true; setReady(false); onHatch.run();
        }, readyToHatch);
        overviewBody.add(centeredRow(bar, 26));
        overviewBody.add(Box.createVerticalStrut(8));
        JLabel rate=label(game==null || (game.activePet==null && !game.awaitingSpin)?"":
            game.isTutorialEgg()?(game.activeGameplay?"Active - 15 minutes per egg":"Idle - 30 minutes per egg"):
            game.activeGameplay?"Active - 1 hour per egg":"Idle - 2 hours per egg",MUTED);
        rate.setFont(FontManager.getRunescapeFont());
        overviewBody.add(centeredRow(rate,20));
        overviewBody.add(Box.createVerticalStrut(28));
        int unique = game == null ? 0 : game.uniquePets();
        int total = game == null ? 0 : game.collection.values().stream().mapToInt(Integer::intValue).sum()
            +game.shinyCollection.values().stream().mapToInt(Integer::intValue).sum();
        JLabel collectionTitle = label("YOUR COLLECTION", GOLD);
        collectionTitle.setFont(HEADING_FONT);
        overviewBody.add(centeredRow(collectionTitle, 28)); overviewBody.add(Box.createVerticalStrut(16));
        JPanel stats = new JPanel(new GridLayout(3, 1, 0, 12)); stats.setOpaque(false);
        stats.add(stat("Total Hatched", Integer.toString(total)));
        stats.add(stat("Unique Pets", unique+" / "+EggGame.PETS.length));
        stats.add(stat("Collection", (unique*100/EggGame.PETS.length)+"%"));
        overviewBody.add(centeredRow(stats, 246));
        overviewBody.add(Box.createVerticalGlue());
        }
        hint.setVisible(game == null);
        rebuildCollection(game);
        revalidate(); repaint();
    }

    private void rebuildCollection(EggGame game)
    {
        Map<String,Integer> counts=game==null?java.util.Collections.emptyMap():new java.util.HashMap<>(game.collection);
        if(game!=null)game.shinyCollection.forEach((key,value)->counts.put("shiny:"+key,value));
        if(counts.equals(shownCollection) && shownHideUnobtained==hideUnobtained.isSelected()
            && shownHideCollected==hideCollected.isSelected())return;
        shownCollection=counts;shownHideUnobtained=hideUnobtained.isSelected();shownHideCollected=hideCollected.isSelected();
        collectionScroll.cancelDrag();
        collection.removeAll();
        int unique = game == null ? 0 : game.uniquePets();
        section(collection, "PET COLLECTION");
        section(collection, unique+" / "+EggGame.PETS.length);
        collection.add(Box.createVerticalStrut(16));
        JPanel grid = new JPanel(new GridLayout(0, 2, 12, 16)); grid.setOpaque(false);
        for (EggPet pet : EggPet.COLLECTION_ORDER)
        {
            int count = game == null ? 0 : game.collectedCount(pet);
            boolean shiny=game!=null && game.hasShiny(pet);
            if((count==0 && hideUnobtained.isSelected()) || (count>0 && hideCollected.isSelected()))continue;
            JPanel card = column();
            JLabel item = new JLabel(count > 0 ? pets.get(pet) : eggs.get(pet)); item.setAlignmentX(CENTER_ALIGNMENT);
            if(shiny)item.setIcon(EggShinyStyle.icon((ImageIcon)item.getIcon()));
            String prefix=shiny?"Shiny ":"";
            String tooltip = count > 0 ? prefix+pet.displayName+" (hatched: "+count+")" : prefix+pet.displayName+" Egg (not yet hatched)";
            item.setToolTipText(tooltip); card.setToolTipText(tooltip); card.add(item); card.add(Box.createVerticalStrut(6));
            String displayName=pet.displayName;
            if(displayName.contains(" "))
            {
                // Balance ordinary names; longer phase names can use three lines.
                int split=displayName.indexOf(' '),best=Integer.MAX_VALUE;
                FontMetrics metrics=getFontMetrics(new Font(Font.SANS_SERIF,Font.BOLD,13));
                for(int i=0;i<displayName.length();i++)if(displayName.charAt(i)==' ')
                {
                    int width=Math.max(metrics.stringWidth(displayName.substring(0,i)),metrics.stringWidth(displayName.substring(i+1)));
                    if(width<best){best=width;split=i;}
                }
                if(best<=94)displayName=displayName.substring(0,split)+"<br>"+displayName.substring(split+1);
                else
                {
                    StringBuilder wrapped=new StringBuilder(),line=new StringBuilder();
                    for(String word:displayName.split(" "))
                    {
                        if(line.length()>0 && metrics.stringWidth(line+" "+word)>94)
                        {wrapped.append(line).append("<br>");line.setLength(0);}
                        if(line.length()>0)line.append(' ');
                        line.append(word);
                    }
                    displayName=wrapped.append(line).toString();
                }
                displayName="<html><center>"+displayName+"</center></html>";
            }
            JLabel name = label(displayName, count > 0 ? GOLD : MUTED); name.setFont(new Font(Font.SANS_SERIF,Font.BOLD,13));
            fullWidth(name, 54); card.add(name); grid.add(card);
        }
        int rows = (grid.getComponentCount() + 1) / 2;
        fullWidth(grid, rows==0?0:rows*132+(rows-1)*16);
        collection.add(grid); collection.add(Box.createVerticalGlue());
        if(rows==0){JLabel empty=label("No pets to show",MUTED);fullWidth(empty,28);collection.add(empty);}
        collectionScroll.installDrag(collection);
        collection.revalidate();collection.repaint();
    }

    @Override public Dimension getPreferredSize()
    {
        Dimension size=super.getPreferredSize();
        if(collectionSelected && getScrollPane()!=null)size.height=getScrollPane().getViewport().getExtentSize().height;
        return size;
    }

    private void configureFilter(JCheckBox filter,String name)
    {
        filter.setName(name);filter.setOpaque(false);filter.setForeground(GOLD);
        filter.setFont(FontManager.getRunescapeFont());filter.setFocusPainted(false);
        Icon icon=new Icon()
        {
            public int getIconWidth(){return 34;}
            public int getIconHeight(){return 16;}
            public void paintIcon(Component c,Graphics graphics,int x,int y)
            {
                Graphics g=graphics.create();g.translate(x,y);g.setColor(MUTED);g.drawRect(0,2,12,12);
                boolean hidden=((AbstractButton)c).isSelected();
                if(hidden){g.setColor(GOLD);g.drawLine(2,8,5,11);g.drawLine(5,11,10,4);}
                g.setColor(hidden?MUTED:GOLD);
                if(hidden){g.drawArc(18,1,14,10,180,180);g.drawLine(19,4,31,14);}
                else{g.drawPolygon(new int[]{18,21,25,29,32,29,25,21},new int[]{8,4,3,4,8,12,13,12},8);g.fillOval(23,6,5,5);}
                g.dispose();
            }
        };
        filter.setIcon(icon);filter.setSelectedIcon(icon);
        filter.addActionListener(event -> {
            rebuildCollection(displayedGame);collectionScroll.getViewport().setViewPosition(new Point(0,0));
        });
    }

    static BufferedImage sidebarIcon()
    { return (BufferedImage) scaled(SOURCE, 16, false).getImage(); }

    void finishReveal()
    {
        revealTimer.stop(); revealedPet = null;
        showGame(displayedGame, hatchAction, chooseAction, resetAction, spinAction, skipAction, null, null);
    }

    private void finishSpin()
    {
        roulette.cancel();
        showGame(displayedGame,hatchAction,chooseAction,resetAction,spinAction,skipAction,null,null);
    }

    boolean isRevealing() { return revealedPet != null; }
    void setDebugPetDropAction(Runnable action){debugPetDropAction=action;}

    void setSpinMuted(boolean muted){roulette.setMuted(muted);}
    void setSpinMuteAction(Consumer<Boolean> onMute){roulette.setMuteAction(onMute);}
    void setJerrySoundPackEnabled(boolean enabled)
    {jerryPack.setSelected(enabled);if(!enabled)jerryAudio.stop();}
    void setJerrySoundPackAction(Consumer<Boolean> onChange){onJerryPack=onChange;}

    boolean isReadyToHatch() { return readyToHatch; }

    private void setReady(boolean ready)
    {
        if (ready && !readyToHatch)
        {
            readyStarted = System.nanoTime();
            if(jerryPack.isSelected())jerryAudio.play(displayedGame!=null && displayedGame.readyWasIdle());else readySound.run();
        }
        readyToHatch = ready;
        updateShakeTimer();
    }

    private void updateShakeTimer()
    {
        if (readyToHatch && isShowing()) shakeTimer.start();
        else shakeTimer.stop();
    }

    private class EggReveal extends JButton
    {
        EggReveal()
        {
            Dimension size = new Dimension(150,208);
            setMinimumSize(new Dimension(0,208)); setPreferredSize(size); setMaximumSize(new Dimension(Integer.MAX_VALUE,208));
            setName("egg-choice");
            boolean mystery = displayedGame != null && displayedGame.activePet == null && revealedPet == null;
            setEnabled(mystery && !choosingEgg);
            setOpaque(false); setContentAreaFilled(false); setBorderPainted(false);
            getAccessibleContext().setAccessibleName(mystery ? "Choose your first egg" : "Current egg");
            addActionListener(event -> { setEnabled(false); choosingEgg = true; chooseAction.run(); });
        }
        @Override protected void paintComponent(Graphics graphics)
        {
            Graphics2D g = (Graphics2D) graphics.create();
            float petAlpha = 0;
            if (revealedPet != null)
            {
                double seconds = (System.nanoTime()-revealStarted)/1_000_000_000.0;
                petAlpha = (float) Math.max(0, Math.min(1,seconds/0.4));
            }
            g.setComposite(AlphaComposite.SrcOver.derive(1-petAlpha));
            Graphics2D eggGraphics = (Graphics2D) g.create();
            int eggX=(getWidth()-150)/2;eggGraphics.translate(eggX,0);
            if (readyToHatch)
            {
                double seconds = (System.nanoTime()-readyStarted)/1_000_000_000.0;
                double sway = Math.sin(seconds*2*Math.PI*3);
                eggGraphics.translate(sway*3,0);
                eggGraphics.rotate(Math.toRadians(sway*2),75,100);
            }
            boolean mystery = displayedGame == null || displayedGame.activePet == null;
            ImageIcon currentEgg = mystery ? MYSTERY_EGG : largeEggs.get(displayedGame.activePet);
            if(!mystery && displayedGame.activeShiny)currentEgg=EggShinyStyle.icon(currentEgg);
            if (revealedPet != null) currentEgg = revealEgg;
            currentEgg.paintIcon(this,eggGraphics,0,0);
            if (mystery && revealedPet == null)
            {
                eggGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                GlyphVector question = new Font(Font.SANS_SERIF,Font.BOLD,76).createGlyphVector(eggGraphics.getFontRenderContext(),"?");
                Shape glyph = question.getOutline(); Rectangle2D bounds = glyph.getBounds2D();
                Shape centered = AffineTransform.getTranslateInstance(75-bounds.getCenterX(),75-bounds.getCenterY()).createTransformedShape(glyph);
                eggGraphics.setStroke(new BasicStroke(6,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
                eggGraphics.setColor(Color.BLACK); eggGraphics.draw(centered);
                eggGraphics.setColor(Color.WHITE); eggGraphics.fill(centered);
            }
            eggGraphics.dispose();
            if (revealedPet != null)
            {
                g.setComposite(AlphaComposite.SrcOver.derive(petAlpha));
                revealedPet.paintIcon(this,g,eggX,0);
                g.setColor(GOLD); g.setFont(new Font(Font.SANS_SERIF,Font.BOLD,12));
                String text = "You hatched";
                g.drawString(text,(getWidth()-g.getFontMetrics().stringWidth(text))/2,166);
                g.drawString(revealedName+"!",(getWidth()-g.getFontMetrics().stringWidth(revealedName+"!"))/2,184);
            }
            else
            {
                g.setComposite(AlphaComposite.SrcOver); g.setColor(GOLD);
                g.setFont(HEADING_FONT);
                String text = mystery ? "" : (displayedGame.activeShiny?"Shiny ":"")+displayedGame.activePet.eggName();
                FontMetrics fm=g.getFontMetrics();
                if(fm.stringWidth(text)<=getWidth()-4)
                    g.drawString(text,(getWidth()-fm.stringWidth(text))/2,177);
                else
                {
                    int split=text.lastIndexOf(' '),best=Integer.MAX_VALUE;
                    for(int i=0;i<text.length();i++) if(text.charAt(i)==' ')
                    {
                        int left=fm.stringWidth(text.substring(0,i)),right=fm.stringWidth(text.substring(i+1));
                        if(Math.max(left,right)<best){best=Math.max(left,right);split=i;}
                    }
                    String first=text.substring(0,split),second=text.substring(split+1);
                    g.drawString(first,(getWidth()-fm.stringWidth(first))/2,174);
                    g.drawString(second,(getWidth()-fm.stringWidth(second))/2,195);
                }
            }
            g.dispose();
        }
    }

    private static ImageIcon scaled(BufferedImage source, int size, boolean grey)
    {
        BufferedImage copy = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int y=0; y<source.getHeight(); y++) for (int x=0; x<source.getWidth(); x++)
        {
            int pixel = source.getRGB(x,y);
            if (grey)
            {
                int r=(pixel>>16)&255, g=(pixel>>8)&255, b=pixel&255;
                int v=(r*30+g*59+b*11)/100; pixel=(pixel&0xff000000)|(v<<16)|(v<<8)|v;
            }
            copy.setRGB(x,y,pixel);
        }
        BufferedImage result = new BufferedImage(size,size,BufferedImage.TYPE_INT_ARGB);
        Graphics2D g=result.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        double scale = Math.min((double) size / copy.getWidth(), (double) size / copy.getHeight());
        int width = Math.max(1, (int) Math.round(copy.getWidth() * scale));
        int height = Math.max(1, (int) Math.round(copy.getHeight() * scale));
        g.drawImage(copy,(size-width)/2,(size-height)/2,width,height,null);
        g.dispose(); return new ImageIcon(result);
    }

    private static JPanel stat(String title, String value)
    {
        JPanel p=column(); p.setBackground(INSET); p.setBorder(BorderFactory.createEmptyBorder(10,4,10,4));
        JLabel a=label(title,MUTED); a.setFont(a.getFont().deriveFont(Font.BOLD,16f));
        JLabel b=label(value,GOLD); b.setFont(b.getFont().deriveFont(Font.BOLD,21f));
        fullWidth(a,22); fullWidth(b,28); p.add(a); p.add(Box.createVerticalStrut(4)); p.add(b); return p;
    }

    private JPanel centeredRow(JComponent content, int height)
    {
        JPanel row = new JPanel(null)
        {
            @Override public void doLayout()
            {
                int width = Math.min(getWidth(), (int) Math.round(EggPanel.this.getWidth() * 0.8));
                content.setBounds((getWidth()-width)/2, 0, width, height);
            }
        };
        row.setOpaque(false); row.add(content); fullWidth(row, height); return row;
    }

    private void section(JPanel target, String text)
    {
        JLabel l=label(text,GOLD); l.setFont(HEADING_FONT); fullWidth(l,26); target.add(l);
    }

    private static void fullWidth(JComponent component, int height)
    {
        component.setAlignmentX(CENTER_ALIGNMENT); component.setMinimumSize(new Dimension(0,height));
        component.setPreferredSize(new Dimension(0,height)); component.setMaximumSize(new Dimension(Integer.MAX_VALUE,height));
    }

    private static JPanel column()
    { JPanel p=new JPanel(); p.setLayout(new BoxLayout(p,BoxLayout.Y_AXIS)); p.setBackground(BACKGROUND); return p; }
    private static JLabel label(String text, Color color)
    { JLabel l=new JLabel(text,JLabel.CENTER); l.setForeground(color); return l; }

    private static class CapsuleBar extends JButton
    {
        private final long progress;
        private final long required;
        CapsuleBar(long progress,long required, Runnable onHatch, boolean allowHatch)
        {
            setName("egg-progress");
            this.progress=progress;
            this.required=required;
            boolean ready = allowHatch && progress >= required;
            setText(ready ? "Hatch" : "Hatch Progress: "+Math.min(100,progress*100/required)+"%");
            setFont(new Font(Font.SANS_SERIF,Font.BOLD,12));
            setEnabled(ready); setOpaque(false); setContentAreaFilled(false); setBorderPainted(false);
            setToolTipText(ready ? "Hatch your egg to reveal its pet" : "Active gameplay: "+required/60000+" minutes per egg. Idle: "+required/30000+" minutes. Quest completions add bonus progress.");
            addActionListener(event -> { setEnabled(false); onHatch.run(); });
        }
        @Override protected void paintComponent(Graphics graphics)
        {
            Graphics2D g=(Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            int w=getWidth(),h=getHeight();
            RoundRectangle2D capsule=new RoundRectangle2D.Double(0,0,w,h,h,h);
            g.setColor(INSET); g.fill(capsule);
            int filled=(int)Math.round(w*Math.max(0,Math.min(1,(double)progress/required)));
            g.clip(capsule); g.setColor(GOLD); g.fillRect(0,0,filled,h); g.setFont(getFont());
            String text=getText(); FontMetrics fm=g.getFontMetrics();
            int x=(w-fm.stringWidth(text))/2,y=(h-fm.getHeight())/2+fm.getAscent();
            g.setColor(Color.WHITE); g.drawString(text,x,y);
            g.clipRect(0,0,filled,h); g.setColor(INSET); g.drawString(text,x,y);
            if (isFocusOwner())
            {
                g.setClip(null); g.setColor(Color.WHITE);
                g.drawRoundRect(2,2,w-5,h-5,h-4,h-4);
            }
            g.dispose();
        }
    }
}
