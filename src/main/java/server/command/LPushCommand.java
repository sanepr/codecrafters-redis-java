package server.command;

import server.data.RedisObject;
import server.data.RedisList;
import server.util.RESPEncoder;

import java.io.OutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class LPushCommand implements Command {

    private final ConcurrentHashMap<String, RedisObject> store;

    public LPushCommand(ConcurrentHashMap<String, RedisObject> store) {
        this.store = store;
    }

    @Override
    public void execute(List<String> args, OutputStream outputStream) throws IOException {
        if (args.size() < 2) {
            RESPEncoder.writeError("wrong number of arguments for 'lpush' command", outputStream);
            return;
        }

        String key = args.get(0);
        List<String> elementsToAdd = args.subList(1, args.size());

        RedisObject obj = store.get(key);
        RedisList list;

        if (obj == null || obj.isExpired()) {
            // Create new list if key doesn't exist or is expired
            list = new RedisList(0);
            store.put(key, list);
        } else if (obj instanceof RedisList) {
            list = (RedisList) obj;
        } else {
            RESPEncoder.writeError("wrong type for key", outputStream);
            return;
        }

        // Prepend elements in the correct order
        for (int i = elementsToAdd.size() - 1; i >= 0; i--) {
            list.getValues().add(0, elementsToAdd.get(i));
        }

        RESPEncoder.writeInteger(list.getValues().size(), outputStream);
    }
}
