package valkyrienwarfare.mod.multithreaded;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.world.World;

/**
 * Only needs to run for the server end of things, the client end will just
 * receive more physics ticks as a result. Handles separate physics threads for
 * different worlds.
 * 
 * @author thebest108
 *
 */
public class VWThreadManager {

    // Potential memory leak here, always be sure to call the killVWThread() method
    // once a world unloads.
    private static final Map<World, VWThread> WORLDS_TO_THREADS = new HashMap<World, VWThread>();

    public static VWThread getVWThreadForWorld(World world) {
        return WORLDS_TO_THREADS.get(world);
    }

    public static void createVWThreadForWorld(World world) {
        WORLDS_TO_THREADS.put(world, new VWThread(world));
        WORLDS_TO_THREADS.get(world).start();
    }

    public static void killVWThreadForWorld(World world) {
        WORLDS_TO_THREADS.remove(world).kill();
    }
}
