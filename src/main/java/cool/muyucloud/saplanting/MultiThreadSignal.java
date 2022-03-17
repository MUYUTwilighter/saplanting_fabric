package cool.muyucloud.saplanting;

import net.minecraft.server.MinecraftServer;
import org.apache.logging.log4j.Logger;

import java.util.LinkedList;

public class MultiThreadSignal {
    private static final Logger LOGGER = Saplanting.getLogger();
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

    public static void registerThread(Runnable function) {
        if (SIGNAL.thread != null) {
            return;
        }

        SIGNAL.taskQueue.clear();

        SIGNAL.thread = new Thread(function);
        SIGNAL.thread.setName("SaplantingItemEntityProcess");
    }

    public static void killThread() {
        if (SIGNAL.thread == null) {
            return;
        }
        SIGNAL.taskQueue.clear();
        SIGNAL.thread.interrupt();
        SIGNAL.thread = null;
        LOGGER.info("Item entity process thread has been discarded.");
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
        return SIGNAL.thread.isAlive();
    }

    public static boolean threadInterrupted() {
        return SIGNAL.thread.isInterrupted();
    }

    public static boolean threadRegistered() {
        return SIGNAL.thread == null;
    }

    public static void killThread(MinecraftServer server) {
        killThread();
    }
}
