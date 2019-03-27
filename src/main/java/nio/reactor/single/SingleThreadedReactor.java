package nio.reactor.single;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * Single threaded reactor
 *
 * @author LightMingMing
 * @see <a href="http://gee.cs.oswego.edu/dl/cpjslides/nio.pdf">Scalable IO in Java</a>
 */
public class SingleThreadedReactor implements Runnable {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final Selector selector;
    private final ServerSocketChannel serverSocketChannel;

    public SingleThreadedReactor(int port) throws IOException {
        selector = Selector.open();
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.bind(new InetSocketAddress(port));
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT, new Acceptor());
    }

    public static void main(String[] args) throws IOException {
        new SingleThreadedReactor(8080).run();
    }

    @Override
    public void run() {
        try {
            logger.info("start...");
            // Dispatch Loop
            while (!Thread.interrupted()) {
                logger.debug("keys number: " + selector.select(this::dispatch));
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    private void dispatch(SelectionKey selectionKey) {
        Runnable runnable = (Runnable) selectionKey.attachment();
        if (runnable != null)
            runnable.run();
    }

    class Acceptor implements Runnable {

        @Override
        public void run() {
            try {
                SocketChannel socketChannel = serverSocketChannel.accept();
                if (socketChannel != null) {
                    logger.info("accept {}", socketChannel.getRemoteAddress());
                    new SocketChannelHandler(selector, socketChannel);
                }
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

}
