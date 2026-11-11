package com.eggcollection;

import java.util.HashSet;
import java.util.Set;

/** Only reward completions observed after this login's complete baseline. */
class EggQuestTracker
{
    private final Set<Integer> finished=new HashSet<>();
    private boolean initialized;
    void reset(){finished.clear();initialized=false;}
    int update(Set<Integer> current)
    {
        int added=0;
        for(int id:current)if(finished.add(id))added++;
        if(!initialized){initialized=true;return 0;}
        return added;
    }
}
