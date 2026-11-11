package com.eggcollection;

import com.google.gson.Gson;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import org.junit.Test;
import static org.junit.Assert.*;

public class EggGameTest
{
    @Test public void firstEggRequiresAClickAndCannotBeRerolled()
    {
        EggGame game=new EggGame(); game.migrateCollection();
        game.advance(18000000L); assertEquals(0L,game.hatchProgress);
        assertFalse(game.canHatch()); assertNull(game.hatch());
        assertTrue(game.assignEgg());
        EggPet assigned=game.activePet;
        assertSame(EggPet.JERRY,assigned);
        assertFalse(game.assignEgg()); assertSame(assigned,game.activePet);
        game.advance(EggGame.TUTORIAL_MILLIS-1); assertFalse(game.canHatch());
        game.advance(1); assertTrue(game.canHatch()); assertTrue(game.collection.isEmpty());
        assertSame(assigned,game.hatch());
        assertEquals(Integer.valueOf(1),game.collection.get(assigned.key));
        assertEquals(0L,game.hatchProgress); assertNull(game.activePet);
        assertTrue(game.awaitingSpin); assertFalse(game.assignEgg());
        assertNotNull(game.spin(new Random(5)));
    }

    @Test public void allFiftySevenNormalEggDesignsCanBeSpunButJerryNeverRepeats()
    {
        Set<String> eggs=new HashSet<>();
        for(int type=0;type<57;type++)
        {
            final int selected=type;
            EggGame game=new EggGame();
            game.awaitingSpin=true;
            game.spin(new Random(){int call;
                @Override public int nextInt(int bound){return call++==0 ? selected : 0;}
            });
            eggs.add(game.activePet.eggResource);
            assertNotSame(EggPet.JERRY,game.activePet);
        }
        assertEquals(57,eggs.size());
        EggPet[] brothers={EggPet.AHRIM,EggPet.DHAROK,EggPet.GUTHAN,EggPet.KARIL,EggPet.TORAG,EggPet.VERAC};
        for(int brother=0;brother<6;brother++)
        {
            final int selected=brother;
            EggGame game=new EggGame();
            game.awaitingSpin=true;
            game.spin(new Random(){int call;
                @Override public int nextInt(int bound){return call++==0 ? 2 : selected;}
            });
            assertEquals(brothers[brother],game.activePet);
        }
    }

    @Test public void everyAssignedBossHatchesItsMatchingPetAndGetsANextEgg()
    {
        EggGame game=new EggGame();
        for(EggPet pet:EggGame.PETS)
        {
            game.activePet=pet;game.activeShiny=false; game.advance(3600000L);
            assertSame(pet,game.hatch());
            assertNull(game.activePet); assertTrue(game.awaitingSpin);
            assertNotNull(game.spin(new Random(7)));
            assertEquals(Integer.valueOf(1),game.collection.get(pet.key));
        }
        assertEquals(104,game.uniquePets());
    }

    @Test public void backgroundXpAndActiveEggSurviveReloadAndSeparateHatches()
    {
        EggGame game=new EggGame(); game.migrateCollection(); game.assignEgg();
        EggPet assigned=game.activePet;
        game.advance(EggGame.TUTORIAL_MILLIS); game.advance(7394400L);
        Gson gson=new Gson(); game=gson.fromJson(gson.toJson(game),EggGame.class);
        assertFalse(game.migrateCollection()); assertSame(assigned,game.activePet);
        assertSame(assigned,game.hatch()); assertEquals(7394400L,game.hatchProgress);
        assertNotNull(game.spin(new Random(2)));
        assertNotNull(game.hatch()); assertEquals(3794400L,game.hatchProgress);
        assertNotNull(game.spin(new Random(3)));
        assertNotNull(game.hatch()); assertEquals(194400L,game.hatchProgress);
        assertNull(game.hatch());
        assertEquals(3,game.collection.values().stream().mapToInt(Integer::intValue).sum());
    }

    @Test public void requestedResetClearsAllOldStatsOnceAndThenPreservesNewProgress()
    {
        Gson gson=new Gson();
        EggGame game=gson.fromJson("{\"hatchProgress\":51000000,\"collection\":{\"Tzrek-jad\":20,\"Nexling\":3}}",EggGame.class);
        assertTrue(game.migrateCollection()); assertNull(game.activePet);
        assertEquals(0L,game.hatchProgress); assertTrue(game.collection.isEmpty());
        game.assignEgg(); game.advance(273600L);
        EggPet assigned=game.activePet;
        game=gson.fromJson(gson.toJson(game),EggGame.class);
        assertFalse(game.migrateCollection()); assertEquals(273600L,game.hatchProgress);
        assertSame(assigned,game.activePet);
    }

    @Test public void wheelWaitAndItsSavedResultCannotRerollOrLoseBackgroundXp()
    {
        Gson gson=new Gson(); EggGame game=new EggGame();game.migrateCollection();
        assertNull(game.spin(new Random(1)));
        game.assignEgg();game.advance(EggGame.TUTORIAL_MILLIS);assertNotNull(game.hatch());
        assertTrue(game.awaitingSpin);assertNull(game.activePet);
        game.advance(273600L);game=gson.fromJson(gson.toJson(game),EggGame.class);
        assertTrue(game.awaitingSpin);assertEquals(273600L,game.hatchProgress);
        assertFalse(game.assignEgg());assertNull(game.hatch());
        EggPet winner=game.spin(new Random(4));assertNotNull(winner);
        game=gson.fromJson(gson.toJson(game),EggGame.class);
        assertSame(winner,game.activePet);assertFalse(game.awaitingSpin);
        assertNull(game.spin(new Random(5)));assertSame(winner,game.activePet);
        game.advance(273600L);assertEquals(547200L,game.hatchProgress);
    }

    @Test public void skipOnlyReadiesAnAssignedEggAndNeverHatchesOrDiscardsBankedXp()
    {
        EggGame game=new EggGame();game.migrateCollection();
        assertFalse(game.skipToHatch());assertNull(game.activePet);
        game.assignEgg();game.advance(273600L);
        assertTrue(game.skipToHatch());assertEquals(EggGame.TUTORIAL_MILLIS,game.hatchProgress);
        assertTrue(game.canHatch());assertTrue(game.collection.isEmpty());
        game.advance(273600L);assertFalse(game.skipToHatch());assertEquals(EggGame.TUTORIAL_MILLIS+273600L,game.hatchProgress);
        assertSame(EggPet.JERRY,game.hatch());assertFalse(game.skipToHatch());
        assertNotSame(EggPet.JERRY,game.spin(new Random(1)));
    }

    @Test public void newSharedEggsCanRevealEveryBossAndPhase()
    {
        EggPet[][] groups={{EggPet.BRANDA,EggPet.ELDRIC},{EggPet.BRUTUS,EggPet.DEMONIC_BRUTUS},
            {EggPet.MUSPAH_MELEE,EggPet.MUSPAH_RANGED,EggPet.MUSPAH_SHIELDED,EggPet.MUSPAH_TELEPORTING},
            {EggPet.ZULRAH_MAGMA,EggPet.ZULRAH_SERPENTINE,EggPet.ZULRAH_TANZANITE}};
        String[] names={"Royal Titans Egg","Brutus Egg","Phantom Muspah Egg","Zulrah Egg"};
        for(int group=0;group<groups.length;group++)for(int member=0;member<groups[group].length;member++)
        {
            final int selectedGroup=25+group,selectedMember=member;
            EggGame game=new EggGame();game.awaitingSpin=true;
            EggPet selected=game.spin(new Random(){int call;
                @Override public int nextInt(int bound){return call++==0?selectedGroup:selectedMember;}
            });
            assertSame(groups[group][member],selected);assertEquals(names[group],selected.eggName());
            game.skipToHatch();assertSame(selected,game.hatch());
            assertEquals(Integer.valueOf(1),game.collection.get(selected.key));
        }
    }

    @Test public void sharedWildernessEggsCanRevealBothMatchingBosses()
    {
        EggPet[][] groups={{EggPet.ARTIO,EggPet.CALLISTO},{EggPet.SPINDEL,EggPet.VENENATIS},
            {EggPet.CALVARION,EggPet.VETION}};
        String[] names={"Cub Egg","Arachnid Egg","Skeletal Egg"};
        for(int group=0;group<groups.length;group++)for(int member=0;member<2;member++)
        {
            final int selectedGroup=16+group,selectedMember=member;
            EggGame game=new EggGame();game.awaitingSpin=true;
            EggPet selected=game.spin(new Random(){int call;
                @Override public int nextInt(int bound){return call++==0?selectedGroup:selectedMember;}
            });
            assertSame(groups[group][member],selected);assertEquals(names[group],selected.eggName());
            game.skipToHatch();assertSame(selected,game.hatch());
            assertEquals(Integer.valueOf(1),game.collection.get(selected.key));
        }
    }

    @Test public void sharedMoonsAndKingsEggsCanRevealEveryMatchingBoss()
    {
        EggPet[][] groups={{EggPet.BLOOD_MOON,EggPet.BLUE_MOON,EggPet.ECLIPSE_MOON},
            {EggPet.DAGANNOTH_PRIME,EggPet.DAGANNOTH_REX,EggPet.DAGANNOTH_SUPREME}};
        String[] names={"Perilous Moons Egg","Dagannoth Kings Egg"};
        for(int group=0;group<groups.length;group++)for(int member=0;member<3;member++)
        {
            final int selectedGroup=10+group,selectedMember=member;
            EggGame game=new EggGame();game.awaitingSpin=true;
            EggPet selected=game.spin(new Random(){int call;
                @Override public int nextInt(int bound){return call++==0?selectedGroup:selectedMember;}
            });
            assertSame(groups[group][member],selected);assertEquals(names[group],selected.eggName());
            game.skipToHatch();assertSame(selected,game.hatch());
            assertEquals(Integer.valueOf(1),game.collection.get(selected.key));
        }
    }
}
