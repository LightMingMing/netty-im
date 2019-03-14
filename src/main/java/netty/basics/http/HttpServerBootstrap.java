package netty.basics.http;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

public class HttpServerBootstrap {

    public static void main(String[] args) throws InterruptedException {
        NioEventLoopGroup boss = new NioEventLoopGroup();
        NioEventLoopGroup worker = new NioEventLoopGroup();
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(boss, worker).channel(NioServerSocketChannel.class)
                    .handler(new BossChannelHandler())
                    .childHandler(new WorkerChannelInitializer());
            ChannelFuture channelFuture = serverBootstrap.bind(8080).sync();
            channelFuture.channel().closeFuture().sync();
        } finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }

    protected static class BossChannelHandler extends ChannelInboundHandlerAdapterLifeCycle {
    }

    protected static class WorkerChannelInitializer extends ChannelInitializerLifeCycle<NioSocketChannel> {

        @Override
        protected void doInitChannel(NioSocketChannel ch) throws Exception {
            ChannelPipeline pipeline = ch.pipeline();
            pipeline.addLast(new HttpServerCodec());
            pipeline.addLast(new SimpleHttpServerHandler());
        }
    }

    protected static class SimpleHttpServerHandler extends SimpleChannelInboundHandlerLifeCycle<HttpObject> {

        private final Logger log = LoggerFactory.getLogger(this.getClass());

        @Override
        protected void messageReceived(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
            log.info("the message type is '" + msg.getClass() + "'");
            if (msg instanceof HttpRequest) {
                ByteBuf content = ctx.alloc().buffer();
                content.writeBytes("Hello, World.".getBytes(StandardCharsets.UTF_8));

                FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, content);
                response.headers().add(HttpHeaderNames.CONTENT_TYPE, "text/plain");
                response.headers().add(HttpHeaderNames.CONTENT_LENGTH, content.readableBytes());
                ctx.writeAndFlush(response);
            }
        }
    }
}
