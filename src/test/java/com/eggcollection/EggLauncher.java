package com.eggcollection;
import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;
public class EggLauncher
{
    public static void main(String[] args) throws Exception
    {
        ExternalPluginManager.loadBuiltin(EggPlugin.class);
        RuneLite.main(args);
    }
}
