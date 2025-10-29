package server.command;

import org.junit.jupiter.api.Test;
import server.data.RedisObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;

public class BlPopCommandTest {

    @Test
    public void testBlPopTimeoutReturnsNullArray() throws Exception {
        ConcurrentHashMap<String, RedisObject> store = new ConcurrentHashMap<>();
        BlPopCommand blpop = new BlPopCommand(store);

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        Thread t = new Thread(() -> {
            try {
                blpop.execute(Arrays.asList("timeout_list", "0.1"), out);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        t.start();
        // wait for the blpop thread to finish (it should time out and exit)
        t.join(2000);

        assertFalse(t.isAlive(), "BLPOP thread should have finished after timeout");
        assertEquals("*-1\r\n", out.toString("UTF-8"));
    }

    @Test
    public void testBlPopUnblocksWhenRPushOccursBeforeTimeout() throws Exception {
        ConcurrentHashMap<String, RedisObject> store = new ConcurrentHashMap<>();
        BlPopCommand blpop = new BlPopCommand(store);
        RPushCommand rpush = new RPushCommand(store);

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        Thread t = new Thread(() -> {
            try {
                blpop.execute(Arrays.asList("mylist2", "0.5"), out);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        t.start();
        // give BLPOP a short moment to register as a waiter
        Thread.sleep(50);

        // Push an element which should immediately unblock the waiting BLPOP
        ByteArrayOutputStream out2 = new ByteArrayOutputStream();
        rpush.execute(Arrays.asList("mylist2", "foo"), out2);

        // Wait for the BLPOP thread to finish (it should be unblocked quickly)
        t.join(2000);

        assertFalse(t.isAlive(), "BLPOP thread should have finished after RPUSH delivered an element");

        String expected = "*2\r\n$7\r\nmylist2\r\n$3\r\nfoo\r\n";
        assertEquals(expected, out.toString("UTF-8"));
    }
}

