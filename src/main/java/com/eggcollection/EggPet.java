package com.eggcollection;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/** Stable save keys are separate from the names shown in the panel. */
enum EggPet
{
    JERRY("Jerry", "Jerry", "jerry-egg.png", "Jerry.png",true),
    JAD("Tzrek-jad", "Jad", "jad-egg.png", "TzTok-Jad.png"),
    MOLE("Baby mole", "Giant Mole", "giant-mole-egg.png", "Giant-Mole.png"),
    AHRIM("Ahrim the Blighted", "Ahrim", "barrows-egg.png", "Ahrim the Blighted.png"),
    DHAROK("Dharok the Wretched", "Dharok", "barrows-egg.png", "Dharok the Wretched.png"),
    GUTHAN("Guthan the Infested", "Guthan", "barrows-egg.png", "Guthan the Infested.png"),
    KARIL("Karil the Tainted", "Karil", "barrows-egg.png", "Karil the Tainted.png"),
    TORAG("Torag the Corrupted", "Torag", "barrows-egg.png", "Torag the Corrupted.png"),
    VERAC("Verac the Defiled", "Verac", "barrows-egg.png", "Verac_the_Defiled.png"),
    KREEARRA("Pet Kree'arra", "Kree'arra", "kree-arra-egg.png", "Kree'arra.png"),
    KRIL("Pet K'ril Tsutsaroth", "K'ril", "kril-tsutsaroth-egg.png", "K'ril Tsutsaroth.png"),
    NEX("Nexling", "Nex", "nex-egg.png", "Nex.png"),
    ZILYANA("Pet Zilyana", "Zilyana", "commander-zilyana-egg.png", "Commander Zilyana.png"),
    CORPOREAL_BEAST("Pet dark core", "Corporeal Beast", "corporeal-beast-egg.png", "Corporeal Beast.png"),
    GRAARDOR("Pet General Graardor", "Graardor", "general-graardor-egg.png", "General Graardor.png"),
    KALPHITE_QUEEN("Kalphite Princess", "Kalphite Queen", "kalphite-queen-egg.png", "Kalphite Queen.png"),
    BLOOD_MOON("Blood Moon", "Blood Moon", "perilous-moons-egg.png", "Blood Moon.png"),
    BLUE_MOON("Blue Moon", "Blue Moon", "perilous-moons-egg.png", "Blue Moon.png"),
    ECLIPSE_MOON("Eclipse Moon", "Eclipse Moon", "perilous-moons-egg.png", "Eclipse Moon.png"),
    DAGANNOTH_PRIME("Dagannoth Prime", "Dagannoth Prime", "dagannoth-kings-egg.png", "Dagannoth Prime.png"),
    DAGANNOTH_REX("Dagannoth Rex", "Dagannoth Rex", "dagannoth-kings-egg.png", "Dagannoth Rex.png"),
    DAGANNOTH_SUPREME("Dagannoth Supreme", "Dagannoth Supreme", "dagannoth-kings-egg.png", "Dagannoth Supreme.png"),
    DERANGED_ARCHAEOLOGIST("Deranged Archaeologist", "Deranged Archaeologist", "deranged-archaeologist-egg.png", "Deranged Archaeologist.png"),
    SARACHNIS("Sraracha", "Sarachnis", "sarachnis-egg.png", "Sarachnis.png"),
    SCURRIUS("Scurrius", "Scurrius", "scurrius-egg.png", "Scurrius.png"),
    HUEYCOATL("Hueycoatl", "Hueycoatl", "hueycoatl-egg.png", "The Hueycoatl.png"),
    ARTIO("Artio", "Artio", "cub-egg.png", "Artio.png"),
    CALLISTO("Callisto", "Callisto", "cub-egg.png", "Callisto.png"),
    SPINDEL("Spindel", "Spindel", "arachnid-egg.png", "Spindel.png"),
    VENENATIS("Venenatis", "Venenatis", "arachnid-egg.png", "Venenatis.png"),
    CALVARION("Calvar'ion", "Calvar'ion", "skeletal-egg.png", "Calvar'ion.png"),
    VETION("Vet'ion", "Vet'ion", "skeletal-egg.png", "Vet'ion.png"),
    CHAOS_ELEMENTAL("Chaos Elemental", "Chaos Elemental", "chaos-elemental-egg.png", "Chaos Elemental.png"),
    CHAOS_FANATIC("Chaos Fanatic", "Chaos Fanatic", "chaos-fanatic-egg.png", "Chaos Fanatic.png"),
    CRAZY_ARCHAEOLOGIST("Crazy Archaeologist", "Crazy Archaeologist", "crazy-archaeologist-egg.png", "Crazy Archaeologist.png"),
    KING_BLACK_DRAGON("Prince Black Dragon", "King Black Dragon", "king-black-dragon-egg.png", "King Black Dragon.png"),
    REVENANT_MALEDICTUS("Revenant Maledictus", "Revenant Maledictus", "revenant-maledictus-egg.png", "Revenant Maledictus.png"),
    SCORPIA("Scorpia", "Scorpia", "scorpia-egg.png", "Scorpia.png"),
    BRANDA("Branda the Fire Queen", "Branda the Fire Queen", "royal-titans-egg.png", "Branda the Fire Queen.png"),
    ELDRIC("Eldric the Ice King", "Eldric the Ice King", "royal-titans-egg.png", "Eldric the Ice King.png"),
    BRUTUS("Brutus", "Brutus", "brutus-egg.png", "Brutus.png"),
    DEMONIC_BRUTUS("Demonic Brutus", "Demonic Brutus", "brutus-egg.png", "Demonic Brutus.png"),
    MUSPAH_MELEE("Phantom Muspah (Melee)", "Phantom Muspah (Melee)", "phantom-muspah-egg.png", "Phantom Muspah (Melee).png"),
    MUSPAH_RANGED("Phantom Muspah (Ranged)", "Phantom Muspah (Ranged)", "phantom-muspah-egg.png", "Phantom Muspah (Ranged).png"),
    MUSPAH_SHIELDED("Phantom Muspah (Shielded)", "Phantom Muspah (Shielded)", "phantom-muspah-egg.png", "Phantom Muspah (Shielded).png"),
    MUSPAH_TELEPORTING("Phantom Muspah (Teleporting)", "Phantom Muspah (Teleporting)", "phantom-muspah-egg.png", "Phantom Muspah (Teleporting).png"),
    ZULRAH_MAGMA("Zulrah (Magma)", "Zulrah (Magma)", "zulrah-egg.png", "Zulrah (Magma).png"),
    ZULRAH_SERPENTINE("Zulrah (Serpentine)", "Zulrah (Serpentine)", "zulrah-egg.png", "Zulrah (Serpentine).png"),
    ZULRAH_TANZANITE("Zulrah (Tanzanite)", "Zulrah (Tanzanite)", "zulrah-egg.png", "Zulrah (Tanzanite).png"),
    AMOXLIATL("Amoxliatl", "Amoxliatl", "amoxliatl-egg.png", "Amoxliatl.png"),
    BRYOPHYTA("Bryophyta", "Bryophyta", "bryophyta-egg.png", "Bryophyta.png"),
    DOOM_OF_MOKHAIOTL("Doom of Mokhaiotl", "Doom of Mokhaiotl", "doom-of-mokhaiotl-egg.png", "Doom of Mokhaiotl.png"),
    MAD_ANGEL("Mad Angel", "Mad Angel", "mad-angel-egg.png", "Mad Angel.png"),
    MAGGOT_KING("Maggot King", "Maggot King", "maggot-king-egg.png", "Maggot King.png"),
    OBOR("Obor", "Obor", "obor-egg.png", "Obor.png"),
    PHOSANIS_NIGHTMARE("Phosani's Nightmare", "Phosani's Nightmare", "phosanis-nightmare-egg.png", "Phosani's Nightmare.png"),
    VORKATH("Vorkath", "Vorkath", "vorkath-egg.png", "Vorkath.png"),
    YAMA("Yama", "Yama", "yama-egg.png", "Yama.png"),
    ALCHEMICAL_HYDRA_ELECTRIC("Alchemical Hydra (Electric)", "Alchemical Hydra (Electric)", "alchemical-hydra-egg.png", "Alchemical Hydra (Electric).png"),
    ALCHEMICAL_HYDRA_EXTINGUISHED("Alchemical Hydra (Extinguished)", "Alchemical Hydra (Extinguished)", "alchemical-hydra-egg.png", "Alchemical Hydra (Extinguished).png"),
    ALCHEMICAL_HYDRA_FIRE("Alchemical Hydra (Fire)", "Alchemical Hydra (Fire)", "alchemical-hydra-egg.png", "Alchemical Hydra (Fire).png"),
    ALCHEMICAL_HYDRA_SERPENTINE("Alchemical Hydra (Serpentine)", "Alchemical Hydra (Serpentine)", "alchemical-hydra-egg.png", "Alchemical Hydra (Serpentine).png"),
    GREAT_OLM("Great Olm", "Great Olm", "chambers-of-xeric-egg.png", "Great Olm.png"),
    TEKTON("Tekton", "Tekton", "chambers-of-xeric-egg.png", "Tekton.png"),
    VANGUARDS("Vanguards", "Vanguards", "chambers-of-xeric-egg.png", "Vanguards.png"),
    VASA_NISTIRIO("Vasa Nistirio", "Vasa Nistirio", "chambers-of-xeric-egg.png", "Vasa Nistirio.png"),
    VESPULA("Vespula", "Vespula", "chambers-of-xeric-egg.png", "Vespula.png"),
    MUTTADILE("Muttadile", "Muttadile", "chambers-of-xeric-egg.png", "Muttadile.png"),
    THE_MAIDEN_OF_SUGADINTI("The Maiden of Sugadinti", "The Maiden of Sugadinti", "theatre-of-blood-egg.png", "The Maiden of Sugadinti.png"),
    PESTILENT_BLOAT("Pestilent Bloat", "Pestilent Bloat", "theatre-of-blood-egg.png", "Pestilent Bloat.png"),
    NYLOCAS_ISCHYROS("Nylocas Ischyros", "Nylocas Ischyros", "theatre-of-blood-egg.png", "Nylocas Ischyros.png"),
    SOTETSEG("Sotetseg", "Sotetseg", "theatre-of-blood-egg.png", "Sotetseg.png"),
    XARPUS("Xarpus", "Xarpus", "theatre-of-blood-egg.png", "Xarpus.png"),
    VERZIK_VITUR("Verzik Vitur", "Verzik Vitur", "theatre-of-blood-egg.png", "Verzik Vitur.png"),
    AKKHA("Akkha", "Akkha", "tombs-of-amascut-egg.png", "Akkha.png"),
    BA_BA("Ba-Ba", "Ba-Ba", "tombs-of-amascut-egg.png", "Ba-Ba.png"),
    KEPHRI("Kephri", "Kephri", "tombs-of-amascut-egg.png", "Kephri.png"),
    ZEBAK("Zebak", "Zebak", "tombs-of-amascut-egg.png", "Zebak.png"),
    ELIDINIS_WARDEN("Elidinis' Warden", "Elidinis' Warden", "tombs-of-amascut-egg.png", "Elidinis' Warden.png"),
    TUMEKENS_WARDEN("Tumeken's Warden", "Tumeken's Warden", "tombs-of-amascut-egg.png", "Tumeken's Warden.png"),
    ARCHIVE_GUARDIAN("Archive Guardian", "Archive Guardian", "fractured-archive-egg.png", "Archive Guardian.png"),
    EZEKIEL("Ezekiel", "Ezekiel", "fractured-archive-egg.png", "Ezekiel.png"),
    BORK("Bork", "Bork", "fractured-archive-egg.png", "Bork.png"),
    MOIA("Moia", "Moia", "fractured-archive-egg.png", "Moia.png"),
    DREADNOTH_THE_JELLIED("Dreadnoth the Jellied", "Dreadnoth the Jellied", "fractured-archive-egg.png", "Dreadnoth the Jellied.png"),
    XARTH("Xarth", "Xarth", "fractured-archive-egg.png", "Xarth.png"),
    DAGONHAI_ELDER("Dagon'hai Elder", "Dagon'hai Elder", "fractured-archive-egg.png", "Dagon'hai Elder.png"),
    OCELLUS("Ocellus", "Ocellus", "fractured-archive-egg.png", "Ocellus.png"),
    DUKE_SUCELLUS("Duke Sucellus", "Duke Sucellus", "duke-sucellus-egg.png", "Duke Sucellus.png"),
    VARDORVIS("Vardorvis", "Vardorvis", "vardorvis-egg.png", "Vardorvis.png"),
    THE_WHISPERER("The Whisperer", "The Whisperer", "whisperer-egg.png", "The Whisperer.png"),
    THE_LEVIATHAN("The Leviathan", "The Leviathan", "leviathan-egg.png", "The Leviathan.png"),
    SHELLBANE_GRYPHON("Shellbane Gryphon", "Shellbane Gryphon", "shellbane-gryphon-egg.png", "Shellbane Gryphon.png"),
    TZKAL_ZUK("TzKal-Zuk", "TzKal-Zuk", "inferno-egg.png", "TzKal-Zuk.png"),
    CRYSTALLINE_HUNLLEF("Crystalline Hunllef", "Crystalline Hunllef", "hunllef-egg.png", "Crystalline Hunllef.png"),
    CORRUPTED_HUNLLEF("Corrupted Hunllef", "Corrupted Hunllef", "hunllef-egg.png", "Corrupted Hunllef.png"),
    CAVE_KRAKEN("Cave Kraken", "Kraken", "kraken-egg.png", "Cave Kraken.png"),
    CERBERUS("Cerberus", "Cerberus", "cerberus-egg.png", "Cerberus.png"),
    ABYSSAL_SIRE("Abyssal Sire", "Abyssal Sire", "abyssal-sire-egg.png", "Abyssal Sire.png"),
    DAWN("Dawn", "Dawn", "grotesque-guardians-egg.png", "Dawn.png"),
    DUSK("Dusk", "Dusk", "grotesque-guardians-egg.png", "Dusk.png"),
    THERMONUCLEAR_SMOKE_DEVIL("Thermonuclear Smoke Devil", "Thermonuclear Smoke Devil", "thermonuclear-smoke-devil-egg.png", "Thermonuclear Smoke Devil.png"),
    SOL_HEREDIT("Sol Heredit", "Sol Heredit", "sol-heredit-egg.png", "Sol Heredit.png"),
    ARAXXOR("Araxxor", "Araxxor", "araxxor-egg.png", "Araxxor.png");

    static final List<EggPet> COLLECTION_ORDER=Arrays.stream(values())
        .sorted(Comparator.comparing(EggPet::sortName))
        .collect(Collectors.toUnmodifiableList());

    final String key;
    final String displayName;
    final String eggResource;
    final String petResource;
    final boolean tutorialOnly;

    String sortName(){return displayName.replace("'", "").toLowerCase(Locale.ROOT);}

    String eggName()
    {
        if(eggResource.endsWith("barrows-egg.png"))return "Barrows Egg";
        if(eggResource.endsWith("perilous-moons-egg.png"))return "Perilous Moons Egg";
        if(eggResource.endsWith("dagannoth-kings-egg.png"))return "Dagannoth Kings Egg";
        if(eggResource.endsWith("cub-egg.png"))return "Cub Egg";
        if(eggResource.endsWith("arachnid-egg.png"))return "Arachnid Egg";
        if(eggResource.endsWith("skeletal-egg.png"))return "Skeletal Egg";
        if(eggResource.endsWith("royal-titans-egg.png"))return "Royal Titans Egg";
        if(eggResource.endsWith("brutus-egg.png"))return "Brutus Egg";
        if(eggResource.endsWith("phantom-muspah-egg.png"))return "Phantom Muspah Egg";
        if(eggResource.endsWith("zulrah-egg.png"))return "Zulrah Egg";
        if(eggResource.endsWith("alchemical-hydra-egg.png"))return "Alchemical Hydra Egg";
        if(eggResource.endsWith("chambers-of-xeric-egg.png"))return "Chambers of Xeric Egg";
        if(eggResource.endsWith("theatre-of-blood-egg.png"))return "Theatre of Blood Egg";
        if(eggResource.endsWith("tombs-of-amascut-egg.png"))return "Tombs of Amascut Egg";
        if(eggResource.endsWith("fractured-archive-egg.png"))return "The Fractured Archive Egg";
        if(eggResource.endsWith("inferno-egg.png"))return "Inferno Egg";
        if(eggResource.endsWith("hunllef-egg.png"))return "Hunllef Egg";
        if(eggResource.endsWith("kraken-egg.png"))return "Kraken Egg";
        if(eggResource.endsWith("grotesque-guardians-egg.png"))return "Grotesque Guardians Egg";
        return displayName + " Egg";
    }

    EggPet(String key, String displayName, String eggResource, String petResource)
    {
        this(key,displayName,eggResource,petResource,false);
    }

    EggPet(String key, String displayName, String eggResource, String petResource, boolean tutorialOnly)
    {
        this.key = key;
        this.displayName = displayName;
        this.eggResource = "eggs/" + eggResource;
        this.petResource = "pets/" + petResource;
        this.tutorialOnly = tutorialOnly;
    }
}
