package server.command;

import server.data.RedisObject;
import server.data.RedisString;
import server.util.RESPEncoder;

import java.io.OutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class GetCommand implements Command {

    private final ConcurrentHashMap<String, RedisObject> store;

    public GetCommand(ConcurrentHashMap<String, RedisObject> store) {
        this.store = store;
    }

    @Override
    public void execute(List<String> args, OutputStream outputStream) throws IOException {
        if (args.isEmpty()) {
            RESPEncoder.writeError("wrong number of arguments for 'get' command", outputStream);
            return;
        }

        String key = args.get(0);
        RedisObject obj = store.get(key);

        if (obj == null || obj.isExpired()) {
            if (obj != null) store.remove(key);
            RESPEncoder.writeBulkString(null, outputStream);
        } else if (obj instanceof RedisString) {
            RESPEncoder.writeBulkString(((RedisString) obj).getValue(), outputStream);
        } else {
            RESPEncoder.writeError("wrong type for key", outputStream);
        }
    }
}
