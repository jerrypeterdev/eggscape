package com.eggcollection;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;

/** A quiet inset scrollbar and grab-to-scroll interaction for the collection. */
class EggCollectionScroll extends JScrollPane
{
    private Point press;
    private int startY;
    private final MouseAdapter drag=new MouseAdapter()
    {
        @Override public void mousePressed(MouseEvent event)
        {
            if(!SwingUtilities.isLeftMouseButton(event))return;
            press=SwingUtilities.convertPoint(event.getComponent(),event.getPoint(),getViewport());
            startY=getViewport().getViewPosition().y;
        }
        @Override public void mouseDragged(MouseEvent event)
        {
            if(press==null)return;
            Point now=SwingUtilities.convertPoint(event.getComponent(),event.getPoint(),getViewport());
            int maximum=Math.max(0,getViewport().getViewSize().height-getViewport().getExtentSize().height);
            getViewport().setViewPosition(new Point(0,Math.max(0,Math.min(maximum,startY+press.y-now.y))));
        }
        @Override public void mouseReleased(MouseEvent event){press=null;}
    };

    EggCollectionScroll(JComponent content)
    {
        super(content,VERTICAL_SCROLLBAR_AS_NEEDED,HORIZONTAL_SCROLLBAR_NEVER);
        setName("collection-scroll");
        setBorder(BorderFactory.createEmptyBorder());
        getViewport().setBackground(new Color(34,34,34));
        getVerticalScrollBar().setUnitIncrement(36);
        styleBar(getVerticalScrollBar());
        getViewport().addMouseListener(drag);getViewport().addMouseMotionListener(drag);
        setMinimumSize(new Dimension(0,0));setPreferredSize(new Dimension(0,480));
    }

    void cancelDrag(){press=null;}

    void installDrag(Component component)
    {
        component.removeMouseListener(drag);component.removeMouseMotionListener(drag);
        component.addMouseListener(drag);component.addMouseMotionListener(drag);
        if(component instanceof Container)
            for(Component child:((Container)component).getComponents())installDrag(child);
    }

    static void styleBar(JScrollBar bar)
    {
        bar.setPreferredSize(new Dimension(6,0));
        bar.setBorder(BorderFactory.createEmptyBorder());
        bar.setUI(new BasicScrollBarUI()
        {
            @Override protected JButton createDecreaseButton(int orientation){return emptyButton();}
            @Override protected JButton createIncreaseButton(int orientation){return emptyButton();}
            private JButton emptyButton()
            {
                JButton button=new JButton();Dimension zero=new Dimension(0,0);
                button.setPreferredSize(zero);button.setMinimumSize(zero);button.setMaximumSize(zero);
                return button;
            }
            @Override protected void paintTrack(Graphics g,JComponent c,Rectangle r)
            {g.setColor(new Color(34,34,34));g.fillRect(r.x,r.y,r.width,r.height);}
            @Override protected void paintThumb(Graphics g,JComponent c,Rectangle r)
            {
                if(r.isEmpty() || !bar.isEnabled())return;
                g.setColor(isDragging || isThumbRollover()?new Color(145,145,145):new Color(85,85,85));
                g.fillRoundRect(r.x+1,r.y,Math.max(2,r.width-2),r.height,4,4);
            }
        });
    }
}
