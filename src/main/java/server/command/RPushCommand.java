package server.command;

import server.data.RedisObject;
import server.data.RedisList;

import java.io.OutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
            outputStream.write("-ERR wrong number of arguments for 'rpush' command\r\n".getBytes(StandardCharsets.UTF_8));
            return;
        }

        String key = args.get(0);
        String element = args.get(1);

        RedisObject obj = store.get(key);

        if (obj == null || obj.isExpired()) {
            RedisList list = new RedisList(0);
            int size = list.append(element);
            store.put(key, list);
            outputStream.write((":" + size + "\r\n").getBytes(StandardCharsets.UTF_8));
        } else if (obj instanceof RedisList) {
            int size = ((RedisList) obj).append(element);
            outputStream.write((":" + size + "\r\n").getBytes(StandardCharsets.UTF_8));
        } else {
            outputStream.write("-ERR wrong type for key\r\n".getBytes(StandardCharsets.UTF_8));
        }
    }
}
