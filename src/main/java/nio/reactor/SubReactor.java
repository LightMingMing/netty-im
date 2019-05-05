package nio.reactor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author LightMingMing
 * @see <a href="http://gee.cs.oswego.edu/dl/cpjslides/nio.pdf">Scalable IO in Java</a>
 */
public class SubReactor implements Runnable {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final Selector selector;
    private AtomicBoolean started = new AtomicBoolean(false);

    public SubReactor() throws IOException {
        this.selector = Selector.open();
    }

    public Selector getSelector() {
        return this.selector;
    }

    public boolean isStarted() {
        return started.get();
    }

    @Override
    public void run() {
        if (!started.compareAndSet(false, true)) return;
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

    public void dispatch(SelectionKey selectionKey) {
        Runnable runnable = (Runnable) selectionKey.attachment();
        if (runnable != null) {
            runnable.run();
        }
    }
}

