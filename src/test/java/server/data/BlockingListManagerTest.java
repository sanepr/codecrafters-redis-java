package server.data;

import org.junit.jupiter.api.Test;

import java.util.concurrent.LinkedBlockingQueue;

import static org.junit.jupiter.api.Assertions.*;

public class BlockingListManagerTest {

    @Test
    public void testRegisterAndPoll() {
        LinkedBlockingQueue<String> waiter = BlockingListManager.registerWaiter("k1");
        LinkedBlockingQueue<String> polled = BlockingListManager.pollWaiter("k1");
        assertNotNull(polled);
        assertEquals(waiter, polled);
    }

    @Test
    public void testDeregisterWaiter() {
        LinkedBlockingQueue<String> w1 = BlockingListManager.registerWaiter("k2");
        LinkedBlockingQueue<String> w2 = BlockingListManager.registerWaiter("k2");

        // deregister first waiter
        BlockingListManager.deregisterWaiter("k2", w1);

        LinkedBlockingQueue<String> polled = BlockingListManager.pollWaiter("k2");
        assertEquals(w2, polled);

        // now queue should be empty
        LinkedBlockingQueue<String> polled2 = BlockingListManager.pollWaiter("k2");
        assertNull(polled2);
    }
}

