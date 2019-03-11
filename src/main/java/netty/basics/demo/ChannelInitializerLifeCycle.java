package netty.basics.demo;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ChannelInitializerLifeCycle<C extends Channel> extends ChannelInitializer<C> {

    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        log.info("channelHandler 添加通道处理器 handlerAdded(ctx) start");
        super.handlerAdded(ctx);
        log.info("channelHandler 添加通道处理器 handlerAdded(ctx) end");
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("channel 通道打开 channelActive(ctx) start");
        super.channelActive(ctx);
        log.info("channel 通道打开 channelActive(ctx) end");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        log.info("channel 通道读取数据 channelRead(ctx, msg) start");
        super.channelRead(ctx, msg);
        log.info("channel 通道读取数据 channelRead(ctx, msg) end");
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        log.info("channel 通道读取完成 channelReadComplete(ctx) start");
        super.channelReadComplete(ctx);
        log.info("channel 通道读取完成 channelReadComplete(ctx) end");
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        log.info("channel 通道可写性改变 channelWritabilityChanged(ctx) start");
        super.channelWritabilityChanged(ctx);
        log.info("channel 通道可写性改变 channelWritabilityChanged(ctx) end");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("channel 通道关闭 channelInactive(ctx) start");
        super.channelInactive(ctx);
        log.info("channel 通道关闭 channelInactive(ctx) end");
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        log.info("channel 通道注销[<- EventLoopGroup] channelUnregistered(ctx) start");
        super.channelUnregistered(ctx);
        log.info("channel 通道注销[<- EventLoopGroup] channelUnregistered(ctx) end");
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        log.info("channelHandler 删除通道处理器 handlerRemoved(ctx) start");
        super.handlerRemoved(ctx);
        log.info("channelHandler 删除通道处理器 handlerRemoved(ctx) end");
    }

    @Override
    protected void initChannel(C channel) throws Exception {
        log.info("channel 通道初始化 initChannel(ch) start");
        doInitChannel(channel);
        log.info("channel 通道初始化 initChannel(ch) end");
    }

    protected abstract void doInitChannel(C channel) throws Exception;
}
