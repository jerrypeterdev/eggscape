package com.eggcollection;

import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;
import javax.imageio.ImageIO;
import org.junit.Test;
import static org.junit.Assert.*;

public class EggArtworkTest
{
    @Test public void catalogResourcesExistAndAllEggsKeepTheBasePixelShape() throws Exception
    {
        BufferedImage base = ImageIO.read(EggPlugin.class.getResource("egg.png"));
        Set<String> saveKeys = new HashSet<>();
        for (EggPet pet : EggGame.PETS)
        {
            assertTrue("Duplicate save key",saveKeys.add(pet.key));
            BufferedImage egg = ImageIO.read(EggPlugin.class.getResource(pet.eggResource));
            BufferedImage boss = ImageIO.read(EggPlugin.class.getResource(pet.petResource));
            assertTrue(boss.getWidth()>0 && boss.getHeight()>0);
            assertEquals(pet.name(),256,egg.getWidth());
            assertEquals(pet.name(),256,egg.getHeight());
            for(int y=0;y<256;y++) for(int x=0;x<256;x++)
            {
                int original=base.getRGB(x,y),actual=egg.getRGB(x,y);
                assertEquals(pet.name()+" silhouette",original>>>24,actual>>>24);
                if((original>>>24)>0 && (original&0xffffff)==0)
                    assertEquals(pet.name()+" black outline",original,actual);
            }
        }
        assertEquals(104,saveKeys.size());
    }

    @Test public void collectionOrderIsAlphabeticalIncludingApostrophes()
    {
        assertEquals(104,EggPet.COLLECTION_ORDER.size());
        String previous="";
        for(EggPet pet:EggPet.COLLECTION_ORDER)
        {
            assertTrue(previous.compareTo(pet.sortName())<=0);previous=pet.sortName();
        }
        assertEquals(EggPet.ABYSSAL_SIRE,EggPet.COLLECTION_ORDER.get(0));
        assertEquals(EggPet.ZULRAH_TANZANITE,EggPet.COLLECTION_ORDER.get(103));
        assertTrue(EggPet.COLLECTION_ORDER.indexOf(EggPet.KALPHITE_QUEEN)<EggPet.COLLECTION_ORDER.indexOf(EggPet.KREEARRA));
        assertTrue(EggPet.COLLECTION_ORDER.indexOf(EggPet.KREEARRA)<EggPet.COLLECTION_ORDER.indexOf(EggPet.KRIL));
    }
}
