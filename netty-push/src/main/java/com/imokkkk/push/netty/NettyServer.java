package com.imokkkk.push.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * @author liuwy
 * @date 2023-05-06 14:45
 * @since 1.0
 */
@Component
public class NettyServer {
    private static final Logger log = LoggerFactory.getLogger(NettyServer.class);
    /** webSocket协议名 */
    private static final String WEBSOCKET_PROTOCOL = "WebSocket";

    /** 端口号 */
    @Value("${webSocket.netty.port:58080}")
    private int port;

    /** webSocket路径 */
    @Value("${webSocket.netty.path:/webSocket}")
    private String webSocketPath;

    @Value("${webSocket.heart.timeout:20}")
    private int heartTimeout;

    @Autowired private IWebSocketHandler iWebSocketHandler;

    private EventLoopGroup bossGroup;
    private EventLoopGroup workGroup;

    @PostConstruct
    public void init() {
        new Thread(
                        () -> {
                            try {
                                start();
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                        })
                .start();
    }

    public void start() throws InterruptedException {
        bossGroup = new NioEventLoopGroup();
        workGroup = new NioEventLoopGroup();
        ServerBootstrap b = new ServerBootstrap();
        b.group(bossGroup, workGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(
                        new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel ch) throws Exception {
                                ChannelPipeline pipeline = ch.pipeline();
                                pipeline.addLast(new HttpServerCodec());
                                ch.pipeline().addLast(new ChunkedWriteHandler());
                                /*
                                http数据在传输过程中是分段的，HttpObjectAggregator可以将多个段聚合
                                 */
                                ch.pipeline().addLast(new HttpObjectAggregator(10 * 1024 * 1024));
                                // 针对客户端，若10s内无读事件则触发心跳处理方法HeartBeatHandler#userEventTriggered
                                ch.pipeline().addLast(new IdleStateHandler(heartTimeout, 0, 0));
                                // 自定义空闲状态检测(自定义心跳检测handler)
                                ch.pipeline().addLast(new HeartBeatHandler());
                                /*
                                webSocket的数据是以帧（frame）的形式传递
                                浏览器请求时 ws://localhost:58080/xxx 表示请求的uri，将http协议升级为ws协议，保持长连接
                                */
                                ch.pipeline()
                                        .addLast(
                                                new WebSocketServerProtocolHandler(
                                                        webSocketPath,
                                                        WEBSOCKET_PROTOCOL,
                                                        true,
                                                        65536 * 10));
                                // 自定义的handler，处理业务逻辑
                                ch.pipeline().addLast(iWebSocketHandler);
                            }
                        });
        ChannelFuture f = b.bind(port).sync();
        log.info("服务端启动并监听：【{}】", f.channel().localAddress());
        // 对关闭通道进行监听
        f.channel().closeFuture().sync();
    }

    @PreDestroy
    public void destroy() throws InterruptedException {
        if (bossGroup != null) {
            bossGroup.shutdownGracefully().sync();
        }
        if (workGroup != null) {
            workGroup.shutdownGracefully().sync();
        }
    }
}
