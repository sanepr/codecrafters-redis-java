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
        if (args.size() < 1) {
            RESPEncoder.writeError("wrong number of arguments for 'lpop' command", outputStream);
            return;
        }

        String key = args.get(0);
        int count = 1; // default: pop one element

        // Optional count argument
        if (args.size() >= 2) {
            try {
                count = Integer.parseInt(args.get(1));
                if (count <= 0) {
                    RESPEncoder.writeNullBulkString(outputStream);
                    return;
                }
            } catch (NumberFormatException e) {
                RESPEncoder.writeError("value is not an integer or out of range", outputStream);
                return;
            }
        }

        RedisObject obj = store.get(key);

        // Key does not exist or is expired
        if (obj == null || obj.isExpired()) {
            RESPEncoder.writeNullBulkString(outputStream);
            store.remove(key);
            return;
        }

        // Type check
        if (!(obj instanceof RedisList list)) {
            RESPEncoder.writeError("WRONGTYPE Operation against a key holding the wrong kind of value", outputStream);
            return;
        }

        List<String> values = list.getValues();

        if (values.isEmpty()) {
            RESPEncoder.writeNullBulkString(outputStream);
            return;
        }

        // Pop elements
        int elementsToPop = Math.min(count, values.size());
        String[] popped = new String[elementsToPop];

        for (int i = 0; i < elementsToPop; i++) {
            popped[i] = values.removeFirst();
        }

        // Clean up if list becomes empty
        if (values.isEmpty()) {
            store.remove(key);
        }

        // RESP response: bulk string for single, array for multiple
        if (count == 1) {
            RESPEncoder.writeBulkString(popped[0], outputStream);
        } else {
            RESPEncoder.writeArray(popped, outputStream);
        }
    }
}
