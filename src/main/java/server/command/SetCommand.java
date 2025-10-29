package server.command;

import java.io.OutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class SetCommand implements Command {

    private final ConcurrentHashMap<String, String> store;

    public SetCommand(ConcurrentHashMap<String, String> store) {
        this.store = store;
    }

    @Override
    public void execute(List<String> args, OutputStream outputStream) throws IOException {
        if (args.size() < 2) {
            outputStream.write("-ERR wrong number of arguments for 'set' command\r\n".getBytes(StandardCharsets.UTF_8));
            return;
        }

        String key = args.get(0);
        String value = args.get(1);
        store.put(key, value);

        outputStream.write("+OK\r\n".getBytes(StandardCharsets.UTF_8));
    }
}
