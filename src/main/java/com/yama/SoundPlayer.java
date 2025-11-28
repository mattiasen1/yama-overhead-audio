package com.yama;

import javax.sound.sampled.*;
import java.io.InputStream;

public class SoundPlayer
{
    /**
     * @param resourcePath path to the wav in resources (e.g. "/yama_changing_aggro.wav")
     * @param volume       0.0 (mute) to 1.0 (full volume)
     */
    public void playSound(String resourcePath, double volume)
    {
        // Mute if volume is 0
        if (volume <= 0.0)
        {
            return;
        }

        try (InputStream in = getClass().getResourceAsStream(resourcePath))
        {
            if (in == null)
            {
                System.err.println("Missing sound resource: " + resourcePath);
                return;
            }

            AudioInputStream audio = AudioSystem.getAudioInputStream(new java.io.BufferedInputStream(in));

            Clip clip = AudioSystem.getClip();
            clip.open(audio);

            // Try to set volume if supported
            if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN))
            {
                FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);

                float min = gainControl.getMinimum(); // typically e.g. -80.0
                float max = gainControl.getMaximum(); // typically e.g. 6.0

                // Linear mapping volume [0.0,1.0] -> [min,max]
                float gain = (float) (min + (max - min) * volume);
                gainControl.setValue(gain);
            }

            clip.start();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
