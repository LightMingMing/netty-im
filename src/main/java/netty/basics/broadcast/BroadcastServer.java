package netty.basics.broadcast;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;

public class BroadcastServer {

    public static void main(String[] args) throws InterruptedException {

        EventLoopGroup boss = new NioEventLoopGroup();
        EventLoopGroup worker = new NioEventLoopGroup();

        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(boss, worker).channel(NioServerSocketChannel.class)
                    .handler(new ChannelInboundHandlerAdapter())
                    .childHandler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel ch) {
                            PipelineDecorator.decorate(ch.pipeline()).addLast(new BroadcastHandler());
                        }
                    });
            ChannelFuture channelFuture = serverBootstrap.bind(8080).sync();
            channelFuture.channel().closeFuture().sync();
        } finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }

    static class BroadcastHandler extends SimpleChannelInboundHandler<String> {

        // must static
        private static ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

        protected Logger log = LoggerFactory.getLogger(this.getClass());

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
            log.info("Received message : \"" + msg + "\" from " + ctx.channel().remoteAddress());
            channelGroup.stream().filter(c -> !c.equals(ctx.channel()))
                    .forEach(c -> c.writeAndFlush("\"" + msg + "\" from " + ctx.channel().remoteAddress() + "\n"));
        }

        @Override
        public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
            log.info("添加通道处理器 @" + this.hashCode() + "");
            log.info("通道 " + ctx.channel());
            Channel channel = ctx.channel();
            channelGroup.forEach(c -> c.writeAndFlush("Welcome" + c.remoteAddress() + " login\n"));
            channelGroup.add(channel);
            super.handlerAdded(ctx);
        }

        @Override
        public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
            log.info("删除通道处理器 @" + this.hashCode());
            log.info("通道 " + ctx.channel());
            SocketAddress remoteAddress = ctx.channel().remoteAddress();
            channelGroup.forEach(c -> c.writeAndFlush("Goodbye " + remoteAddress + "\n"));
            super.handlerRemoved(ctx);
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            log.info("通道打开");
            super.channelActive(ctx);
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            log.info("通道关闭");
            super.channelInactive(ctx);
        }

    }
}
