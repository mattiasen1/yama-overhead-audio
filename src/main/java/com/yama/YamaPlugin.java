package com.yama;

import com.google.inject.Provides;
import javax.inject.Inject;

import net.runelite.api.NPC;
import net.runelite.api.events.OverheadTextChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

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

    private final SoundPlayer soundPlayer = new SoundPlayer();

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
        System.out.println("Yama Overhead Audio plugin started");
    }

    @Override
    protected void shutDown()
    {
        System.out.println("Yama Overhead Audio plugin stopped");
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

        System.out.println("Yama said: " + text);

        YamaEvent eventType = classifyDialogue(text);
        if (eventType == null)
        {
            return;
        }

        double volume = getVolume();

        switch (eventType)
        {
            case CHANGING_AGGRO:
                soundPlayer.playSound("/yama_changing_aggro.wav", volume);
                break;

            case INFERNAL_ROCKFALL:
                soundPlayer.playSound("/yama_infernal_rockfall.wav", volume);
                break;

            case SHADOW_STOMP:
                soundPlayer.playSound("/yama_shadow_stomp.wav", volume);
                break;

            case PHASE_TRANSITION:
                soundPlayer.playSound("/yama_phase_transition.wav", volume);
                break;

            case PLAYER_DIES:
                soundPlayer.playSound("/yama_player_dies.wav", volume);
                break;

            case DEFEATED:
                soundPlayer.playSound("/yama_defeated.wav", volume);
                break;

            case DEFEATED_LOW_HP:
                soundPlayer.playSound("/yama_defeated_low_hp.wav", volume);
                break;

            case DEFEATED_PERFECT:
                soundPlayer.playSound("/yama_defeated_perfect.wav", volume);
                break;
        }
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
    private double getVolume()
    {
        int volPercent = config.volume(); // 0–100
        // Clamp just in case
        if (volPercent < 0) volPercent = 0;
        if (volPercent > 100) volPercent = 100;
        return volPercent / 100.0; // 0.0–1.0
    }

}
