package com.eggcollection;

import java.awt.*;
import javax.swing.ImageIcon;

/** Live UI decoration: preserve original sprite pixels and identify shiny rewards. */
final class EggShinyStyle
{
    static ImageIcon icon(ImageIcon base)
    {
        return new ImageIcon(base.getImage())
        {
            @Override public void paintIcon(Component c,Graphics g,int x,int y)
            {super.paintIcon(c,g,x,y);paint(g,x,y,getIconWidth(),getIconHeight());}
        };
    }
    static void paint(Graphics graphics,int x,int y,int width,int height)
    {
        Graphics2D g=(Graphics2D)graphics.create();g.translate(x,y);
        int size=Math.max(3,width/18);
        star(g,width/7,height/5,size,new Color(255,220,90));
        star(g,width*6/7,height/3,size,Color.WHITE);
        star(g,width*4/5,height*4/5,size,new Color(255,220,90));
        g.dispose();
    }
    private static void star(Graphics2D g,int x,int y,int r,Color colour)
    {
        g.setColor(Color.BLACK);g.drawLine(x-r-1,y,x+r+1,y);g.drawLine(x,y-r-1,x,y+r+1);
        g.setColor(colour);g.drawLine(x-r,y,x+r,y);g.drawLine(x,y-r,x,y+r);
        g.fillRect(x-1,y-1,3,3);
    }
}
