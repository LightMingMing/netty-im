package nio.samples;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class SelectorSample {

    private static Selector selector;

    static {
        try {
            selector = Selector.open();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        int[] ports = {8080, 8081, 8082, 8083, 8084};
        for (int port : ports) {
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.bind(new InetSocketAddress(port));
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            System.out.println(port);
        }
        while (true) {
            System.out.println(selector.select(SelectorSample::consume));
        }
    }

    private static void consume(SelectionKey selectedKey) {
        try {
            if (selectedKey.isAcceptable()) {
                ServerSocketChannel serverSocketChannel = (ServerSocketChannel) selectedKey.channel();
                SocketChannel socketChannel = serverSocketChannel.accept();
                socketChannel.configureBlocking(false);
                socketChannel.register(selector, SelectionKey.OP_READ);
                System.out.println(socketChannel.getLocalAddress() + " " + socketChannel.getRemoteAddress());
            } else {
                selectedKey.cancel();
                selectedKey.channel().close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
