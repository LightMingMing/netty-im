package netty.basics.cs;

import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.nio.charset.StandardCharsets;

public class PipelineDecorator {

    public static ChannelPipeline decorate(ChannelPipeline cp) {
        cp.addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4));
        cp.addLast(new LengthFieldPrepender(4));
        cp.addLast(new StringDecoder(StandardCharsets.UTF_8));
        cp.addLast(new StringEncoder(StandardCharsets.UTF_8));
        return cp;
    }
}
