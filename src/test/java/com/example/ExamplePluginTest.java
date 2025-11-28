package com.example;

import com.yama.YamaPlugin;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class ExamplePluginTest
{
    public static void main(String[] args) throws Exception
    {
        ExternalPluginManager.loadBuiltin(YamaPlugin.class);
        RuneLite.main(args);
    }
}
