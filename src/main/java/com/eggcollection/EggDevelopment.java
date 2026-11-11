package com.eggcollection;

/** Test-launcher opt-in; absent in normal RuneLite/Plugin Hub launches. */
final class EggDevelopment
{
    private EggDevelopment() {}
    static boolean enabled(){return Boolean.getBoolean("eggscape.developerTools");}
}
