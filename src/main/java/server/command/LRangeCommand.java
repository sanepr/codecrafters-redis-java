package server.command;

import server.data.RedisObject;
import server.data.RedisList;
import server.util.RESPEncoder;

import java.io.OutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class LRangeCommand implements Command {

    private final ConcurrentHashMap<String, RedisObject> store;

    public LRangeCommand(ConcurrentHashMap<String, RedisObject> store) {
        this.store = store;
    }

    @Override
    public void execute(List<String> args, OutputStream outputStream) throws IOException {
        if (args.size() < 3) {
            RESPEncoder.writeError("wrong number of arguments for 'lrange' command", outputStream);
            return;
        }

        String key = args.get(0);
        int start, stop;

        try {
            start = Integer.parseInt(args.get(1));
            stop = Integer.parseInt(args.get(2));
        } catch (NumberFormatException e) {
            RESPEncoder.writeError("start and stop must be integers", outputStream);
            return;
        }

        RedisObject obj = store.get(key);
        if (obj == null || obj.isExpired()) {
            if (obj != null) store.remove(key);
            RESPEncoder.writeArray(new String[0], outputStream); // empty array
            return;
        }

        if (!(obj instanceof RedisList list)) {
            RESPEncoder.writeError("wrong type for key", outputStream);
            return;
        }

        List<String> values = list.getValues();
        int size = values.size();

        // Convert negative indexes to positive
        if (start < 0) start = size + start;
        if (stop < 0) stop = size + stop;

        // Clamp to valid range
        start = Math.max(0, start);
        stop = Math.min(stop, size - 1);

        if (start > stop || start >= size) {
            RESPEncoder.writeArray(new String[0], outputStream); // empty array
            return;
        }

        List<String> subList = values.subList(start, stop + 1);
        RESPEncoder.writeArray(subList.toArray(new String[0]), outputStream);
    }
}
