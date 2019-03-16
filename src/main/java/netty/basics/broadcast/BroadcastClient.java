package netty.basics.broadcast;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Scanner;

public class BroadcastClient {
    public static void main(String[] args) throws InterruptedException {
        EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
        try (Scanner in = new Scanner(System.in)) {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(eventLoopGroup).channel(NioSocketChannel.class).handler(new ChannelInitializer<NioSocketChannel>() {
                @Override
                protected void initChannel(NioSocketChannel ch) throws Exception {
                    PipelineDecorator.decorate(ch.pipeline()).addLast(new BroadcastHandler());
                }
            });
            Channel channel = bootstrap.connect("localhost", 8080).sync().channel();
            while (true) {
                channel.writeAndFlush(in.nextLine() + "\n");
            }

        } finally {
            eventLoopGroup.shutdownGracefully();
        }
    }

    static class BroadcastHandler extends SimpleChannelInboundHandler<String> {

        protected Logger log = LoggerFactory.getLogger(this.getClass());

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
            log.info("Received message : " + msg);
        }
    }

}
