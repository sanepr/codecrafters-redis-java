package server.command;

import server.data.RedisList;
import server.data.RedisObject;
import server.util.RESPEncoder;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class LLenCommand implements Command {
    private final ConcurrentHashMap<String, RedisObject> store;

    public LLenCommand(ConcurrentHashMap<String, RedisObject> store) {
        this.store = store;
    }

    @Override
    public void execute(List<String> args, OutputStream outputStream) throws IOException {
        if (args.size() != 1) {
            RESPEncoder.writeError("wrong number of arguments for 'llen' command", outputStream);
            return;
        }

        String key = args.getFirst();
        RedisObject obj = store.get(key);

        if (obj == null || obj.isExpired()) {
            RESPEncoder.writeInteger(0, outputStream);
            return;
        }

        if (obj instanceof RedisList list) {
            RESPEncoder.writeInteger(list.getValues().size(), outputStream);
        } else {
            RESPEncoder.writeError("WRONGTYPE Operation against a key holding the wrong kind of value", outputStream);
        }
    }
}
