package nio.reactor.single;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

/**
 * SocketChannelHandler
 *
 * @author LightMingMing
 * @see <a href="http://gee.cs.oswego.edu/dl/cpjslides/nio.pdf">Scalable IO in Java</a>
 */
public class SocketChannelHandler implements Runnable {

    private static final int READING = 0, WRITING = 1;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final SocketChannel socketChannel;
    private final SelectionKey selectionKey;
    private final ByteBuffer data = ByteBuffer.allocateDirect(1024);
    private int state = READING;


    public SocketChannelHandler(Selector selector, SocketChannel socketChannel) throws IOException {
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

    private void read() throws IOException {
        int readBytes = socketChannel.read(data);
        logger.info("read {} {} bytes ", socketChannel.getRemoteAddress(), readBytes);
        if (readBytes == -1) {
            selectionKey.cancel();
            socketChannel.close();
            return;
        }
        data.flip();
        state = WRITING;
        this.selectionKey.interestOps(SelectionKey.OP_WRITE);

    }

    private void write() throws IOException {
        int writeBytes = socketChannel.write(data);
        data.clear();
        state = READING;
        this.selectionKey.interestOps(SelectionKey.OP_READ);
    }
}
