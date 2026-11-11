package com.eggcollection;

interface EggSpinAudio
{
    void play(long offsetNanos);
    void stop();

    EggSpinAudio SILENT=new EggSpinAudio()
    {
        public void play(long offsetNanos) {}
        public void stop() {}
    };
}
