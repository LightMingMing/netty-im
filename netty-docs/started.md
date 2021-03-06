# Netty 学习
## 概述
[官网](https://netty.io)

Netty 是一个异步事件驱动的网络应用框架，用于快速开发高性能基于协议的客户端、服务端。 
### 特点
#### 设计
1. 不同传输类型如阻塞、非阻塞`socket`, 使用统一的API
2. 灵活的、可扩展的事件模型, 开发者只需关注事件的回调
3. 高可自定制的线程模型-单线程、多线程池如**`SEDA`** - **`Staged event driven architechure`**, 事件的不同阶段可采用不同的线程去处理
4. 支持无连接的数据报`datagram`
#### 性能
1. 高吞吐、低延迟 **Better throughput**, **lower latency**
2. 最少资源消耗
3. 最少不必要的内存拷贝

#### 支持的协议
1. `HTPP & WebSocket`
2. `SSL StartTLS`
3. `Google Protobuf`, 序列化结构化的数据，跨语言(Java、Python、C++)、跨平台
4. `zlib/gzip compression` 压缩
5. `Large File Transfer` 大文件传输
6. `RTSP` - `Real Time Streaming Protocol`网络流媒体协议

## 组件

```java
public class HttpServerBootstrap {

    public static void main(String[] args) throws InterruptedException {
        NioEventLoopGroup boss = new NioEventLoopGroup();
        NioEventLoopGroup worker = new NioEventLoopGroup();
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(boss, worker).channel(NioServerSocketChannel.class)
                    .handler(new BossChannelHandler())
                    .childHandler(new WorkerChannelInitializer());
            ChannelFuture channelFuture = serverBootstrap.bind(8080).sync();
            channelFuture.channel().closeFuture().sync();
        } finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }
}
```
1. NioEventLoopGroup Nio事件轮询组, 一般定义两个boss, worker, boss用来接收新的请求，交给worker去处理具体的请求
2. ServerBootstrap 服务端启动
3. ChannelHandler 通道处理器

### 通道处理器
基础的几个通道处理器UML图, 如下:
![ChannelHandler](png/ChannelHandler.png)

**ChannelHandler**
```java
package io.netty.channel;

public interface ChannelHandler {

    // 通道处理器被添加时，调用该方法，准备处理事件
    void handlerAdded(ChannelHandlerContext ctx) throws Exception;

    // 通道处理器被移除时，调用该方法，不再处理事件
    void handlerRemoved(ChannelHandlerContext ctx) throws Exception;
}
```

**ChannelHandlerAdapter**  
实现了`ChannelHandler`接口, 但操作为空，允许子类重写
```java
package io.netty.channel;

public abstract class ChannelHandlerAdapter implements ChannelHandler {
 
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        // NOOP
    }
    
    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        // NOOP
    }
}
```

**ChannelInboundHandler**  
定义了通道相关的回调方法**注册、打开、读取消息、关闭、取消(通道生命周期结束)** 
```java
package io.netty.channel;

public interface ChannelInboundHandler extends ChannelHandler {

    // 通道注册, 绑定到EventLoop
    void channelRegistered(ChannelHandlerContext ctx) throws Exception;
    
    // 取消通道注册，取消在EventLoop中的绑定
    void channelUnregistered(ChannelHandlerContext ctx) throws Exception;

    // 通道打开，等待连接
    void channelActive(ChannelHandlerContext ctx) throws Exception;

    // 通道关闭，通道的生命周期结束
    void channelInactive(ChannelHandlerContext ctx) throws Exception;

    // 通道读取消息
    void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception;

    // 通道读取消息完成
    void channelReadComplete(ChannelHandlerContext ctx) throws Exception;
}
```

**ChannelInboundHandlerAdapter**  
实现了ChannelInboundHandler中的所有方法，实现方式仅仅是将操作传递给下一个`ChannelPipeline`中的`ChannelHandler`，即回调下一个`ChannelHandler`的方法
```java
package io.netty.channel;

public class ChannelInboundHandlerAdapter extends ChannelHandlerAdapter implements ChannelInboundHandler {

    // provide implementations of all ChannelInboundHandler's methods
}
```

**SimpleChannelInboundHandler**  
抽象范型类，重点关注`channelRead`通道读取，范型`I`即为要读取的消息类型，如果类型匹配，则调用子类定义的回调方法`channelRead0`(消息接收)进行读取
```java
package io.netty.channel;

public abstract class SimpleChannelInboundHandler<I> extends ChannelInboundHandlerAdapter {
        
    private final TypeParameterMatcher matcher;
    private final boolean autoRelease;

    protected SimpleChannelInboundHandler() {
        this(true);
    }

    protected SimpleChannelInboundHandler(boolean autoRelease) {
        matcher = TypeParameterMatcher.find(this, SimpleChannelInboundHandler.class, "I");
        this.autoRelease = autoRelease;
    }
    
    // ...
    
    public boolean acceptInboundMessage(Object msg) throws Exception { 
        return matcher.match(msg);
    }
    
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        boolean release = true;
        try {
            if (acceptInboundMessage(msg)) {
                @SuppressWarnings("unchecked")
                I imsg = (I) msg;
                channelRead0(ctx, imsg);
            } else {
                release = false;
                ctx.fireChannelRead(msg);
            }
        } finally {
            if (autoRelease && release) {
                ReferenceCountUtil.release(msg);
            }
        }
    }
}
```
**ChannelInitializer**  
正常情况下，在通道处理器被添加到ChannelPipeline后，进行通道的初始化，并且通道初始化后，通道处理器会被从ChannelPipeline中删除(即回调`handlerRemoved`)
```java
package io.netty.channel;

public abstract class ChannelInitializer<C extends Channel> extends ChannelInboundHandlerAdapter {
    
    private final Set<ChannelHandlerContext> initMap = Collections.newSetFromMap(
            new ConcurrentHashMap<ChannelHandlerContext, Boolean>());

    // 通道注册时，调用该方法，之后该实例会被从channel的ChannelPipeline中移除
    protected abstract void initChannel(C ch) throws Exception;

    @Override
    public final void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        // Normally this method will never be called as handlerAdded(...) should call initChannel(...) and remove
        // the handler.
        if (initChannel(ctx)) {
            // we called initChannel(...) so we need to call now pipeline.fireChannelRegistered() to ensure we not
            // miss an event.
            ctx.pipeline().fireChannelRegistered();

            // We are done with init the Channel, removing all the state for the Channel now.
            removeState(ctx);
        } else {
            // Called initChannel(...) before which is the expected behavior, so just forward the event.
            ctx.fireChannelRegistered();
        }
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        if (ctx.channel().isRegistered()) {
            // This should always be true with our current DefaultChannelPipeline implementation.
            // The good thing about calling initChannel(...) in handlerAdded(...) is that there will be no ordering
            // surprises if a ChannelInitializer will add another ChannelInitializer. This is as all handlers
            // will be added in the expected order.
            if (initChannel(ctx)) {

                // We are done with init the Channel, removing the initializer now.
                removeState(ctx);
            }
        }
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        initMap.remove(ctx);
    }

    private boolean initChannel(ChannelHandlerContext ctx) throws Exception {
        if (initMap.add(ctx)) { // Guard against re-entrance.
            try {
                initChannel((C) ctx.channel());
            } catch (Throwable cause) {
                // Explicitly call exceptionCaught(...) as we removed the handler before calling initChannel(...).
                // We do so to prevent multiple calls to initChannel(...).
                exceptionCaught(ctx, cause);
            } finally {
                ChannelPipeline pipeline = ctx.pipeline();
                if (pipeline.context(this) != null) {
                    pipeline.remove(this);
                }
            }
            return true;
        }
        return false;
    }
}
```

**lsof命令**
```
➜  ~ lsof -i:8080
COMMAND PID     USER   FD   TYPE             DEVICE SIZE/OFF NODE NAME
java    938 mingming  165u  IPv6 0x221afd4ccaad178f      0t0  TCP *:http-alt (LISTEN)
java    938 mingming  166u  IPv6 0x221afd4ccaad008f      0t0  TCP localhost:http-alt->localhost:49987 (ESTABLISHED)
java    940 mingming   93u  IPv6 0x221afd4ccaacf50f      0t0  TCP localhost:49987->localhost:http-alt (ESTABLISHED)
```
**nc命令**
```
➜  ~ nc localhost 8080
1234567890y
1234567890y
```
### 通道
`Channel`用于客户端与服务端的交互, 相关类图如下
![通道类图](png/Channel.png)

```java
package io.netty.channel;
public interface Channel extends AttributeMap, ChannelOutboundInvoker, Comparable<Channel> {
    
    // 通道的全局唯一的标识符
    ChannelId id();

    // 返回通道注册的事件轮询组
    EventLoop eventLoop();

    Channel parent();

    ChannelConfig config();

    boolean isOpen();

    boolean isRegistered();

    boolean isActive();
}
```
对于`Socket`有两个重要的通道`SocketChannel`, `NioSocketChannel`
**SocketChannel**
```java
package io.netty.channel.socket;

import io.netty.channel.Channel;

import java.net.InetSocketAddress;

public interface SocketChannel extends DuplexChannel {
    @Override
    ServerSocketChannel parent();
    @Override
    SocketChannelConfig config();
    @Override
    InetSocketAddress localAddress();
    @Override
    InetSocketAddress remoteAddress();
}
```

**ServerSocketChannel**
```java
package io.netty.channel.socket;

import io.netty.channel.ServerChannel;

import java.net.InetSocketAddress;

public interface ServerSocketChannel extends ServerChannel {
    @Override
    ServerSocketChannelConfig config();
    @Override
    InetSocketAddress localAddress();
    @Override
    InetSocketAddress remoteAddress();
}
```

**通道组**  
`ChannelGroup`一组通道的集合
```java
package io.netty.channel.group;
public interface ChannelGroup extends Set<Channel>, Comparable<ChannelGroup> {

    String name();

    Channel find(ChannelId id);

    ChannelGroupFuture write(Object message);

    ChannelGroupFuture write(Object message, ChannelMatcher matcher);
    
    ChannelGroupFuture write(Object message, ChannelMatcher matcher, boolean voidPromise);
    
    ChannelGroup flush(ChannelMatcher matcher);
        
    ChannelGroupFuture writeAndFlush(Object message);

    ChannelGroupFuture writeAndFlush(Object message, ChannelMatcher matcher);
    
    ChannelGroupFuture writeAndFlush(Object message, ChannelMatcher matcher, boolean voidPromise);
    
    // ....
}
```

#### 编码器、解码器

### 心跳检测
空闲检测处理器 `IdleStateHandler`  
服务端
```java
public class ServerChannelInitializer extends ChannelInitializer<NioSocketChannel> {
    @Override
    protected void initChannel(NioSocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(new IdleStateHandler(5, 10, 15));
        pipeline.addLast(new HeartbeatDetectHandler());
    }
}
```
```java
public class HeartbeatDetectHandler extends ChannelInboundHandlerAdapter {
    
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
```
**IdleState**枚举类型
```java
package io.netty.handler.timeout;

import io.netty.channel.Channel;

public enum IdleState {
    /**
     * No data was received for a while.
     */
    READER_IDLE,
    /**
     * No data was sent for a while.
     */
    WRITER_IDLE,
    /**
     * No data was either received or sent for a while.
     */
    ALL_IDLE
}
```
### Web Socket的支持

Http特性
1. 无状态的
2. 基于请求、进行响应的
3. Http1.1 keep-alive 持续连接--> 连接重用
4. Http2 长连接

如果在浏览器上实现聊天不太现实，需要服务端主动向客户端推送消息. 一般采用轮询的方法，但是数据的及时性不高、每次都是Http请求，会有很多的头信息，容量甚至可能超过内容本身。  
WebSocket 能够实现真正意义上的长连接，连接一旦建立，双方就是一个对等的关系，能够双向的数据通信。真正实现服务端的push，同时只需发送数据本身，不需要发送头信息，有效的减少网络带宽。


`new HttpServerCodec()`
`new ChunkedWriteHandler()`
`new HttpObjectAggregator(8192)`
`new WebSocketServerProtocolHandler(uri)`

```
channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) {
    msg.text();
    ctx.channel().writeAndFlush(new TextWebSocketFrame("Hello"));
}
```
6种 WebSocketFrame 帧
1. BinaryWebSocketFrame 二进制
2. CloseWebSocketFrame 关闭指令
3. ContinueWebSocketFrame 数据未发送完, 还会有
4. PingWebSocketFrame ping指令
5. PongWebSocketFrame ping指令的响应pong
6. TextWebSocketFrame 帧中包含的文本内容  
类图如下
![WebSocketFrame](png/WebSocketFrame.png)

头信息
```
General
Request URL: ws://localhost:8080/websocket
Request Method: GET
Status Code: 101 Switching Protocols

Response Headers
connection: upgrade
sec-websocket-accept: v9HwMmcvOtDPeUnBVxs9cbu8s2o=
upgrade: websocket

Request Headers
Accept-Encoding: gzip, deflate, br
Accept-Language: zh-CN,zh;q=0.9
Cache-Control: no-cache
Connection: Upgrade
Host: localhost:8080
Origin: http://localhost:63342
Pragma: no-cache
Sec-WebSocket-Extensions: permessage-deflate; client_max_window_bits
Sec-WebSocket-Key: XlFj4lrfCIw9C8J9R2jZeg==
Sec-WebSocket-Version: 13
Upgrade: websocket
```
**Status Code: 101 Switching Protocols**  
**Connection: Upgrade**  
**Upgrade: websocket**  
