package com.eggcollection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
class EggGame
{
    static final long HATCH_MILLIS = 60*60*1000L;
    static final long TUTORIAL_MILLIS = 15*60*1000L;
    static final long QUEST_BONUS_MILLIS = 15*60*1000L;
    static final EggPet[] PETS = EggPet.values();
    long hatchProgress;
    int progressVersion;
    long questBonusUsed;
    long pendingQuestBonus;
    boolean activeGameplay;
    boolean activeShiny;
    boolean lastHatchedShiny;
    boolean debugPetUsed;
    List<Boolean> readyIdleFlags=new ArrayList<>();
    List<EggPet> shinyQueue=new ArrayList<>();
    Map<String,Integer> shinyCollection=new LinkedHashMap<>();
    Map<String,Integer> bossKills=new LinkedHashMap<>();
    java.util.Set<String> shinyUnlocks=new java.util.HashSet<>();
    int collectionVersion;
    EggPet activePet;
    boolean awaitingSpin;
    Map<String, Integer> collection = new LinkedHashMap<>();
    boolean migrateCollection()
    {
        boolean changed=false;
        if (collectionVersion != 3)
        {
            hatchProgress=0;activePet=null;awaitingSpin=false;
            collection.clear();collectionVersion=3;changed=true;
        }
        if(progressVersion!=1)
        {
            // Preserve the fraction and banked eggs from the old 1,000-XP model.
            hatchProgress=Math.max(0,hatchProgress)*(HATCH_MILLIS/1000);
            progressVersion=1;changed=true;
        }
        evaluateLogUnlocks();
        return changed;
    }
    void advance(long millis)
    {
        if (millis <= 0 || (activePet == null && !awaitingSpin)) return;
        addProgress(millis);
        applyQuestBonus();
    }

    void awardQuestBonus(int quests)
    {
        if(quests<=0 || (activePet==null && !awaitingSpin))return;
        pendingQuestBonus+=quests*QUEST_BONUS_MILLIS;
        applyQuestBonus();
    }

    private void applyQuestBonus()
    {
        long bonus=Math.min(pendingQuestBonus,Math.min(QUEST_BONUS_MILLIS-questBonusUsed,
            Math.max(0,hatchRequirement()-hatchProgress)));
        if(bonus>0){addProgress(bonus);questBonusUsed+=bonus;pendingQuestBonus-=bonus;}
    }

    private void addProgress(long millis)
    {
        long before=completedProgressEggs(hatchProgress);
        hatchProgress+=millis;
        while(readyIdleFlags.size()<before)readyIdleFlags.add(false);
        for(long i=before;i<completedProgressEggs(hatchProgress);i++)readyIdleFlags.add(!activeGameplay);
    }
    boolean isTutorialEgg()
    {return activePet==EggPet.JERRY && !activeShiny && collection.getOrDefault(EggPet.JERRY.key,0)==0;}
    long hatchRequirement(){return isTutorialEgg()?TUTORIAL_MILLIS:HATCH_MILLIS;}
    private long completedProgressEggs(long progress)
    {
        long first=hatchRequirement();
        return progress<first?0:1+(progress-first)/HATCH_MILLIS;
    }
    boolean readyWasIdle(){return !readyIdleFlags.isEmpty() && readyIdleFlags.get(0);}
    void awardBossBonus(long millis)
    {
        if(activePet==null || millis<=0)return;
        activeGameplay=true;addProgress(Math.min(millis,Math.max(0,hatchRequirement()-hatchProgress)));
    }
    void recordBossKill(EggPet boss)
    {
        if(boss==null)return;
        int kills=bossKills.merge(EggBossCatalog.family(boss),1,Integer::sum);
        if(EggBossCatalog.killUnlock(boss) && kills>=100)queueShiny(boss);
    }
    void evaluateLogUnlocks()
    {
        for(EggPet group:new EggPet[]{EggPet.AHRIM,EggPet.BLOOD_MOON})
        {
            List<EggPet> members=EggBossCatalog.members(group);
            if(members.stream().allMatch(pet -> collection.getOrDefault(pet.key,0)>0))
                for(EggPet member:members)queueShiny(member);
        }
    }
    boolean queueShiny(EggPet pet)
    {
        if(!shinyUnlocks.add(pet.key))return false;
        shinyQueue.add(pet);return true;
    }
    boolean unlockRealPet(EggPet pet,boolean specific,Random random)
    {
        if(!EggBossCatalog.hasRealPet(pet))return false;
        List<EggPet> choices=specific?java.util.Collections.singletonList(pet):EggBossCatalog.members(pet);
        List<EggPet> available=new ArrayList<>();
        for(EggPet choice:choices)if(!shinyUnlocks.contains(choice.key))available.add(choice);
        List<EggPet> pool=available.isEmpty()?choices:available;
        EggPet reward=pool.get(random.nextInt(pool.size()));
        shinyUnlocks.add(reward.key);shinyQueue.add(reward);return true;
    }
    boolean receiveJerryMessage(String sender)
    {return EggJerryUnlock.authorized(sender) && queueShiny(EggPet.JERRY);}
    boolean debugPetDrop()
    {
        if(!canDebugPetDrop())return false;
        debugPetUsed=true;
        shinyUnlocks.add(activePet.key);shinyQueue.add(0,activePet);return true;
    }
    boolean canDebugPetDrop(){return activePet!=null && !activeShiny && !debugPetUsed;}
    int collectedCount(EggPet pet)
    {return collection.getOrDefault(pet.key,0)+shinyCollection.getOrDefault(pet.key,0);}
    boolean hasShiny(EggPet pet){return shinyCollection.getOrDefault(pet.key,0)>0;}
    int shinyUniquePets()
    {return (int)java.util.Arrays.stream(PETS).filter(pet -> shinyCollection.getOrDefault(pet.key,0)>0).count();}

    boolean canHatch()
    {
        return activePet != null && hatchProgress >= hatchRequirement();
    }

    EggPet hatch()
    {
        if (!canHatch()) return null;
        EggPet hatched = activePet;
        lastHatchedShiny=activeShiny;
        hatchProgress -= hatchRequirement();
        if(!readyIdleFlags.isEmpty())readyIdleFlags.remove(0);
        questBonusUsed=0;
        (activeShiny?shinyCollection:collection).merge(hatched.key, 1, Integer::sum);
        evaluateLogUnlocks();activeShiny=false;
        activePet = null;
        awaitingSpin = true;
        applyQuestBonus();
        return hatched;
    }

    boolean assignEgg()
    {
        if (activePet != null || awaitingSpin) return false;
        activePet = EggPet.JERRY;activeShiny=false;
        return true;
    }

    boolean skipToHatch()
    {
        if(activePet==null || canHatch())return false;
        hatchProgress=hatchRequirement();readyIdleFlags.clear();readyIdleFlags.add(false);
        return true;
    }

    EggPet spin(Random random)
    {
        if (!awaitingSpin || activePet != null) return null;
        activeShiny=!shinyQueue.isEmpty();
        activePet = activeShiny?shinyQueue.remove(0):randomPet(random);
        awaitingSpin = false;
        return activePet;
    }

    private EggPet randomPet(Random random)
    {
        // Equal chance per egg design; a Barrows egg can reveal any brother.
        Map<String, List<EggPet>> types = new LinkedHashMap<>();
        for (EggPet pet : PETS)
            if(!pet.tutorialOnly) types.computeIfAbsent(pet.eggResource, key -> new ArrayList<>()).add(pet);
        List<List<EggPet>> choices = new ArrayList<>(types.values());
        List<EggPet> pets = choices.get(random.nextInt(choices.size()));
        return pets.get(random.nextInt(pets.size()));
    }

    int uniquePets()
    {
        int unique = 0;
        for (EggPet pet : PETS)
            if (collectedCount(pet) > 0) unique++;
        return unique;
    }
}
