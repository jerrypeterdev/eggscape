package com.eggcollection;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

/** PCM resource metadata and frame-aligned WAV slices; playback belongs to RuneLite. */
final class EggWave
{
    private final byte[] format, samples;
    private final int frameSize, sampleRate;

    private EggWave(byte[] format, byte[] samples, int frameSize, int sampleRate)
    {
        this.format=format;this.samples=samples;this.frameSize=frameSize;this.sampleRate=sampleRate;
    }

    static EggWave load(String resource) throws IOException
    {
        try(InputStream stream=EggWave.class.getResourceAsStream(resource))
        {
            if(stream==null)throw new IOException("Missing sound resource: "+resource);
            return decode(stream.readAllBytes());
        }
    }

    static EggWave decode(byte[] bytes) throws IOException
    {
        ByteBuffer input=ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
        if(bytes.length<12 || input.getInt()!=0x46464952 || input.getInt(8)!=0x45564157)
            throw new IOException("Expected a RIFF WAVE resource");
        byte[] format=null,samples=null;
        for(int position=12;position<=bytes.length-8;)
        {
            int id=input.getInt(position),length=input.getInt(position+4),start=position+8;
            if(length<0 || length>bytes.length-start)throw new IOException("Invalid WAV chunk size");
            if(id==0x20746d66)format=Arrays.copyOfRange(bytes,start,start+length);
            if(id==0x61746164)samples=Arrays.copyOfRange(bytes,start,start+length);
            long next=(long)start+length+(length&1);
            if(next>bytes.length)break;
            position=(int)next;
        }
        if(format==null || format.length<16 || samples==null)throw new IOException("Incomplete WAV resource");
        ByteBuffer fmt=ByteBuffer.wrap(format).order(ByteOrder.LITTLE_ENDIAN);
        int channels=Short.toUnsignedInt(fmt.getShort(2)),rate=fmt.getInt(4);
        int frameSize=Short.toUnsignedInt(fmt.getShort(12)),bits=Short.toUnsignedInt(fmt.getShort(14));
        if(fmt.getShort(0)!=1 || channels<1 || channels>2 || rate<=0 || bits!=16
            || frameSize!=channels*2 || fmt.getInt(8)!=(long)rate*frameSize || samples.length%frameSize!=0)
            throw new IOException("Expected frame-aligned 16-bit PCM audio");
        return new EggWave(format,samples,frameSize,rate);
    }

    long frames(){return samples.length/frameSize;}
    long durationNanos(){return Math.round(frames()*1_000_000_000.0/sampleRate);}
    long frameAt(long nanos){return nanos>=durationNanos()?frames():Math.max(0,nanos)*sampleRate/1_000_000_000L;}
    long segmentFrames(){return Math.max(1,sampleRate/4);}
    long nanosFor(long frames){return Math.round(frames*1_000_000_000.0/sampleRate);}

    byte[] slice(long first,long count)
    {
        if(first<0 || count<0 || first>frames() || count>frames()-first)throw new IllegalArgumentException("Invalid audio range");
        int size=Math.toIntExact(count*frameSize),formatPadding=format.length&1;
        ByteBuffer output=ByteBuffer.allocate(28+format.length+formatPadding+size).order(ByteOrder.LITTLE_ENDIAN);
        output.putInt(0x46464952).putInt(output.capacity()-8).putInt(0x45564157);
        output.putInt(0x20746d66).putInt(format.length).put(format);
        if(formatPadding!=0)output.put((byte)0);
        output.putInt(0x61746164).putInt(size).put(samples,Math.toIntExact(first*frameSize),size);
        return output.array();
    }
}
