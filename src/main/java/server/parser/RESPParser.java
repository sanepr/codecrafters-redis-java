package server.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class RESPParser {

    // Private constructor — prevent instantiation
    private RESPParser() {}

    /**
     * Parses a RESP Array (like *2\r\n$4\r\nECHO\r\n$3\r\nhey\r\n)
     * into a list of strings (e.g., ["ECHO", "hey"]).
     *
     * @param reader BufferedReader connected to client input stream.
     * @return List of command parts, or null if invalid/empty.
     * @throws IOException if network input fails.
     */
    public static List<String> parseRESPArray(BufferedReader reader) throws IOException {
        String line = reader.readLine();
        if (line == null || line.isEmpty()) {
            return null;
        }

        if (line.charAt(0) != '*') {
            return null; // not a valid RESP array
        }

        int numElements;
        try {
            numElements = Integer.parseInt(line.substring(1));
        } catch (NumberFormatException e) {
            return null;
        }

        List<String> parts = new ArrayList<>();

        for (int i = 0; i < numElements; i++) {
            String lengthLine = reader.readLine();
            if (lengthLine == null || !lengthLine.startsWith("$")) {
                return null;
            }

            int length;
            try {
                length = Integer.parseInt(lengthLine.substring(1));
            } catch (NumberFormatException e) {
                return null;
            }

            char[] buf = new char[length];
            int read = reader.read(buf, 0, length);
            if (read != length) {
                return null; // incomplete message
            }

            String data = new String(buf);
            reader.readLine(); // skip trailing CRLF
            parts.add(data);
        }

        return parts;
    }
}
