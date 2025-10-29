package server.command;

import server.data.BlockingListManager;
import server.data.RedisObject;
import server.data.RedisList;
import server.util.RESPEncoder;

import java.io.OutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class RPushCommand implements Command {

    private final ConcurrentHashMap<String, RedisObject> store;

    public RPushCommand(ConcurrentHashMap<String, RedisObject> store) {
        this.store = store;
    }

    @Override
    public void execute(List<String> args, OutputStream outputStream) throws IOException {
        if (args.size() < 2) {
            RESPEncoder.writeError("wrong number of arguments for 'rpush' command", outputStream);
            return;
        }

        String key = args.get(0);
        List<String> elementsToAdd = args.subList(1, args.size());

        RedisObject obj = store.get(key);
        RedisList list = null;

        if (obj != null && !obj.isExpired()) {
            if (obj instanceof RedisList) {
                list = (RedisList) obj;
            } else {
                RESPEncoder.writeError("wrong type for key", outputStream);
                return;
            }
        }

        // For each element, deliver to any waiting BLPOP client if present; otherwise append to list
        int appendedCount = 0;
        for (String el : elementsToAdd) {
            LinkedBlockingQueue<String> waiter = BlockingListManager.pollWaiter(key);
            if (waiter != null) {
                try {
                    waiter.put(el);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    // If interrupted, fall back to appending to list
                    if (list == null) {
                        list = new RedisList(0);
                        store.put(key, list);
                    }
                    list.append(el);
                    appendedCount++;
                }
            } else {
                if (list == null) {
                    list = new RedisList(0);
                    store.put(key, list);
                }
                list.append(el);
                appendedCount++;
            }
        }

        // Respond with the new length of the list (as Redis does, includes appended elements only)
        RESPEncoder.writeInteger(list != null ? list.getValues().size() : 0, outputStream);
    }
}
