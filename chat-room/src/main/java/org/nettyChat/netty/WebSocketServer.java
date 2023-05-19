package org.nettyChat.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;
import org.nettyChat.session.SessionMap;


import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

public class WebSocketServer {
    private static  WebSocketServer singleInstance = new WebSocketServer();
    public static WebSocketServer inst()
    {
        return singleInstance;
    }
    private final EventLoopGroup group = new NioEventLoopGroup();
    private Channel channel;

    /**
     * 关闭服务器。
     */
    public void stop()
    {
        if (channel != null)
        {
            channel.close();
        }
        SessionMap.inst().shutdownGracefully();
        group.shutdownGracefully();
    }
    public void start(){
        WebSocketServer webSocketServer = new WebSocketServer();
        ChannelFuture channelFuture = null;
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(group).channel(NioServerSocketChannel.class)
                .childHandler(new ChatServerInitializer());
        InetSocketAddress inetSocketAddress = new InetSocketAddress(9999);
        channelFuture = bootstrap.bind(inetSocketAddress);
        channelFuture.syncUninterruptibly();
        channel = channelFuture.channel();
        //在jvm中增加一个关闭的钩子，当jvm关闭的时候，会执行系统中已经设置的所有通过方法addShutdownHook添加的钩子，当系统执行完这些钩子后，jvm才会关闭
        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run(){
                webSocketServer.stop();
            }
        });
        channelFuture.channel().closeFuture().syncUninterruptibly();
    }
    class ChatServerInitializer extends ChannelInitializer<Channel>{

        private static final int READ_IDLE_TIME_OUT = 60; // 读超时  s
        private static final int WRITE_IDLE_TIME_OUT = 0;// 写超时
        private static final int ALL_IDLE_TIME_OUT = 0; // 所有超时

        @Override
        protected void initChannel(Channel channel) throws Exception {
            ChannelPipeline pipeline = channel.pipeline();
            //Netty的编码器和解码器
            pipeline.addLast(new HttpServerCodec());

            pipeline.addLast(new ChunkedWriteHandler());

            pipeline.addLast(new HttpObjectAggregator(64 * 1024));
            //WebSocket数据压缩
            pipeline.addLast(new WebSocketServerCompressionHandler());
            //配置websocket的监听地址/协议包长度限制
            pipeline.addLast(new WebSocketServerProtocolHandler("/ws", null, true, 10 * 1024));
            //当连接在60秒内没有接收到消息时，就会触发一个 IdleStateEvent 事件，
            pipeline.addLast(new IdleStateHandler(READ_IDLE_TIME_OUT, WRITE_IDLE_TIME_OUT, ALL_IDLE_TIME_OUT, TimeUnit.SECONDS));
            //自定义逻辑处理器，
            pipeline.addLast(new TextWebSocketFrameHandler() );

        }
    }
}
