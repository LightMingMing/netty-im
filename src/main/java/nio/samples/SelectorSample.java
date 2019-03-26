package nio.samples;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Selector sample: multi channels select
 *
 * @author LightMingMing
 */
public class SelectorSample extends Thread {

    private Selector selector;
    private ServerSocketChannel[] serverSocketChannels;

    private AtomicBoolean shutdown = new AtomicBoolean();

    public SelectorSample(int... ports) {
        try {
            selector = Selector.open();
            serverSocketChannels = new ServerSocketChannel[ports.length];
            for (int i = 0; i < ports.length; i++) {
                serverSocketChannels[i] = ServerSocketChannel.open();
                serverSocketChannels[i].configureBlocking(false);
                serverSocketChannels[i].bind(new InetSocketAddress(ports[i]));
                serverSocketChannels[i].register(selector, SelectionKey.OP_ACCEPT);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println(new Date() + "Starting");
        SelectorSample task = new SelectorSample(8080, 8081, 8082);
        task.start();
        TimeUnit.SECONDS.sleep(60);
        task.cancel();
        task.join(1000);
        System.out.println(new Date() + " End");
    }

    @Override
    public void run() {
        while (!shutdown.get()) {
            try {
                System.out.println("keys number: " + selector.select(this::accept));
            } catch (IOException e) {
                e.printStackTrace();
                shutdown.set(true);
            }
        }
    }

    public void cancel() throws IOException {
        shutdown.set(true);
        for (ServerSocketChannel serverSocketChannel : serverSocketChannels) {
            serverSocketChannel.close();
        }
        this.selector.close();
    }

    private void accept(SelectionKey selectedKey) {
        try {
            if (selectedKey.isAcceptable()) {
                ServerSocketChannel serverSocketChannel = (ServerSocketChannel) selectedKey.channel();
                SocketChannel socketChannel = serverSocketChannel.accept();
                socketChannel.configureBlocking(false);
                socketChannel.register(selector, SelectionKey.OP_READ);
                System.out.println(socketChannel.getLocalAddress() + " " + socketChannel.getRemoteAddress());
            } else if (selectedKey.isReadable()) {
                read(selectedKey);
            } else {
                selectedKey.cancel();
                selectedKey.channel().close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void read(SelectionKey selectedKey) {
        SocketChannel socketChannel = (SocketChannel) selectedKey.channel();
        final int maxReadLength = 1024;
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(maxReadLength);
        try {
            int readBytes = socketChannel.read(byteBuffer);
            System.out.println("read bytes: " + readBytes);
            if (readBytes == -1)
                socketChannel.close();
            byteBuffer.flip();
            socketChannel.write(byteBuffer);
            byteBuffer.clear();
        } catch (IOException e) {
            e.printStackTrace();
            selectedKey.cancel();
        }
    }
}
