package server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

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
            byte[] buffer = new byte[1024];
            int bytesRead;

            // Loop to handle multiple commands from this client
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                String input = new String(buffer, 0, bytesRead, StandardCharsets.UTF_8);
                System.out.println("Received from client " + clientSocket.getInetAddress() + ": " + input.trim());

                // For now, always respond with PONG
                outputStream.write("+PONG\r\n".getBytes(StandardCharsets.UTF_8));
                outputStream.flush();
            }

        } catch (IOException e) {
            System.out.println("Client " + clientSocket.getInetAddress() + " disconnected: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                System.out.println("Error closing client socket: " + e.getMessage());
            }
        }
    }
}