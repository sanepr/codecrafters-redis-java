package server.util;

import java.io.OutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Utility class for encoding Redis responses in RESP protocol.
 */
public final class RESPEncoder {

    private RESPEncoder() {
        // Prevent instantiation
    }

    /**
     * Encodes a simple string (e.g., +OK\r\n)
     */
    public static void writeSimpleString(String value, OutputStream out) throws IOException {
        out.write(("+" + value + "\r\n").getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Encodes an error (e.g., -ERR something\r\n)
     */
    public static void writeError(String message, OutputStream out) throws IOException {
        out.write(("-ERR " + message + "\r\n").getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Encodes a bulk string (e.g., $3\r\nbar\r\n)
     * Null bulk string is represented with value = null
     */
    public static void writeBulkString(String value, OutputStream out) throws IOException {
        if (value == null) {
            out.write("$-1\r\n".getBytes(StandardCharsets.UTF_8));
        } else {
            out.write(("$" + value.length() + "\r\n" + value + "\r\n").getBytes(StandardCharsets.UTF_8));
        }
    }

    /**
     * Encodes an integer (e.g., :5\r\n)
     */
    public static void writeInteger(long value, OutputStream out) throws IOException {
        out.write((":" + value + "\r\n").getBytes(StandardCharsets.UTF_8));
    }
}
