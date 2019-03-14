package netty.basics.cs;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import netty.basics.http.SimpleChannelInboundHandlerLifeCycle;

public class EchoClientBootstrap {

    public static void main(String[] args) throws InterruptedException {

        EventLoopGroup worker = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(worker).channel(NioSocketChannel.class).handler(new ChannelInitializer<NioSocketChannel>() {
                @Override
                protected void initChannel(NioSocketChannel ch) throws Exception {
                    ChannelPipeline cp = ch.pipeline();
                    PipelineDecorator.decorate(cp).addLast(new EchoServerHandler());
                }
            });
            ChannelFuture channelFuture = bootstrap.connect("localhost", 8080).sync();
            channelFuture.channel().closeFuture().sync();
        } finally {
            worker.shutdownGracefully();
        }
    }

    static class EchoServerHandler extends SimpleChannelInboundHandlerLifeCycle<String> {

        @Override
        protected void messageReceived(ChannelHandlerContext ctx, String msg) throws Exception {
            log.info(msg);
            ctx.channel().writeAndFlush("HELLO, I'M CLIENT");
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            ctx.channel().writeAndFlush("HELLO");
        }
    }
}
