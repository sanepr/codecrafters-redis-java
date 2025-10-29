package server;


import server.parser.RESPParser;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class ClientHandler implements Runnable {
    private final Socket clientSocket;

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
    }

    @Override
    public void run() {
        try (
                InputStream inputStream = clientSocket.getInputStream();
                OutputStream outputStream = clientSocket.getOutputStream();
        ) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));

            while (true) {
                List<String> commandParts = RESPParser.parseRESPArray(reader);
                if (commandParts == null || commandParts.isEmpty()) {
                    break;
                }

                String command = commandParts.get(0).toUpperCase();

                switch (command) {
                    case "PING" -> {
                        outputStream.write("+PONG\r\n".getBytes(StandardCharsets.UTF_8));
                    }
                    case "ECHO" -> {
                        if (commandParts.size() >= 2) {
                            StringBuilder responseBuilder = new StringBuilder();
                            responseBuilder.append("$");
                            responseBuilder.append(commandParts.get(1).length());
                            responseBuilder.append("\r\n");
                            responseBuilder.append(commandParts.get(1));
                            responseBuilder.append("\r\n");

                            outputStream.write(responseBuilder.toString().getBytes(StandardCharsets.UTF_8));
                        } else {
                            outputStream.write("-ERR wrong number of arguments for 'echo' command\r\n".getBytes(StandardCharsets.UTF_8));
                        }
                    }
                    default -> {
                        outputStream.write(("-ERR unknown command '" + command + "'\r\n").getBytes(StandardCharsets.UTF_8));
                    }
                }

                outputStream.flush();
            }

        } catch (IOException e) {
            System.out.println("Client disconnected: " + e.getMessage());
        }
    }
}
