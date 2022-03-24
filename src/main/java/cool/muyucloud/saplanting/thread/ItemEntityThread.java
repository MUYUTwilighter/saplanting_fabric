package cool.muyucloud.saplanting.thread;

import cool.muyucloud.saplanting.Saplanting;
import net.minecraft.server.MinecraftServer;

import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ItemEntityThread {
    private static final ItemEntityThread THREAD = new ItemEntityThread();

    private boolean isThreadExists;
    private boolean isThreadWaiting;
    private boolean isScheduledKill;
    private final ThreadPoolExecutor threadPoolExecutor;
    private final LinkedList<Object> taskQueue;

    private ItemEntityThread() {
        this.isThreadExists = false;
        this.isThreadWaiting = false;
        this.isScheduledKill = true;

        this.threadPoolExecutor = new ThreadPoolExecutor(
                1, 1, 0L,
                TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>()
        );

        this.taskQueue = new LinkedList<>();
    }

    /* Thread Signature Accessors */
    public static boolean isThreadExists() {
        return THREAD.isThreadExists;
    }

    public static boolean isThreadWaiting() {
        return THREAD.isThreadWaiting;
    }

    public static boolean isUnexpectedKill() {
        return THREAD.isScheduledKill;
    }

    /* Thread Operations */
    public static void initThread(Runnable function) {
        if (THREAD.isThreadExists) {
            return;
        }
        THREAD.threadPoolExecutor.execute(function);
        THREAD.isThreadExists = true;
        THREAD.isThreadWaiting = false;
        THREAD.isScheduledKill = true;
    }

    public static void onServerStopping(MinecraftServer server) {
        THREAD.taskQueue.clear();
    }

    public synchronized static void wakeUp() {
        THREAD.threadPoolExecutor.shutdownNow();
        THREAD.isThreadWaiting = false;
    }

    public synchronized static void sleep() {
        THREAD.isThreadWaiting = true;
        Thread.onSpinWait();
    }

    public static void markAsStopped() {
        THREAD.isThreadExists = false;
        THREAD.isThreadWaiting = false;
    }

    /* Task Queue Operations */
    // please ensure that input is valid!
    public static void addTask(Object item) {
        if (THREAD.taskQueue.size() > 1000) {
            Saplanting.getLogger().warn("Too many items! Cleared " + THREAD.taskQueue.size() + " tasks.");
            THREAD.taskQueue.clear();
        }
        THREAD.taskQueue.add(item);
    }

    public static Object popTask() {
        Object output = null;
        while (!THREAD.taskQueue.isEmpty() && output == null) {
            output = THREAD.taskQueue.poll();
        }
        return output;
    }

    public static boolean taskEmpty() {
        return THREAD.taskQueue.isEmpty();
    }
}
