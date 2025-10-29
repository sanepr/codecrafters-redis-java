package server.command;

import server.util.RESPEncoder;

import java.io.OutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class PingCommand implements Command {
    @Override
    public void execute(List<String> args, OutputStream outputStream) throws IOException {
//        outputStream.write("+PONG\r\n".getBytes(StandardCharsets.UTF_8));
        RESPEncoder.writeSimpleString("PONG", outputStream);
    }
}
