package netty.basics.http;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class SimpleChannelInboundHandlerLifeCycle<I> extends SimpleChannelInboundHandler<I> {

    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        log.info("channelHandler 添加通道处理器 handlerAdded(ctx)");
        super.handlerAdded(ctx);
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        log.info("chanel 通道注册[-> EventLoopGroup]");
        super.channelRegistered(ctx);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("channel 通道连接 channelActive(ctx)");
        super.channelActive(ctx);
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
    protected void channelRead0(ChannelHandlerContext ctx, I msg) throws Exception {
        log.info("channel 通道读取 channelRead0(ctx, msg)");
        messageReceived(ctx, msg);
    }

    protected abstract void messageReceived(ChannelHandlerContext ctx, I msg) throws Exception;

}
