package server.command;

import java.io.OutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class GetCommand implements Command {

    private final ConcurrentHashMap<String, String> store;

    public GetCommand(ConcurrentHashMap<String, String> store) {
        this.store = store;
    }

    @Override
    public void execute(List<String> args, OutputStream outputStream) throws IOException {
        if (args.isEmpty()) {
            outputStream.write("-ERR wrong number of arguments for 'get' command\r\n".getBytes(StandardCharsets.UTF_8));
            return;
        }

        String key = args.get(0);
        String value = store.get(key);

        if (value != null) {
            String response = "$" + value.length() + "\r\n" + value + "\r\n";
            outputStream.write(response.getBytes(StandardCharsets.UTF_8));
        } else {
            outputStream.write("$-1\r\n".getBytes(StandardCharsets.UTF_8));
        }
    }
}
