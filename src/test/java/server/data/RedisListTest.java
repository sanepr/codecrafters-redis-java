package server.data;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RedisListTest {

    @Test
    public void testAppendAndRemoveFirst() {
        RedisList list = new RedisList(0);
        list.append("a");
        list.append("b");
        assertEquals(2, list.getValues().size());
        String first = list.getValues().removeFirst();
        assertEquals("a", first);
        assertEquals(1, list.getValues().size());
    }
}

