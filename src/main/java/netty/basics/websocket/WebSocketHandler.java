package netty.basics.websocket;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebSocketHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) {
        log.info("Received message : \"" + msg.text() + "\" from " + ctx.channel().remoteAddress());
        ctx.writeAndFlush(new TextWebSocketFrame("Hello"));
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        log.info("通道\'{}\'注册", ctx.channel().id().asLongText());
        super.channelRegistered(ctx);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("通道\'{}\'打开", ctx.channel().id().asLongText());
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("通道\'{}\'关闭", ctx.channel().id().asLongText());
        super.channelInactive(ctx);
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        log.info("通道\'{}\'注册取消", ctx.channel().id().asLongText());
        super.channelUnregistered(ctx);
    }
}
