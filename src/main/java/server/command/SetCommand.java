package server.command;

import java.io.OutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class SetCommand implements Command {

    private final ConcurrentHashMap<String, ValueWithExpiry> store;

    public SetCommand(ConcurrentHashMap<String, ValueWithExpiry> store) {
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
        long expiryMillis = 0; // default: no expiry

        // Parse optional arguments (EX seconds, PX milliseconds)
        if (args.size() >= 4) {
            String option = args.get(2).toUpperCase();
            String optionValue = args.get(3);

            try {
                long time = Long.parseLong(optionValue);
                if (option.equals("EX")) {
                    expiryMillis = System.currentTimeMillis() + time * 1000L;
                } else if (option.equals("PX")) {
                    expiryMillis = System.currentTimeMillis() + time;
                }
            } catch (NumberFormatException e) {
                outputStream.write("-ERR invalid expire time\r\n".getBytes(StandardCharsets.UTF_8));
                return;
            }
        }

        store.put(key, new ValueWithExpiry(value, expiryMillis));
        outputStream.write("+OK\r\n".getBytes(StandardCharsets.UTF_8));
    }
}
