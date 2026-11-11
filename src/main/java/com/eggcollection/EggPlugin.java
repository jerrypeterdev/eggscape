package com.eggcollection;

import com.google.gson.Gson;
import java.util.EnumMap;
import java.util.Map;
import java.util.Random;
import java.util.HashSet;
import java.util.Set;
import java.util.HashMap;
import java.util.Collection;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.swing.SwingUtilities;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Skill;
import net.runelite.api.Player;
import net.runelite.api.NPC;
import net.runelite.api.Quest;
import net.runelite.api.QuestState;
import net.runelite.api.VarPlayer;
import net.runelite.api.ChatMessageType;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.InteractingChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;


import net.runelite.client.plugins.loottracker.LootReceived;
import net.runelite.client.events.ServerNpcLoot;
import net.runelite.client.events.NpcLootReceived;
import net.runelite.client.game.ItemStack;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;

@PluginDescriptor(name = "EggScape – Pet Collection", description = "Collect boss pets through gameplay")

public class EggPlugin extends Plugin
{
    private static final String GROUP = "eggcollection";
    @Inject private Client client;
    @Inject private ConfigManager config;
    @Inject private ClientToolbar toolbar;
    @Inject private EggPanel panel;
    @Inject private ClientThread clientThread;
    @Inject private ItemManager itemManager;
    private final Gson gson = new Gson();
    private final EggXpTracker xpTracker = new EggXpTracker();
    private final EggActivityTracker activity = new EggActivityTracker();
    private final EggQuestTracker quests = new EggQuestTracker();
    private WorldPoint previousLocation;
    private int sessionTicks;
    private boolean questDirty;
    private final Random random = new Random();
    private final EggBossTracker bossTracker=new EggBossTracker();
    private final EggRewardDetector rewards=new EggRewardDetector(random);
    private final EggLootGuard lootGuard=new EggLootGuard();
    private EggGame game;
    private String profile;
    private NavigationButton navigation;

    @Override protected void startUp()
    {
        navigation = NavigationButton.builder().tooltip("EggScape")
            .icon(EggPanel.sidebarIcon()).panel(panel).build();
        toolbar.addNavigation(navigation);
        resetSession();
        boolean spinMuted=Boolean.parseBoolean(config.getConfiguration(GROUP,"spinMuted"));
        boolean jerrySoundPack=Boolean.parseBoolean(config.getConfiguration(GROUP,"jerrySoundPack"));
        SwingUtilities.invokeLater(() -> {
            panel.setSpinMuted(spinMuted);
            panel.setSpinMuteAction(muted -> clientThread.invoke(() -> config.setConfiguration(GROUP,"spinMuted",muted)));
            panel.setJerrySoundPackEnabled(jerrySoundPack);
            panel.setJerrySoundPackAction(enabled -> clientThread.invoke(() -> config.setConfiguration(GROUP,"jerrySoundPack",enabled)));
        });
        clientThread.invoke(() -> {
            if (client.getGameState() == GameState.LOGGED_IN) updateProgress();
        });
    }
    @Override protected void shutDown()
    {
        toolbar.removeNavigation(navigation);
        resetSession();
    }
    @Subscribe public void onGameStateChanged(GameStateChanged event)
    {
        GameState state = event.getGameState();
        if(state!=GameState.LOGGED_IN)activity.pause();
        if (state == GameState.LOGIN_SCREEN || state == GameState.HOPPING
            || state == GameState.CONNECTION_LOST) resetSession();
    }
    @Subscribe public void onMenuOptionClicked(MenuOptionClicked event)
    {
        String action=event.getMenuAction().name();
        if(client.getGameState()==GameState.LOGGED_IN && !action.startsWith("RUNELITE")
            && !action.equals("CANCEL") && !action.equals("UNKNOWN") && !action.startsWith("EXAMINE"))
            activity.action(System.nanoTime());
    }
    @Subscribe public void onVarbitChanged(VarbitChanged event)
    {
        if(event.getVarpId()==VarPlayer.QUEST_POINTS)questDirty=true;
    }
    @Subscribe public void onLootReceived(LootReceived event)
    {
        String type=event.getType().name();
        if(type.equals("NPC") || type.equals("EVENT"))confirmedLoot(event.getName(),event.getItems());
    }
    @Subscribe public void onServerNpcLoot(ServerNpcLoot event)
    {if(event.getComposition()!=null)confirmedLoot(event.getComposition().getName(),event.getItems());}
    @Subscribe public void onNpcLootReceived(NpcLootReceived event)
    {if(event.getNpc()!=null)confirmedLoot(event.getNpc().getName(),event.getItems());}

    private boolean liveProfile()
    {return game!=null && profile!=null && profile.equals(config.getRSProfileKey()) && client.getGameState()==GameState.LOGGED_IN;}

    private void confirmedLoot(String source,Collection<ItemStack> items)
    {
        if(!liveProfile())return;
        EggPet boss=EggBossCatalog.source(source);if(boss==null)return;
        int tick=client.getTickCount();
        String key=EggBossCatalog.family(boss)+"|"+items.stream().map(item -> item.getId()+":"+item.getQuantity()).sorted().collect(Collectors.joining(","));
        if(!lootGuard.accept(key,tick))return;
        game.recordBossKill(boss);
        long before=game.hatchProgress;
        game.awardBossBonus(bossTracker.claim(game.activePet,boss));
        rewards.loot(game,boss,tick);
        for(ItemStack item:items)rewards.item(game,itemManager.getItemComposition(item.getId()).getName(),tick);
        if(game.hatchProgress>before)
            client.addChatMessage(ChatMessageType.GAMEMESSAGE,"","EggScape: +"+((game.hatchProgress-before)/1000)+" seconds of hatch progress for "+source+".","");
        saveProgress();refresh();
    }
    @Subscribe public void onChatMessage(ChatMessage event)
    {
        if(!liveProfile())return;
        if(EggRewardDetector.isJerryPrivateMessage(event.getType(),event.getName()))
        {
            if(game.receiveJerryMessage(event.getName())){saveProgress();refresh();}
        }
        else if(event.getType()==ChatMessageType.GAMEMESSAGE || event.getType()==ChatMessageType.SPAM)
        {
            int before=game.shinyUnlocks.size();
            int queued=game.shinyQueue.size();
            rewards.message(game,event.getMessage(),client.getTickCount());
            if(game.shinyUnlocks.size()!=before || game.shinyQueue.size()!=queued){saveProgress();refresh();}
        }
    }
    @Subscribe public void onAnimationChanged(AnimationChanged event)
    {if(event.getActor() instanceof Player)checkJerryPunch((Player)event.getActor());}
    @Subscribe public void onInteractingChanged(InteractingChanged event)
    {if(event.getSource() instanceof Player)checkJerryPunch((Player)event.getSource());}
    private void checkJerryPunch(Player attacker)
    {
        if(liveProfile() && EggJerryUnlock.punchedLocalPlayer(attacker,client.getLocalPlayer()) && game.queueShiny(EggPet.JERRY))
        {saveProgress();refresh();}
    }
    @Subscribe public void onGameTick(GameTick event)
    {
        if (client.getGameState() == GameState.LOGGED_IN) updateProgress();
    }
    private void updateProgress()
    {
        String current = config.getRSProfileKey();
        Player player=client.getLocalPlayer();
        if (current == null || player==null){activity.pause();return;}
        boolean loaded = !current.equals(profile);
        if (loaded)
        {
            profile = current;
            xpTracker.reset();
            activity.reset();quests.reset();previousLocation=null;sessionTicks=0;questDirty=true;
            bossTracker.reset();rewards.reset();lootGuard.reset();
            String saved = config.getRSProfileConfiguration(GROUP, "gameTest100V2");
            try { game = saved == null ? new EggGame() : gson.fromJson(saved, EggGame.class); }
            catch (RuntimeException ex) { game = new EggGame(); }
            if (game == null || game.collection == null) game = new EggGame();
            // Keep the collection and convert the old XP fraction to time once.
            if (game.migrateCollection())
            {
                config.setRSProfileConfiguration(GROUP, "gameTest100V2", gson.toJson(game));
            }
        }
        // GameTick runs after stat updates, avoiding partial login snapshots and
        // queued StatChanged events overwriting a newer baseline with old values.
        Map<Skill, Integer> currentXp = new EnumMap<>(Skill.class);
        for (Skill skill : Skill.values())
            if (skill != Skill.OVERALL) currentXp.put(skill, client.getSkillExperience(skill));
        long gained = xpTracker.update(currentXp);
        WorldPoint location=player.getWorldLocation();
        boolean moved=previousLocation!=null && !previousLocation.equals(location);
        previousLocation=location;
        boolean combat=EggActivityTracker.isCombat(player);
        long elapsed=activity.update(System.nanoTime(),gained>0 || moved || player.getAnimation()!=-1 || combat);
        game.activeGameplay=activity.isActive();game.advance(elapsed);
        if(!game.shinyUnlocks.contains(EggPet.JERRY.key))
            for(Player nearby:client.getPlayers())if(EggJerryUnlock.authorized(nearby.getName()))checkJerryPunch(nearby);
        EggPet target=combat && player.getInteracting() instanceof NPC?EggBossCatalog.source(player.getInteracting().getName()):null;
        int region=location==null?0:location.getRegionID();
        if(player.isDead())bossTracker.reset();
        else bossTracker.observe(game.activePet,elapsed,game.activeGameplay,target,region==14131 || region==14231);
        sessionTicks++;
        if(loaded || questDirty || sessionTicks%10==0)
        {
            Set<Integer> finished=finishedQuests();
            if(finished!=null){game.awardQuestBonus(quests.update(finished));questDirty=false;}
            saveProgress();
        }
        refresh();
    }

    private Set<Integer> finishedQuests()
    {
        Set<Integer> finished=new HashSet<>();
        try{for(Quest quest:Quest.values())if(quest.getState(client)==QuestState.FINISHED)finished.add(quest.getId());}
        catch(RuntimeException ex){return null;}
        return finished;
    }

    private void saveProgress()
    {
        if(game!=null && profile!=null && profile.equals(config.getRSProfileKey()))
            config.setRSProfileConfiguration(GROUP,"gameTest100V2",gson.toJson(game));
    }
    private void resetSession()
    {
        saveProgress();profile=null;game=null;xpTracker.reset();activity.reset();quests.reset();
        bossTracker.reset();rewards.reset();lootGuard.reset();
        previousLocation=null;sessionTicks=0;questDirty=false;refresh();
    }
    private void refresh()
    {
        refresh(null);
    }
    private void refresh(EggPet revealedPet)
    {
        refresh(revealedPet,null);
    }
    private void refresh(EggPet revealedPet, EggPet spunEgg)
    {
        EggGame expectedGame = game;
        String expectedProfile = profile;
        EggGame snapshot = game == null ? null : gson.fromJson(gson.toJson(game), EggGame.class);
        SwingUtilities.invokeLater(() -> {
        panel.setDebugPetDropAction(() -> clientThread.invoke(() -> {
            if(!EggDevelopment.enabled())return;
            if(!liveProfile() || game!=expectedGame || !profile.equals(expectedProfile))return;
            if(game.debugPetDrop())
            {
                saveProgress();refresh();
                client.addChatMessage(ChatMessageType.GAMEMESSAGE,"","EggScape debug: queued a shiny "+game.activePet.displayName+" egg for the next spin.","");
            }
        }));
        panel.showGame(snapshot, () -> clientThread.invoke(() -> {
            // Ignore a queued click if the character/session changed before it ran.
            if (game == null || game != expectedGame || expectedProfile == null
                || !expectedProfile.equals(config.getRSProfileKey())
                || client.getGameState() != GameState.LOGGED_IN) return;
            EggPet hatched = game.hatch();
            bossTracker.reset();
            if (hatched != null)
            {
                config.setRSProfileConfiguration(GROUP, "gameTest100V2", gson.toJson(game));
                refresh(hatched);
            }
        }), () -> clientThread.invoke(() -> {
            if (game == null || game.activePet != null || game.awaitingSpin || game != expectedGame || expectedProfile == null
                || !expectedProfile.equals(config.getRSProfileKey())
                || client.getGameState() != GameState.LOGGED_IN) return;
            // Start the clock at the click; time before choosing is excluded.
            Map<Skill, Integer> currentXp = new EnumMap<>(Skill.class);
            for (Skill skill : Skill.values())
                if (skill != Skill.OVERALL) currentXp.put(skill, client.getSkillExperience(skill));
            xpTracker.reset(); xpTracker.update(currentXp);
            activity.pause();
            if (game.assignEgg())
            {
                bossTracker.reset();
                config.setRSProfileConfiguration(GROUP, "gameTest100V2", gson.toJson(game));
                refresh();
            }
        }), () -> clientThread.invoke(() -> {
            if (game == null || game != expectedGame || expectedProfile == null
                || !expectedProfile.equals(config.getRSProfileKey())
                || client.getGameState() != GameState.LOGGED_IN) return;
            if(!EggDevelopment.enabled())return;
            game = new EggGame(); game.migrateCollection();
            bossTracker.reset();rewards.reset();lootGuard.reset();
            xpTracker.reset();
            activity.pause();
            config.setRSProfileConfiguration(GROUP, "gameTest100V2", gson.toJson(game));
            refresh();
        }), () -> clientThread.invoke(() -> {
            if (game == null || game != expectedGame || expectedProfile == null
                || !expectedProfile.equals(config.getRSProfileKey())
                || client.getGameState() != GameState.LOGGED_IN) return;
            EggPet selected = game.spin(random);
            bossTracker.reset();
            if (selected != null)
            {
                // Commit the choice before animation: closing the UI cannot reroll it.
                config.setRSProfileConfiguration(GROUP,"gameTest100V2",gson.toJson(game));
                refresh(null,selected);
            }
        }), () -> clientThread.invoke(() -> {
            if (game == null || game != expectedGame || expectedProfile == null
                || !expectedProfile.equals(config.getRSProfileKey())
                || client.getGameState() != GameState.LOGGED_IN) return;
            if(EggDevelopment.enabled() && game.skipToHatch())
            {
                config.setRSProfileConfiguration(GROUP,"gameTest100V2",gson.toJson(game));
                refresh();
            }
        }), revealedPet,spunEgg);
        });
    }
}

