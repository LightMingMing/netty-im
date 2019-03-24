package nio.samples;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Arrays;

public class ScatterGatherServerSocket {
    public static void main(String[] args) throws IOException {
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.bind(new InetSocketAddress(8080));
        SocketChannel socketChannel = serverSocketChannel.accept();

        ByteBuffer[] buffer = new ByteBuffer[3];
        buffer[0] = ByteBuffer.allocateDirect(3);
        buffer[1] = ByteBuffer.allocateDirect(4);
        buffer[2] = ByteBuffer.allocateDirect(5);
        final int messageLength = 12;
        try {
            while (true) {
                int readBytes = 0;

                while (readBytes < messageLength) {
                    long length = socketChannel.read(buffer);
                    if (length <= 0) return;
                    readBytes += length;

                    Arrays.stream(buffer).map(b -> "position: " + b.position() + ", limit: " + b.limit())
                            .forEach(System.out::println);
                    System.out.println("readBytes: " + readBytes);
                }
                Arrays.stream(buffer).forEach(ByteBuffer::flip);

                socketChannel.write(buffer);
                Arrays.stream(buffer).forEach(ByteBuffer::clear);
            }
        } finally {
            socketChannel.close();
            serverSocketChannel.close();
        }

    }
}
