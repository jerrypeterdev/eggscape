package com.eggcollection;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.*;
import javax.imageio.metadata.*;
import javax.imageio.stream.FileImageOutputStream;

/** Writes actual Swing-rendered frames for visual review. */
final class GifPreview implements AutoCloseable
{
    private final ImageWriter writer=ImageIO.getImageWritersByFormatName("gif").next();
    private final FileImageOutputStream stream;
    private boolean first=true;
    GifPreview(File file)throws IOException
    {stream=new FileImageOutputStream(file);writer.setOutput(stream);writer.prepareWriteSequence(null);}

    void frame(BufferedImage image,int hundredths)throws IOException
    {
        IIOMetadata metadata=writer.getDefaultImageMetadata(ImageTypeSpecifier.createFromRenderedImage(image),writer.getDefaultWriteParam());
        String format=metadata.getNativeMetadataFormatName();
        IIOMetadataNode root=(IIOMetadataNode)metadata.getAsTree(format);
        IIOMetadataNode control=(IIOMetadataNode)root.getElementsByTagName("GraphicControlExtension").item(0);
        control.setAttribute("disposalMethod","none");control.setAttribute("userInputFlag","FALSE");
        control.setAttribute("transparentColorFlag","FALSE");control.setAttribute("delayTime",Integer.toString(hundredths));
        control.setAttribute("transparentColorIndex","0");
        if(first)
        {
            IIOMetadataNode extensions=new IIOMetadataNode("ApplicationExtensions");
            IIOMetadataNode loop=new IIOMetadataNode("ApplicationExtension");
            loop.setAttribute("applicationID","NETSCAPE");loop.setAttribute("authenticationCode","2.0");
            loop.setUserObject(new byte[]{1,0,0});extensions.appendChild(loop);root.appendChild(extensions);first=false;
        }
        metadata.setFromTree(format,root);
        writer.writeToSequence(new IIOImage(image,null,metadata),writer.getDefaultWriteParam());
    }
    @Override public void close()throws IOException
    {try{writer.endWriteSequence();}finally{writer.dispose();stream.close();}}
}
