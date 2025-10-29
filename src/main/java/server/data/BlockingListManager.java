package server.data;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Manages waiters for blocking list pop operations (BLPOP).
 * Each key maps to a FIFO queue of waiter queues. When an element is pushed
 * to a list, the RPUSH command can poll this manager to find a waiting client
 * and deliver the element.
 */
public final class BlockingListManager {
    private static final ConcurrentHashMap<String, LinkedBlockingQueue<LinkedBlockingQueue<String>>> waiters = new ConcurrentHashMap<>();

    private BlockingListManager() {}

    public static LinkedBlockingQueue<String> registerWaiter(String key) {
        LinkedBlockingQueue<LinkedBlockingQueue<String>> q =
                waiters.computeIfAbsent(key, k -> new LinkedBlockingQueue<>());
        LinkedBlockingQueue<String> waiter = new LinkedBlockingQueue<>(1);
        q.add(waiter);
        return waiter;
    }

    public static LinkedBlockingQueue<String> pollWaiter(String key) {
        LinkedBlockingQueue<LinkedBlockingQueue<String>> q = waiters.get(key);
        if (q == null) return null;
        LinkedBlockingQueue<String> waiter = q.poll();
        if (q.isEmpty()) {
            waiters.remove(key, q);
        }
        return waiter;
    }

    public static void deregisterWaiter(String key, LinkedBlockingQueue<String> waiter) {
        LinkedBlockingQueue<LinkedBlockingQueue<String>> q = waiters.get(key);
        if (q == null) return;
        q.remove(waiter);
        if (q.isEmpty()) {
            waiters.remove(key, q);
        }
    }
}
