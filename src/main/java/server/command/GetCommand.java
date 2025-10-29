package server.command;

import server.data.RedisObject;
import server.data.RedisString;

import java.io.OutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
            outputStream.write("-ERR wrong number of arguments for 'get' command\r\n".getBytes(StandardCharsets.UTF_8));
            return;
        }

        String key = args.get(0);
        RedisObject obj = store.get(key);

        if (obj == null || obj.isExpired()) {
            if (obj != null) store.remove(key);
            outputStream.write("$-1\r\n".getBytes(StandardCharsets.UTF_8));
        } else if (obj instanceof RedisString) {
            String value = ((RedisString) obj).getValue();
            String response = "$" + value.length() + "\r\n" + value + "\r\n";
            outputStream.write(response.getBytes(StandardCharsets.UTF_8));
        } else {
            outputStream.write("-ERR wrong type for key\r\n".getBytes(StandardCharsets.UTF_8));
        }
    }
}
