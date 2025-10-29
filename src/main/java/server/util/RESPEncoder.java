package server.util;

import java.io.OutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public final class RESPEncoder {

    private static final byte[] NULL_BULK_STRING = "$-1\r\n".getBytes(StandardCharsets.UTF_8);

    private RESPEncoder() {}

    public static void writeSimpleString(String value, OutputStream out) throws IOException {
        out.write(("+" + value + "\r\n").getBytes(StandardCharsets.UTF_8));
    }

    public static void writeError(String message, OutputStream out) throws IOException {
        out.write(("-ERR " + message + "\r\n").getBytes(StandardCharsets.UTF_8));
    }

    public static void writeBulkString(String value, OutputStream out) throws IOException {
        if (value == null) {
            writeNullBulkString(out);
        } else {
            out.write(("$" + value.length() + "\r\n" + value + "\r\n").getBytes(StandardCharsets.UTF_8));
        }
    }

    public static void writeNullBulkString(OutputStream out) throws IOException {
        out.write(NULL_BULK_STRING);
    }

    public static void writeInteger(long value, OutputStream out) throws IOException {
        out.write((":" + value + "\r\n").getBytes(StandardCharsets.UTF_8));
    }

    public static void writeArray(String[] elements, OutputStream out) throws IOException {
        out.write(("*" + elements.length + "\r\n").getBytes(StandardCharsets.UTF_8));
        for (String el : elements) {
            writeBulkString(el, out);
        }
    }
}
