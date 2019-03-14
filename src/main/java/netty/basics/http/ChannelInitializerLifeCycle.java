package netty.basics.http;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
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
        log.info("channel 通道连接 channelActive(ctx)");
        super.channelActive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        log.info("channel 通道读取数据 channelRead(ctx, msg)");
        super.channelRead(ctx, msg);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        log.info("channel 通道读取完成 channelReadComplete(ctx)");
        super.channelReadComplete(ctx);
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        log.info("channel 通道可写性改变 channelWritabilityChanged(ctx)");
        super.channelWritabilityChanged(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("channel 通道关闭 channelInactive(ctx)");
        super.channelInactive(ctx);
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        log.info("channel 取消通道注册[<- EventLoopGroup] channelUnregistered(ctx)");
        super.channelUnregistered(ctx);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        log.info("channelHandler 删除通道处理器 handlerRemoved(ctx)");
        super.handlerRemoved(ctx);
    }

    @Override
    protected void initChannel(C channel) throws Exception {
        log.info("channel 通道初始化 initChannel(ch) start");
        doInitChannel(channel);
        log.info("channel 通道初始化 initChannel(ch) end");
    }

    protected abstract void doInitChannel(C channel) throws Exception;
}
