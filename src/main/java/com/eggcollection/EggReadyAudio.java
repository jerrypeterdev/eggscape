package com.eggcollection;

interface EggReadyAudio
{
    void play();
    default void play(boolean idle){play();}
    void stop();
    EggReadyAudio SILENT=new EggReadyAudio()
    {
        public void play() {}
        public void stop() {}
    };
}
