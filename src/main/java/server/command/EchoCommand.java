package server.command;

import java.io.OutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class EchoCommand implements Command {
    @Override
    public void execute(List<String> args, OutputStream outputStream) throws IOException {
        if (args.isEmpty()) {
            outputStream.write("-ERR wrong number of arguments for 'echo' command\r\n".getBytes(StandardCharsets.UTF_8));
            return;
        }

        String message = args.get(0);
        String response = "$" + message.length() + "\r\n" + message + "\r\n";
        outputStream.write(response.getBytes(StandardCharsets.UTF_8));
    }
}
