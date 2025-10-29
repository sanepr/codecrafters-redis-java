package server;

import server.parser.RESPParser;
import server.command.*;
import server.data.RedisObject;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClientHandler implements Runnable {

    private final Socket clientSocket;
    private static final ConcurrentHashMap<String, RedisObject> store = new ConcurrentHashMap<>();

    private final Map<String, Command> commands;

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
        this.commands = Map.of(
                "PING", new PingCommand(),
                "ECHO", new EchoCommand(),
                "SET", new SetCommand(store),
                "GET", new GetCommand(store),
                "RPUSH", new RPushCommand(store),
                "LPUSH", new LPushCommand(store),
                "LLEN", new LLenCommand(store),
                "LPOP", new LPopCommand(store),
                "LRANGE", new LRangeCommand(store)
        );

    }

    @Override
    public void run() {
        try (
                InputStream inputStream = clientSocket.getInputStream();
                OutputStream outputStream = clientSocket.getOutputStream()
        ) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));

            while (true) {
                List<String> commandParts = RESPParser.parseRESPArray(reader);
                if (commandParts == null || commandParts.isEmpty()) break;

                String commandName = commandParts.get(0).toUpperCase();
                List<String> args = commandParts.subList(1, commandParts.size());

                Command command = commands.get(commandName);
                if (command != null) {
                    command.execute(args, outputStream);
                } else {
                    outputStream.write(("-ERR unknown command '" + commandName + "'\r\n").getBytes(StandardCharsets.UTF_8));
                }

                outputStream.flush();
            }

        } catch (IOException e) {
            System.out.println("Client disconnected: " + e.getMessage());
        }
    }
}
