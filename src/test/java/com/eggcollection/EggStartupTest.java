package com.eggcollection;

import com.google.inject.Injector;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import net.runelite.client.RuneLite;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginManager;
import org.junit.Test;
import static org.junit.Assert.*;

public class EggStartupTest
{
    @Test public void realHostLoaderCanInstantiateWithoutLootTrackerServiceExports() throws Exception
    {
        // Exercise RuneLite's dependency validation, the path that rejected the plugin.
        // Injection is stubbed because no game client is running in this unit test.
        Injector previous=RuneLite.getInjector();
        Injector stub=(Injector)Proxy.newProxyInstance(getClass().getClassLoader(),new Class<?>[]{Injector.class},
            (proxy,method,args)->method.getName().equals("createChildInjector")?proxy:null);
        try
        {
            RuneLite.setInjector(stub);
            Class<?> factoryClass=Class.forName("net.runelite.client.plugins.PluginModuleFactory");
            Constructor<?> factoryConstructor=factoryClass.getDeclaredConstructor();factoryConstructor.setAccessible(true);
            Constructor<?> managerConstructor=PluginManager.class.getDeclaredConstructors()[0];managerConstructor.setAccessible(true);
            Object manager=managerConstructor.newInstance(false,false,null,null,null,null,factoryConstructor.newInstance());
            Method instantiate=PluginManager.class.getDeclaredMethod("instantiate",List.class,Class.class);instantiate.setAccessible(true);
            Plugin plugin=(Plugin)instantiate.invoke(manager,List.of(),EggPlugin.class);
            assertTrue(plugin instanceof EggPlugin);assertSame(stub,plugin.getInjector());
        }
        finally{RuneLite.setInjector(previous);}
    }
}
