package netty.basics.broadcast;

import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.nio.charset.StandardCharsets;

public class PipelineDecorator {

    public static ChannelPipeline decorate(ChannelPipeline pipeline) {
        pipeline.addLast(new DelimiterBasedFrameDecoder(4096, Delimiters.lineDelimiter()));
        pipeline.addLast(new StringDecoder(StandardCharsets.UTF_8));
        pipeline.addLast(new StringEncoder(StandardCharsets.UTF_8));
        return pipeline;
    }
}
