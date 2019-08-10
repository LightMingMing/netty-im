package netty.basics.cs;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import netty.basics.http.SimpleChannelInboundHandlerLifeCycle;

public class EchoServerBootstrap {

    public static void main(String[] args) throws InterruptedException {

        EventLoopGroup boss = new NioEventLoopGroup(1);
        EventLoopGroup worker = new NioEventLoopGroup();

        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(boss, worker).channel(NioServerSocketChannel.class).handler(new ChannelInboundHandlerAdapter()).childHandler(new ChannelInitializer<NioSocketChannel>() {
                @Override
                protected void initChannel(NioSocketChannel ch) throws Exception {
                    ChannelPipeline cp = ch.pipeline();
                    PipelineDecorator.decorate(cp).addLast(new EchoClientHandler());
                }
            });
            ChannelFuture channelFuture = serverBootstrap.bind(8080).sync();
            channelFuture.channel().closeFuture().sync();
        } finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }

    static class EchoClientHandler extends SimpleChannelInboundHandlerLifeCycle<String> {

        @Override
        protected void messageReceived(ChannelHandlerContext ctx, String msg) throws Exception {
            log.info(msg);
            ctx.channel().writeAndFlush("HELLO, I'M SERVER.");
        }
    }
}
