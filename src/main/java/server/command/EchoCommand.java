package server.command;

import server.util.RESPEncoder;

import java.io.OutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class EchoCommand implements Command {
    @Override
    public void execute(List<String> args, OutputStream outputStream) throws IOException {
        if (args.isEmpty()) {
            RESPEncoder.writeError("wrong number of arguments for 'echo' command", outputStream);
        } else {
            RESPEncoder.writeBulkString(args.getFirst(), outputStream);
        }
    }
}
