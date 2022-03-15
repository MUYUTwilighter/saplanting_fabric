package cool.muyucloud.saplanting;

import net.minecraft.server.MinecraftServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.LinkedList;

public class MultiThreadSignal {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final MultiThreadSignal SIGNAL = new MultiThreadSignal();

    private final LinkedList<Object> taskQueue;
    private Thread thread;

    private MultiThreadSignal() {
        this.taskQueue = new LinkedList<>();
        this.thread = null;
    }

    public static void threadStart() {
        SIGNAL.thread.start();
    }

    public static boolean registerThread(Thread thread) {
        if (SIGNAL.thread != null) {
            return false;
        }

        SIGNAL.taskQueue.clear();

        SIGNAL.thread = thread;
        SIGNAL.thread.setName("SaplantingItemEntityProcess");
        return true;
    }

    public static void killThread() {
        if (SIGNAL.thread == null) {
            return;
        }
        SIGNAL.taskQueue.clear();
        SIGNAL.thread.interrupt();
        SIGNAL.thread = null;
    }

    public static void addTask(Object itemEntity) {
        if (SIGNAL.taskQueue.size() > 1000) {
            LOGGER.warn("Too many items in queue! Cleared " + SIGNAL.taskQueue.size() + " tasks.");
            SIGNAL.taskQueue.clear();
        }
        if (itemEntity != null) {
            SIGNAL.taskQueue.offer(itemEntity);
        }
    }

    public static Object popTask() {
        Object item = null;
        while (!taskEmpty() && item == null) {
            item = SIGNAL.taskQueue.poll();
        }
        return item;
    }

    public static boolean taskEmpty() {
        return SIGNAL.taskQueue.isEmpty();
    }

    public static boolean threadAlive() {
        if (SIGNAL.thread == null) {
            return false;
        }
        return SIGNAL.thread.isAlive();
    }

    public static void killThread(MinecraftServer server) {
        killThread();
    }
}
