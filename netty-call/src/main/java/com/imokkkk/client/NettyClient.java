package com.imokkkk.client;

import cn.hutool.core.lang.id.NanoId;

import com.imokkkk.client.handler.ClientBizHandler;
import com.imokkkk.client.handler.HearBeatReqHandler;
import com.imokkkk.client.handler.LoginAuthReqHandler;
import com.imokkkk.kryo.KryoDecoder;
import com.imokkkk.kryo.KryoEncoder;
import com.imokkkk.model.Message;
import com.imokkkk.model.MessageType;
import com.imokkkk.server.NettyServer;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author liuwy
 * @date 2023-05-11 10:54
 * @since 1.0
 */
@Slf4j
public class NettyClient implements Runnable {
    private EventLoopGroup group = new NioEventLoopGroup();

    private Channel channel;

    /*是否用户主动关闭连接的标志值*/
    private volatile boolean userClose = false;
    /*连接是否成功关闭的标志值*/
    private volatile boolean connected = false;

    /*负责重连的线程池*/
    private ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

    @Override
    public void run() {
        try {
            connect(NettyServer.SERVER_IP, NettyServer.SERVER_PORT);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /*------------测试NettyClient--------------------------*/
    public static void main(String[] args) throws Exception {
        NettyClient nettyClient = new NettyClient();
        nettyClient.connect(NettyServer.SERVER_IP, NettyServer.SERVER_PORT);
        nettyClient.send("Hello");
        nettyClient.close();
    }

    /*------------以下方法供业务方使用--------------------------*/
    public void send(Object message) {
        if (channel == null || !channel.isActive()) {
            throw new IllegalStateException("和服务器还未未建立起有效连接！" + "请稍后再试！！");
        }
        Message msg = new Message();
        msg.buildBasicMessage(message, NanoId.randomNanoId(), MessageType.SERVICE_REQ);
        channel.writeAndFlush(msg);
    }

    public void sendOneWay(Object message) {
        if (channel == null || !channel.isActive()) {
            throw new IllegalStateException("和服务器还未未建立起有效连接！" + "请稍后再试！！");
        }

        if (channel == null || !channel.isActive()) {
            throw new IllegalStateException("和服务器还未未建立起有效连接！" + "请稍后再试！！");
        }
        Message msg = new Message();
        msg.buildBasicMessage(message, NanoId.randomNanoId(), MessageType.ONE_WAY);
        channel.writeAndFlush(msg);
    }

    public void close() {
        userClose = true;
        channel.close();
    }

    private void connect(String host, int port) throws InterruptedException {
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap
                    .group(group)
                    .channel(NioSocketChannel.class)
                    .handler(
                            new ChannelInitializer<SocketChannel>() {
                                @Override
                                protected void initChannel(SocketChannel ch) throws Exception {
                                    ChannelPipeline pipeline = ch.pipeline();
                                    // 连接写空闲检测
                                    pipeline.addLast(new IdleStateHandler(0, 8, 0));

                                    pipeline.addLast(
                                            new LengthFieldBasedFrameDecoder(65535, 0, 2, 0, 2));
                                    pipeline.addLast(new LengthFieldPrepender(2)); // 序列化
                                    pipeline.addLast(new KryoDecoder());
                                    pipeline.addLast(new KryoEncoder());

                                    pipeline.addLast(new LoggingHandler(LogLevel.DEBUG));

                                    pipeline.addLast(new LoginAuthReqHandler());

                                    pipeline.addLast(new ReadTimeoutHandler(15));

                                    pipeline.addLast(new HearBeatReqHandler());
                                    pipeline.addLast(new ClientBizHandler());
                                }
                            });
            ChannelFuture future = bootstrap.connect(host, port).sync();
            log.info("已连接服务器！");
            channel = future.channel();
            synchronized (this) {
                this.connected = true;
                this.notifyAll();
            }
            channel.closeFuture().sync();
        } finally {
            if (!userClose) {
                log.warn("需要重新连接！");
                executor.execute(
                        new Runnable() {
                            @Override
                            public void run() {
                                // 等待释放相关资源
                                try {
                                    TimeUnit.SECONDS.sleep(1);
                                    connect(NettyServer.SERVER_IP, NettyServer.SERVER_PORT);
                                } catch (InterruptedException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        });
            } else {
                channel = null;
                group.shutdownGracefully().sync();
                synchronized (this) {
                    this.connected = false;
                    this.notifyAll();
                }
            }
        }
    }
}
