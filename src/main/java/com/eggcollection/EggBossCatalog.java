package com.eggcollection;

import java.util.*;

/** Explicit boss/reward aliases; never fuzzy-match a player message. */
final class EggBossCatalog
{
    static String clean(String value)
    {return value==null?"":value.replaceAll("<[^>]*>","").toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]","");}
    static String family(EggPet pet){return pet==null?"":pet.eggResource;}
    static List<EggPet> members(EggPet pet)
    {
        List<EggPet> result=new ArrayList<>();
        for(EggPet candidate:EggPet.values())if(family(candidate).equals(family(pet)))result.add(candidate);
        return result;
    }
    static EggPet source(String name)
    {
        String key=clean(name);
        for(EggPet pet:EggPet.values())
            if(!pet.tutorialOnly && (key.equals(clean(pet.displayName)) || key.equals(clean(pet.key))
                || key.equals(clean(pet.petResource.replace("pets/","").replace(".png","")))))return pet;
        switch(key)
        {
            case "barrows":case "barrowschest":return EggPet.AHRIM;
            case "moonsofperil":case "perilousmoons":case "lunarchest":return EggPet.BLOOD_MOON;
            case "dagannothkings":return EggPet.DAGANNOTH_PRIME;
            case "royaltitans":case "branda":case "eldric":return EggPet.BRANDA;
            case "phantommuspah":return EggPet.MUSPAH_MELEE;
            case "zulrah":return EggPet.ZULRAH_MAGMA;
            case "fightcaves":case "tzhaarfightcave":case "tzhaarfightcaves":return EggPet.JAD;
            case "thehueycoatl":return EggPet.HUEYCOATL;
            case "themadangel":return EggPet.MAD_ANGEL;
            case "themaggotking":return EggPet.MAGGOT_KING;
            case "alchemicalhydra":return EggPet.ALCHEMICAL_HYDRA_SERPENTINE;
            case "chambersofxeric":case "chambersofxericchallengemode":return EggPet.GREAT_OLM;
            case "theatreofblood":case "theatreofbloodhardmode":case "theatreofbloodentrymode":return EggPet.VERZIK_VITUR;
            case "tombsofamascut":case "tombsofamascutexpertmode":case "tombsofamascutentrymode":return EggPet.TUMEKENS_WARDEN;
            case "thefracturedarchive":case "fracturedarchive":return EggPet.OCELLUS;
            case "thearchiveguardian":return EggPet.ARCHIVE_GUARDIAN;
            case "vanguard":return EggPet.VANGUARDS;
            case "nylocasvasilias":case "nylocastoxobolos":case "nylocashagios":case "nylocasprinkipas":return EggPet.NYLOCAS_ISCHYROS;
            case "thepestilentbloat":return EggPet.PESTILENT_BLOAT;
            case "grotesqueguardians":return EggPet.DUSK;
            case "thegauntlet":case "gauntlet":case "gauntletchest":return EggPet.CRYSTALLINE_HUNLLEF;
            case "thecorruptedgauntlet":case "corruptedgauntlet":case "corruptedgauntletchest":return EggPet.CORRUPTED_HUNLLEF;
            case "inferno":case "theinferno":return EggPet.TZKAL_ZUK;
            case "fortiscolosseum":case "solheredit":return EggPet.SOL_HEREDIT;
            case "fontofconsumption":return EggPet.ABYSSAL_SIRE;
            default:return null;
        }
    }
    static boolean same(EggPet egg,EggPet boss){return egg!=null && boss!=null && family(egg).equals(family(boss));}
    static boolean groupLogUnlock(EggPet pet){return same(pet,EggPet.AHRIM)||same(pet,EggPet.BLOOD_MOON);}
    static boolean raid(EggPet pet)
    {return same(pet,EggPet.GREAT_OLM)||same(pet,EggPet.VERZIK_VITUR)
        ||same(pet,EggPet.TUMEKENS_WARDEN)||same(pet,EggPet.OCELLUS);}
    static boolean killUnlock(EggPet pet)
    {return pet==EggPet.OBOR || pet==EggPet.BRYOPHYTA || pet==EggPet.CRAZY_ARCHAEOLOGIST
        || pet==EggPet.DERANGED_ARCHAEOLOGIST || pet==EggPet.REVENANT_MALEDICTUS;}
    static boolean hasRealPet(EggPet pet){return pet!=null && pet!=EggPet.JERRY && !groupLogUnlock(pet) && !killUnlock(pet);}
    static EggPet petItem(String name,EggPet context)
    {
        switch(clean(name))
        {
            case "tzrekjad":return EggPet.JAD;
            case "babymole":return EggPet.MOLE;
            case "petkreearra":return EggPet.KREEARRA;
            case "petkriltsutsaroth":return EggPet.KRIL;
            case "nexling":return EggPet.NEX;
            case "petzilyana":return EggPet.ZILYANA;
            case "petdarkcore":return EggPet.CORPOREAL_BEAST;
            case "petgeneralgraardor":return EggPet.GRAARDOR;
            case "kalphiteprincess":return EggPet.KALPHITE_QUEEN;
            case "petdagannothprime":return EggPet.DAGANNOTH_PRIME;
            case "petdagannothrex":return EggPet.DAGANNOTH_REX;
            case "petdagannothsupreme":return EggPet.DAGANNOTH_SUPREME;
            case "sraracha":return EggPet.SARACHNIS;
            case "scurry":return EggPet.SCURRIUS;
            case "huberte":return EggPet.HUEYCOATL;
            case "callistocub":return EggPet.CALLISTO;
            case "venenatisspiderling":return EggPet.VENENATIS;
            case "vetionjr":return EggPet.VETION;
            case "petchaoselemental":return context==EggPet.CHAOS_FANATIC?context:EggPet.CHAOS_ELEMENTAL;
            case "princeblackdragon":return EggPet.KING_BLACK_DRAGON;
            case "scorpiasoffspring":return EggPet.SCORPIA;
            case "bran":return EggPet.BRANDA;
            case "beef":return EggPet.BRUTUS;
            case "muphin":return EggPet.MUSPAH_MELEE;
            case "petsnakeling":return EggPet.ZULRAH_MAGMA;
            case "moxi":return EggPet.AMOXLIATL;
            case "dom":return EggPet.DOOM_OF_MOKHAIOTL;
            case "aggy":return EggPet.MAD_ANGEL;
            case "maggotmarquess":return EggPet.MAGGOT_KING;
            case "littlenightmare":return EggPet.PHOSANIS_NIGHTMARE;
            case "vorki":return EggPet.VORKATH;
            case "yami":return EggPet.YAMA;
            case "ikklehydra":return EggPet.ALCHEMICAL_HYDRA_SERPENTINE;
            case "olmlet":return EggPet.GREAT_OLM;
            case "lilzik":return EggPet.VERZIK_VITUR;
            case "tumekensguardian":return EggPet.TUMEKENS_WARDEN;
            case "orion":return EggPet.OCELLUS;
            case "baron":return EggPet.DUKE_SUCELLUS;
            case "butch":return EggPet.VARDORVIS;
            case "wisp":return EggPet.THE_WHISPERER;
            case "lilviathan":return EggPet.THE_LEVIATHAN;
            case "gull":return EggPet.SHELLBANE_GRYPHON;
            case "jalnibrek":return EggPet.TZKAL_ZUK;
            case "youngllef":return EggPet.CRYSTALLINE_HUNLLEF;
            case "petkraken":return EggPet.CAVE_KRAKEN;
            case "hellpuppy":return EggPet.CERBERUS;
            case "abyssalorphan":return EggPet.ABYSSAL_SIRE;
            case "noon":return EggPet.DUSK;
            case "petsmokedevil":return EggPet.THERMONUCLEAR_SMOKE_DEVIL;
            case "smolheredit":return EggPet.SOL_HEREDIT;
            case "nid":return EggPet.ARAXXOR;
            default:return null;
        }
    }
    static boolean dagannoth(EggPet pet){return same(pet,EggPet.DAGANNOTH_PRIME);}
}
