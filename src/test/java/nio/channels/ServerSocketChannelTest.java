package nio.channels;

import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.ServerSocketChannel;
import java.util.concurrent.*;

import static org.junit.Assert.*;

/**
 * Test cases for ServerSocketChannel
 *
 * @author LightMingMing
 * @see ServerSocketChannel
 */
public class ServerSocketChannelTest {

    @Test
    public void testAcceptWithUnblocking() throws IOException {
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.bind(new InetSocketAddress(8080));
        serverSocketChannel.configureBlocking(false);
        assertNull(serverSocketChannel.accept());
        serverSocketChannel.close();
    }

    @Test
    public void testAcceptWithBlocking() throws IOException, InterruptedException, ExecutionException, TimeoutException {
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.bind(new InetSocketAddress(8080));
        serverSocketChannel.configureBlocking(true);
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        Callable<Exception> task = () -> {
            try {
                serverSocketChannel.accept();
            } catch (IOException e) {
                return e;
            }
            return null;
        };
        Future<Exception> result = executorService.submit(task);
        Exception e = null;
        try {
            result.get(3, TimeUnit.SECONDS);
            fail();
        } catch (TimeoutException excepted) {
            e = excepted;
        }
        assertNotNull(e);
        serverSocketChannel.close();
        assertTrue(result.get(3, TimeUnit.SECONDS) instanceof AsynchronousCloseException);
    }
}
