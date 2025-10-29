package server.command;

import server.data.RedisList;
import server.data.RedisObject;
import server.util.RESPEncoder;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class LPopCommand implements Command {
    private final ConcurrentHashMap<String, RedisObject> store;

    public LPopCommand(ConcurrentHashMap<String, RedisObject> store) {
        this.store = store;
    }

    @Override
    public void execute(List<String> args, OutputStream outputStream) throws IOException {
        if (args.size() != 1) {
            RESPEncoder.writeError("wrong number of arguments for 'lpop' command", outputStream);
            return;
        }

        String key = args.get(0);
        RedisObject obj = store.get(key);

        // Key does not exist or is expired
        if (obj == null || obj.isExpired()) {
            RESPEncoder.writeNullBulkString(outputStream);
            store.remove(key);
            return;
        }

        // Check type
        if (!(obj instanceof RedisList list)) {
            RESPEncoder.writeError("WRONGTYPE Operation against a key holding the wrong kind of value", outputStream);
            return;
        }

        // If list is empty
        if (list.getValues().isEmpty()) {
            RESPEncoder.writeNullBulkString(outputStream);
            return;
        }

        // Remove first element
        String value = list.getValues().removeFirst();

        // Remove key if list becomes empty
        if (list.getValues().isEmpty()) {
            store.remove(key);
        }

        RESPEncoder.writeBulkString(value, outputStream);
    }
}
