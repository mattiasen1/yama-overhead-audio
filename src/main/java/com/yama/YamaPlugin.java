package com.yama;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.audio.AudioPlayer;


import net.runelite.api.NPC;
import net.runelite.api.events.OverheadTextChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

@Slf4j
@PluginDescriptor(
        name = "Yama Overhead Audio",
        description = "Plays sounds when Yama says specific overhead lines",
        tags = {"yama", "boss", "audio"}
)
public class YamaPlugin extends Plugin
{
    // Name as it appears in-game
    private static final String YAMA_NAME = "Yama";

    @Inject
    private YamaConfig config;

    @Inject
    private AudioPlayer audioPlayer;

    // Types of Yama events based on overhead text
    private enum YamaEvent
    {
        CHANGING_AGGRO,
        INFERNAL_ROCKFALL,
        SHADOW_STOMP,
        PHASE_TRANSITION,
        PLAYER_DIES,
        DEFEATED,
        DEFEATED_LOW_HP,
        DEFEATED_PERFECT
    }

    @Provides
    YamaConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(YamaConfig.class);
    }

    @Override
    protected void startUp()
    {
        log.info("Yama Overhead Audio plugin started");
    }

    @Override
    protected void shutDown()
    {
        log.info("Yama Overhead Audio plugin stopped");
    }

    @Subscribe
    public void onOverheadTextChanged(OverheadTextChanged event)
    {
        if (!(event.getActor() instanceof NPC))
        {
            return;
        }

        NPC npc = (NPC) event.getActor();
        String name = npc.getName();
        if (name == null || !name.equalsIgnoreCase(YAMA_NAME))
        {
            return;
        }

        String text = event.getOverheadText();
        if (text == null)
        {
            return;
        }

        log.debug("Yama said: {}", text);

        YamaEvent eventType = classifyDialogue(text);
        if (eventType == null)
        {
            return;
        }

        switch (eventType)
        {
            case CHANGING_AGGRO:
                playSound("yama_changing_aggro.wav");
                break;

            case INFERNAL_ROCKFALL:
                playSound("yama_infernal_rockfall.wav");
                break;

            case SHADOW_STOMP:
                playSound("yama_shadow_stomp.wav");
                break;

            case PHASE_TRANSITION:
                playSound("yama_phase_transition.wav");
                break;

            case PLAYER_DIES:
                playSound("yama_player_dies.wav");
                break;

            case DEFEATED:
                playSound("yama_defeated.wav");
                break;

            case DEFEATED_LOW_HP:
                playSound("yama_defeated_low_hp.wav");
                break;

            case DEFEATED_PERFECT:
                playSound("yama_defeated_perfect.wav");
                break;
        }
    }

    private void playSound(String resourceName)
    {
        float gainDb = getGainDb();

        try
        {
            audioPlayer.play(YamaPlugin.class, "/" + resourceName, gainDb);
        }
        catch (Exception e)
        {
            log.warn("Failed to play Yama sound: {}", resourceName, e);
        }
    }

    private float getGainDb()
    {
        int volPercent = config.volume(); // 0–100
        if (volPercent < 0)
        {
            volPercent = 0;
        }
        if (volPercent > 100)
        {
            volPercent = 100;
        }

        // Map 0–100% to -60 dB .. 0 dB
        if (volPercent == 0)
        {
            // Effectively mute
            return -80.0f;
        }

        double volume = volPercent / 100.0; // 0.0–1.0
        double gain = -60.0 + 60.0 * volume; // -60 dB at 0%, 0 dB at 100%
        return (float) gain;
    }



    // Turn a specific line of text into one of our YamaEvent types
    private YamaEvent classifyDialogue(String rawText)
    {
        if (rawText == null)
        {
            return null;
        }

        String txt = rawText.trim();
        String lower = txt.toLowerCase();

        if (lower.startsWith("yama:"))
        {
            lower = lower.substring("yama:".length()).trim();
        }

        // CHANGING AGGRO
        if (lower.contains("your strike lacks bite")
                || lower.contains("a change of pace"))
        {
            return YamaEvent.CHANGING_AGGRO;
        }

        // INFERNAL ROCKFALL
        if (lower.contains("colabi, infernus")
                || lower.contains("ven, estella infernus")
                || lower.contains("colabesur infernus"))
        {
            return YamaEvent.INFERNAL_ROCKFALL;
        }

        // SHADOW STOMP
        if (lower.contains("ven, umbra eclipta")
                || lower.contains("umbra, proriumpse")
                || lower.contains("umbra apprerendehe"))
        {
            return YamaEvent.SHADOW_STOMP;
        }

        // PHASE TRANSITIONING
        if (lower.equals("begone")
                || lower.equals("you bore me.")
                || lower.equals("you bore me")
                || lower.equals("enough.")
                || lower.equals("enough"))
        {
            return YamaEvent.PHASE_TRANSITION;
        }

        // PLAYER DIES
        if (lower.contains("lacking.")
                || lower.contains("pathetic.")
                || lower.contains("the price is paid")
                || lower.contains("another day, perhaps")
                || lower.contains("another day, another soul")
                || lower.contains("your soul was always mine"))
        {
            return YamaEvent.PLAYER_DIES;
        }

        // DEFEATING YAMA (normal)
        if (lower.contains("fair is fair")
                || lower.contains("your reward, as agreed")
                || lower.contains("a satisfying warmup. collect your loot and let us continue")
                || lower.contains("a passing performance"))
        {
            return YamaEvent.DEFEATED;
        }

        // DEFEATING YAMA – LOW HP
        if (lower.contains("a close fight. your nerve was well held")
                || lower.contains("a good risk, adequately rewarded")
                || lower.contains("excellent performance. again"))
        {
            return YamaEvent.DEFEATED_LOW_HP;
        }

        // DEFEATING YAMA – PERFECT KILL
        if (lower.contains("effective, if a bit cowardly")
                || lower.contains("risk averse, are we"))
        {
            return YamaEvent.DEFEATED_PERFECT;
        }

        return null;
    }
}
