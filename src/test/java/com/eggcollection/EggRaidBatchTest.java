package com.eggcollection;

import com.google.gson.Gson;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class EggRaidBatchTest
{
    @Test public void eachNewSharedEggCanRevealEveryIncludedBoss()
    {
        EggPet[] groups={EggPet.ALCHEMICAL_HYDRA_ELECTRIC,EggPet.GREAT_OLM,
            EggPet.THE_MAIDEN_OF_SUGADINTI,EggPet.AKKHA,EggPet.ARCHIVE_GUARDIAN,
            EggPet.CRYSTALLINE_HUNLLEF,EggPet.DAWN};
        int[] sizes={4,6,6,6,8,2,2};
        for(int i=0;i<groups.length;i++)
        {
            List<EggPet> members=EggBossCatalog.members(groups[i]);
            assertEquals(sizes[i],members.size());
            List<String> designs=new ArrayList<>();
            for(EggPet pet:EggPet.values())if(!pet.tutorialOnly && !designs.contains(pet.eggResource))designs.add(pet.eggResource);
            final int design=designs.indexOf(groups[i].eggResource);
            Set<EggPet> outcomes=new HashSet<>();
            for(int j=0;j<members.size();j++)
            {
                final int member=j;
                EggGame game=new EggGame();game.awaitingSpin=true;
                outcomes.add(game.spin(new Random(){int call;
                    @Override public int nextInt(int bound){return call++==0?design:member;}
                }));
            }
            assertEquals(new HashSet<>(members),outcomes);
        }
        assertFalse(EggBossCatalog.same(EggPet.DUKE_SUCELLUS,EggPet.VARDORVIS));
        assertFalse(EggBossCatalog.same(EggPet.THE_WHISPERER,EggPet.THE_LEVIATHAN));
        assertTrue(EggBossCatalog.same(EggBossCatalog.source("Nylocas Vasilias"),EggPet.VERZIK_VITUR));
        assertTrue(EggBossCatalog.same(EggBossCatalog.source("Theatre of Blood (Hard Mode)"),EggPet.VERZIK_VITUR));
        assertTrue(EggBossCatalog.same(EggBossCatalog.source("Font of Consumption"),EggPet.ABYSSAL_SIRE));
    }

    @Test public void raidRewardNamesAndPetsQueueTheirOwnFamily()
    {
        String[][] rewards={{"Chambers of Xeric","Olmlet"},{"Theatre of Blood","Lil' Zik"},
            {"Tombs of Amascut","Tumeken's guardian"},{"Fractured Archive","Orion"},
            {"Alchemical Hydra","Ikkle hydra"},{"The Gauntlet","Youngllef"},
            {"Grotesque Guardians","Noon"},{"The Inferno","Jal-Nib-Rek"},
            {"Shellbane Gryphon","Gull"},{"Sol Heredit","Smol heredit"},
            {"Kraken","Pet kraken"},{"Abyssal Sire","Abyssal orphan"},
            {"Cerberus","Hellpuppy"},{"Araxxor","Nid"},
            {"Duke Sucellus","Baron"},{"Vardorvis","Butch"},
            {"The Whisperer","Wisp"},{"The Leviathan","Lil'viathan"},
            {"Thermonuclear Smoke Devil","Pet smoke devil"}};
        for(String[] pair:rewards)
        {
            EggPet boss=EggBossCatalog.source(pair[0]);assertNotNull(pair[0],boss);
            EggPet pet=EggBossCatalog.petItem(pair[1],boss);assertNotNull(pair[1],pet);
            assertTrue(pair[0],EggBossCatalog.same(boss,pet));
            EggGame game=new EggGame();game.migrateCollection();
            EggRewardDetector detector=new EggRewardDetector(new Random(1));
            detector.message(game,"New item added to your collection log: "+pair[1],100);
            assertEquals(1,game.shinyQueue.size());
            assertTrue(pair[1],EggBossCatalog.same(boss,game.shinyQueue.get(0)));
        }
    }

    @Test public void raidFightTimeSurvivesRoomBreaksButOnlyPaysOnceOnMatchingLoot()
    {
        EggBossTracker tracker=new EggBossTracker();
        for(int i=0;i<100;i++)tracker.observe(EggPet.GREAT_OLM,1000,true,EggPet.TEKTON,false);
        for(int i=0;i<300;i++)tracker.observe(EggPet.GREAT_OLM,1000,false,null,false);
        for(int i=0;i<100;i++)tracker.observe(EggPet.GREAT_OLM,1000,true,EggPet.GREAT_OLM,false);
        assertEquals(0,tracker.claim(EggPet.GREAT_OLM,EggPet.VERZIK_VITUR));
        assertEquals(200_000,tracker.claim(EggPet.GREAT_OLM,EggBossCatalog.source("Chambers of Xeric")));
        assertEquals(0,tracker.claim(EggPet.GREAT_OLM,EggPet.GREAT_OLM));
        tracker.observe(EggPet.GREAT_OLM,1000,true,EggPet.TEKTON,false);tracker.reset();
        assertEquals(0,tracker.claim(EggPet.GREAT_OLM,EggPet.GREAT_OLM));
    }

    @Test public void existingSaveAndNewRaidQueueSurviveCatalogExpansion()
    {
        EggGame game=new EggGame();game.migrateCollection();game.activePet=EggPet.NEX;
        game.hatchProgress=123456;game.collection.put(EggPet.MOLE.key,7);
        game.shinyCollection.put(EggPet.JAD.key,1);game.debugPetUsed=true;
        game.queueShiny(EggPet.OCELLUS);
        game=new Gson().fromJson(new Gson().toJson(game),EggGame.class);
        assertFalse(game.migrateCollection());assertSame(EggPet.NEX,game.activePet);
        assertEquals(123456,game.hatchProgress);assertEquals(7,game.collectedCount(EggPet.MOLE));
        assertTrue(game.hasShiny(EggPet.JAD));assertTrue(game.debugPetUsed);
        game.skipToHatch();game.hatch();assertSame(EggPet.OCELLUS,game.spin(new Random(0)));
        assertTrue(game.activeShiny);
    }
}
