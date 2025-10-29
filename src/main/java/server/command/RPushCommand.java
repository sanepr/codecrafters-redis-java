package server.command;

import server.data.RedisObject;
import server.data.RedisList;
import server.util.RESPEncoder;

import java.io.OutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

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

        String key = args.getFirst();
        List<String> elementsToAdd = args.subList(1, args.size());

        RedisObject obj = store.get(key);
        RedisList list;

        if (obj == null || obj.isExpired()) {
            list = new RedisList(0);
            store.put(key, list);
        } else if (obj instanceof RedisList) {
            list = (RedisList) obj;
        } else {
            RESPEncoder.writeError("wrong type for key", outputStream);
            return;
        }

        elementsToAdd.forEach(list::append);

        RESPEncoder.writeInteger(list.getValues().size(), outputStream);
    }
}
