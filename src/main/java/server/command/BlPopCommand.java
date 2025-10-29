package server.command;

import server.data.BlockingListManager;
import server.data.RedisList;
import server.data.RedisObject;
import server.util.RESPEncoder;

import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Blocking LPOP: blocks until an element becomes available and returns [key, element]
 * For this stage, only timeout=0 (block indefinitely) is supported.
 */
public class BlPopCommand implements Command {
    private final ConcurrentHashMap<String, RedisObject> store;

    public BlPopCommand(ConcurrentHashMap<String, RedisObject> store) {
        this.store = store;
    }

    @Override
    public void execute(List<String> args, OutputStream outputStream) throws IOException {
        if (args.size() < 2) {
            RESPEncoder.writeError("wrong number of arguments for 'blpop' command", outputStream);
            return;
        }

        String key = args.get(0);
        String timeout = args.get(args.size() - 1); // last argument is timeout in seconds

        // Only support timeout == "0" for indefinite blocking in this stage
        if (!"0".equals(timeout)) {
            RESPEncoder.writeError("this implementation only supports timeout 0 for BLPOP", outputStream);
            return;
        }

        // Register as a waiter first to avoid races with RPUSH
        LinkedBlockingQueue<String> waiter = BlockingListManager.registerWaiter(key);

        // After registration, re-check the list under lock. If an element is present,
        // consume it immediately and deregister the waiter so RPUSH won't deliver it.
        RedisObject obj = store.get(key);
        if (obj != null && !obj.isExpired() && obj instanceof RedisList list) {
            LinkedList<String> values = list.getValues();
            synchronized (values) {
                if (!values.isEmpty()) {
                    String val = values.removeFirst();
                    if (values.isEmpty()) store.remove(key);
                    // Remove this waiter since we consumed the element ourselves
                    BlockingListManager.deregisterWaiter(key, waiter);
                    RESPEncoder.writeArray(new String[]{key, val}, outputStream);
                    return;
                }
            }
        }

        // Otherwise block indefinitely until RPUSH delivers an element into our waiter
        String value;
        try {
            value = waiter.take();
        } catch (InterruptedException e) {
            // If interrupted, deregister and return null
            BlockingListManager.deregisterWaiter(key, waiter);
            Thread.currentThread().interrupt();
            RESPEncoder.writeNullBulkString(outputStream);
            return;
        }

        RESPEncoder.writeArray(new String[]{key, value}, outputStream);
    }
}
