package server.util;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RESPEncoderTest {

    @org.junit.Test
    public void testWriteNullArray() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        RESPEncoder.writeNullArray(out);
        assertEquals("*-1\r\n", out.toString("UTF-8"));
    }

    @Test
    public void testWriteNullBulkString() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        RESPEncoder.writeNullBulkString(out);
        assertEquals("$-1\r\n", out.toString("UTF-8"));
    }

    @Test
    public void testWriteBulkStringAndArray() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        RESPEncoder.writeBulkString("hello", out);
        assertEquals("$5\r\nhello\r\n", out.toString("UTF-8"));

        out.reset();
        RESPEncoder.writeArray(new String[]{"a","bb"}, out);
        String expected = "*2\r\n$1\r\na\r\n$2\r\nbb\r\n";
        assertEquals(expected, out.toString("UTF-8"));
    }
}

