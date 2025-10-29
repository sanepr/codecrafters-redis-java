package server.command;

import org.junit.jupiter.api.Test;
import server.data.RedisObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RPushLPopCommandsTest {

    @Test
    public void testRPushThenLPop() throws IOException {
        ConcurrentHashMap<String, RedisObject> store = new ConcurrentHashMap<>();
        RPushCommand rpush = new RPushCommand(store);
        LPopCommand lpop = new LPopCommand(store);

        ByteArrayOutputStream out1 = new ByteArrayOutputStream();
        rpush.execute(Arrays.asList("mylist", "foo"), out1);
        assertEquals(":1\r\n", out1.toString("UTF-8"));

        ByteArrayOutputStream out2 = new ByteArrayOutputStream();
        lpop.execute(Arrays.asList("mylist"), out2);
        assertEquals("$3\r\nfoo\r\n", out2.toString("UTF-8"));

        // subsequent lpop should return null bulk string
        ByteArrayOutputStream out3 = new ByteArrayOutputStream();
        lpop.execute(Arrays.asList("mylist"), out3);
        assertEquals("$-1\r\n", out3.toString("UTF-8"));
    }
}

