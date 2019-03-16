package netty.basics.heartbeat;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HeartbeatDetectHandler extends ChannelInboundHandlerAdapter {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        log.info("通道注册 " + ctx.channel().remoteAddress());
        super.channelRegistered(ctx);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("通道打开 " + ctx.channel().remoteAddress());
        super.channelActive(ctx);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleState idleState = ((IdleStateEvent) evt).state();
            log.info("检测到通道[" + ctx.channel().remoteAddress() + "] 空闲类型 : " + idleState.name());
            if (idleState.equals(IdleState.ALL_IDLE))
                ctx.channel().close();
        }
    }
}
