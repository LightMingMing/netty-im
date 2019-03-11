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
![ChannelHandler](png/ChannelHandler.png)