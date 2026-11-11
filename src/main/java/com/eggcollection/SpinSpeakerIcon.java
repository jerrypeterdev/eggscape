package com.eggcollection;

import java.awt.*;
import javax.swing.Icon;

/** Small native UI icon, styled to match the gold RuneLite panel controls. */
final class SpinSpeakerIcon implements Icon
{
    private final boolean muted;
    SpinSpeakerIcon(boolean muted){this.muted=muted;}
    @Override public int getIconWidth(){return 30;}
    @Override public int getIconHeight(){return 30;}
    @Override public void paintIcon(Component component,Graphics graphics,int x,int y)
    {
        Graphics2D g=(Graphics2D)graphics.create();g.translate(x,y);
        Color gold=new Color(235,190,75);
        Polygon speaker=new Polygon(new int[]{3,8,16,16,8,3},new int[]{11,11,4,26,19,19},6);
        g.setStroke(new BasicStroke(3));g.setColor(Color.BLACK);g.draw(speaker);
        g.setColor(muted ? new Color(85,85,80) : new Color(135,123,95));g.fill(speaker);
        g.setStroke(new BasicStroke(1));g.setColor(muted ? new Color(130,130,120) : gold);g.draw(speaker);
        if(muted)
        {
            g.setStroke(new BasicStroke(5,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
            g.setColor(Color.BLACK);g.drawLine(3,4,27,26);g.drawLine(27,4,3,26);
            g.setStroke(new BasicStroke(2.5f,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
            g.setColor(gold);g.drawLine(3,4,27,26);g.drawLine(27,4,3,26);
        }
        else
        {
            g.setColor(Color.BLACK);g.setStroke(new BasicStroke(4));
            g.drawArc(12,8,11,14,-65,130);g.drawArc(11,3,17,24,-65,130);
            g.setColor(gold);g.setStroke(new BasicStroke(2));
            g.drawArc(12,8,11,14,-65,130);g.drawArc(11,3,17,24,-65,130);
        }
        g.dispose();
    }
}
