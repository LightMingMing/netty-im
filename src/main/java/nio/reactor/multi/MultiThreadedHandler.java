package nio.reactor.multi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * MultiThreadedHandler
 *
 * @author LightMingMing
 */
public class MultiThreadedHandler implements Runnable {

    private static final int READING = 0, WRITING = 1, PROCESSING = 2;
    private static final ExecutorService exec = Executors.newFixedThreadPool(16);

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final SocketChannel socketChannel;
    private final SelectionKey selectionKey;
    private final Selector selector;
    private final ByteBuffer input = ByteBuffer.allocateDirect(1024);
    private final ByteBuffer output = ByteBuffer.allocateDirect(1024);
    private int state = READING;

    public MultiThreadedHandler(Selector selector, SocketChannel socketChannel) throws IOException {
        this.selector = selector;
        this.socketChannel = socketChannel;
        this.socketChannel.configureBlocking(false);
        this.selectionKey = socketChannel.register(selector, SelectionKey.OP_READ, this);
        selector.wakeup();
    }

    @Override
    public void run() {
        try {
            if (state == READING) {
                read();
            } else if (state == WRITING) {
                write();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private synchronized void read() throws IOException {
        int readBytes = socketChannel.read(input);
        logger.info("read {} {} bytes ", socketChannel.getRemoteAddress(), readBytes);
        if (readBytes == -1) {
            this.selectionKey.cancel();
            this.socketChannel.close();
            return;
        }
        if (isInputComplete()) {
            state = PROCESSING;
            exec.submit(this::processAndHandOff);
        }
    }

    private void process() {
        input.flip();
        byte[] inputBytes = new byte[input.limit()];
        input.get(inputBytes);
        input.clear();
        try {
            String receivedMessage = new String(inputBytes, StandardCharsets.UTF_8);
            if (receivedMessage.endsWith("\n"))
                receivedMessage = receivedMessage.substring(0, receivedMessage.length() - 1);
            logger.info("received message '{}' from '{}'", receivedMessage, socketChannel.getRemoteAddress());
        } catch (IOException e) {
            logger.warn(e.getMessage());
        }
        output.put(inputBytes);
        output.flip();
    }

    private synchronized void processAndHandOff() {
        process();
        state = WRITING;
        selectionKey.interestOps(SelectionKey.OP_WRITE);
        selector.wakeup();
    }

    private void write() throws IOException {
        socketChannel.write(output);
        if (isOutputComplete()) {
            output.clear();
            state = READING;
            selectionKey.interestOps(SelectionKey.OP_READ);
        }
    }

    private boolean isInputComplete() {
        return true;
    }

    private boolean isOutputComplete() {
        return true;
    }

}
