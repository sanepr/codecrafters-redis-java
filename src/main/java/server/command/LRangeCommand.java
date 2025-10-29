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
            if (start < 0 || stop < 0) {
                RESPEncoder.writeError("negative indexes are not supported yet", outputStream);
                return;
            }
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

        if (!(obj instanceof RedisList)) {
            RESPEncoder.writeError("wrong type for key", outputStream);
            return;
        }

        RedisList list = (RedisList) obj;
        List<String> values = list.getValues();

        if (start >= values.size() || start > stop) {
            RESPEncoder.writeArray(new String[0], outputStream); // empty array
            return;
        }

        // Adjust stop if it exceeds list size
        stop = Math.min(stop, values.size() - 1);

        List<String> subList = values.subList(start, stop + 1);
        RESPEncoder.writeArray(subList.toArray(new String[0]), outputStream);
    }
}
