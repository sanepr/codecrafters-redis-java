package server.command;

import server.data.RedisObject;
import server.data.RedisString;
import server.util.RESPEncoder;

import java.io.OutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class SetCommand implements Command {

    private final ConcurrentHashMap<String, RedisObject> store;

    public SetCommand(ConcurrentHashMap<String, RedisObject> store) {
        this.store = store;
    }

    @Override
    public void execute(List<String> args, OutputStream outputStream) throws IOException {
        if (args.size() < 2) {
            RESPEncoder.writeError("wrong number of arguments for 'set' command", outputStream);
            return;
        }

        String key = args.get(0);
        String value = args.get(1);
        long expiryMillis = 0;

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
                RESPEncoder.writeError("invalid expire time", outputStream);
                return;
            }
        }

        store.put(key, new RedisString(value, expiryMillis));
        RESPEncoder.writeSimpleString("OK", outputStream);
    }
}
