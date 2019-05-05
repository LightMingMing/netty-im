package nio.reactor;

import nio.reactor.single.SocketChannelHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author LightMingMing
 * @see <a href="http://gee.cs.oswego.edu/dl/cpjslides/nio.pdf">Scalable IO in Java</a>
 */
public class MainReactor implements Runnable {

    protected final static int N_THREADS = Runtime.getRuntime().availableProcessors() * 2;
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final Selector selector;
    private final ServerSocketChannel serverSocketChannel;
    private final SubReactor[] subReactors;
    private final ThreadPoolExecutor executor = new ThreadPoolExecutor(N_THREADS, N_THREADS, 60, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(), new CustomizeThreadFactory("reactor"));
    private AtomicInteger next = new AtomicInteger();

    public MainReactor(int port) throws IOException {
        selector = Selector.open();
        subReactors = new SubReactor[N_THREADS];
        for (int i = 0; i < N_THREADS; i++) {
            subReactors[i] = new SubReactor();
        }
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.bind(new InetSocketAddress(port));
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT, new Acceptor());
    }

    public static void main(String[] args) throws IOException {
        new MainReactor(8080).run();
    }

    @Override
    public void run() {
        try {
            logger.info("start...");
            // Dispatch Loop
            while (!Thread.interrupted()) {
                // selector.select(this::dispatch, 100);
                logger.info("keys number:" + selector.select(this::dispatch));
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    private void dispatch(SelectionKey selectionKey) {
        Runnable runnable = (Runnable) selectionKey.attachment();
        if (runnable != null) {
            runnable.run();
        }
    }

    private class Acceptor implements Runnable {

        @Override
        public void run() {
            try {
                SocketChannel socketChannel = serverSocketChannel.accept();
                if (socketChannel != null) {
                    logger.info("accept {}", socketChannel.getRemoteAddress());
                    SubReactor subReactor = subReactors[next.getAndIncrement() % N_THREADS];
                    if (!subReactor.isStarted())
                        executor.execute(subReactor);
                    new SocketChannelHandler(subReactor.getSelector(), socketChannel);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
