package cool.muyucloud.saplanting.thread;

import cool.muyucloud.saplanting.Saplanting;

import java.util.LinkedList;

public class ItemEntityThread {
    private static final ItemEntityThread SIGNAL = new ItemEntityThread();

    private boolean isThreadExists;
    private boolean isThreadWaiting;
    private boolean scheduledKill;
    private Thread thread;
    private final LinkedList<Object> taskQueue;

    private ItemEntityThread() {
        this.isThreadExists = false;
        this.isThreadWaiting = false;
        this.scheduledKill = false;
        this.thread = null;

        this.taskQueue = new LinkedList<>();
    }

    /* Thread Signature Accessors */
    public static boolean isThreadExists() {
        return SIGNAL.isThreadExists;
    }

    public static boolean isThreadWaiting() {
        return SIGNAL.isThreadWaiting;
    }

    public static boolean scheduledKill() {
        return SIGNAL.scheduledKill;
    }

    /* Thread Operations */
    public static void initThread(Runnable function) {
        if (SIGNAL.isThreadExists) {
            return;
        }
        SIGNAL.isThreadExists = true;
        SIGNAL.isThreadWaiting = false;
        SIGNAL.scheduledKill = false;
        SIGNAL.thread = new Thread(function);
        SIGNAL.thread.start();
    }

    public static void discardThread() {
        SIGNAL.scheduledKill = true;
        SIGNAL.taskQueue.clear();
    }

    public synchronized static void awaken() {
        SIGNAL.thread.interrupt();
        SIGNAL.isThreadWaiting = false;
    }

    public synchronized static void sleep() throws InterruptedException {
        SIGNAL.isThreadWaiting = true;
        SIGNAL.thread.wait();
    }

    public static void markAsStopped() {
        SIGNAL.isThreadExists = false;
        SIGNAL.isThreadWaiting = false;
    }

    /* Task Queue Operations */
    // please ensure that input is valid!
    public static void addTask(Object item) {
        if (SIGNAL.taskQueue.size() > 1000) {
            Saplanting.getLogger().warn("Too many items! Cleared " + SIGNAL.taskQueue.size() + " tasks.");
            SIGNAL.taskQueue.clear();
        }
        SIGNAL.taskQueue.add(item);
    }

    public static Object popTask() {
        Object output = null;
        while (!SIGNAL.taskQueue.isEmpty() && output == null) {
            output = SIGNAL.taskQueue.poll();
        }
        return output;
    }

    public static boolean taskEmpty() {
        return SIGNAL.taskQueue.isEmpty();
    }
}
