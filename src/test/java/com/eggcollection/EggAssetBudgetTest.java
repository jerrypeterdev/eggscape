package com.eggcollection;

import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import org.junit.Test;
import static org.junit.Assert.*;

public class EggAssetBudgetTest
{
    @Test public void collectionImagesFitTheSidebarMemoryBudget() throws Exception
    {
        for(EggPet pet:EggGame.PETS)
            for(String resource:new String[]{pet.petResource,pet.eggResource})
            {
                BufferedImage image=ImageIO.read(EggPlugin.class.getResource(resource));
                assertNotNull(resource,image);
                assertTrue(resource,(long)image.getWidth()*image.getHeight()*4<=256*1024);
            }
    }
}
