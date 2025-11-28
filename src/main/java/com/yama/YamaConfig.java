package com.yama;

import net.runelite.client.config.*;

@ConfigGroup("yama")
public interface YamaConfig extends Config
{
    @Range(
            min = 0,
            max = 100
    )
    @Units(Units.PERCENT)
    @ConfigItem(
            keyName = "volume",
            name = "Volume",
            description = "Volume of Yama sound effects",
            position = 1
    )
    default int volume()
    {
        return 100; // default to 100%
    }
}
