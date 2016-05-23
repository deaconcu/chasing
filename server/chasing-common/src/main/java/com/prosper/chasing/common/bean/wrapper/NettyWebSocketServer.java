package com.prosper.chasing.common.bean.wrapper;

import static io.netty.handler.codec.http.HttpHeaders.Names.HOST;
import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.FORBIDDEN;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import io.netty.util.AttributeKey;
import io.netty.util.CharsetUtil;

import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.thrift.TMultiplexedProcessor;
import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import com.prosper.chasing.common.bean.client.ZkClient;

public class NettyWebSocketServer implements ApplicationListener<ContextRefreshedEvent>{
    
    private Logger log = LoggerFactory.getLogger(getClass());
    
    private final AttributeKey<Map<String, Object>> customValueKey = AttributeKey.valueOf("customValueKey");
    
    private int port;
    private boolean isSSL;
    private Map<Integer, Channel> channelMap;
    private WebSocketService service;
    
    public NettyWebSocketServer(int port, boolean isSSL, WebSocketService service) {
        this.port = port;
        this.isSSL = isSSL;
        this.service = service;
        channelMap = new ConcurrentHashMap<>();
    }

    @Override
    public void onApplicationEvent(final ContextRefreshedEvent event) {
        try {
            final SslContext sslCtx;
            if (isSSL) {
                SelfSignedCertificate ssc = new SelfSignedCertificate();
                sslCtx = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();
            } else {
                sslCtx = null;
            }

            EventLoopGroup bossGroup = new NioEventLoopGroup();
            EventLoopGroup workerGroup = new NioEventLoopGroup();

            try {
                NettySocketInitializer wssi = new NettySocketInitializer(sslCtx);
                ServerBootstrap b = new ServerBootstrap();
                b.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(wssi)
                .option(ChannelOption.SO_BACKLOG, 128)  // 设置最大等待数
                .childOption(ChannelOption.SO_KEEPALIVE, true);

                ChannelFuture f = b.bind(port).sync();
                f.channel().closeFuture().sync();
            } finally {
                workerGroup.shutdownGracefully();
                bossGroup.shutdownGracefully();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    private class NettySocketInitializer extends ChannelInitializer<SocketChannel> {

        private final SslContext sslCtx;
        private boolean isSSL = false;

        public NettySocketInitializer(SslContext sslCtx) {
            this.sslCtx = sslCtx;
            if (sslCtx == null) {
                this.isSSL = true;
            }
        }
        
        @Override
        public void initChannel(SocketChannel ch) throws Exception {
            ChannelPipeline pipeline = ch.pipeline();
            if (sslCtx != null) {
                pipeline.addLast(sslCtx.newHandler(ch.alloc()));
            }
            
            NettyWebSocketHandler wsServerHadler = new NettyWebSocketHandler(isSSL);
            
            pipeline.addLast(new HttpServerCodec());
            pipeline.addLast(new HttpObjectAggregator(65536));
            pipeline.addLast(wsServerHadler);
        }

    }
    
    private class NettyWebSocketHandler extends SimpleChannelInboundHandler<Object> {
        private static final String WEBSOCKET_PATH = "/websocket";

        private WebSocketServerHandshaker handshaker;
        
        private boolean isSSL = false;
        
        public NettyWebSocketHandler() {
        }
        
        public NettyWebSocketHandler(boolean isSSL) {
            this.isSSL = isSSL;
        }

        @Override
        public void channelRead0(ChannelHandlerContext ctx, Object msg) {
            if (msg instanceof FullHttpRequest) {
                handleHttpRequest(ctx, (FullHttpRequest) msg);
            } else if (msg instanceof WebSocketFrame) {
                handleWebSocketFrame(ctx, (WebSocketFrame) msg);
            }
        }

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) {
            ctx.flush();
        }

        private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest req) {
            if (!req.getDecoderResult().isSuccess()) {
                sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, BAD_REQUEST));
                return;
            }

            if (req.getMethod() != GET) {
                sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, FORBIDDEN));
                return;
            }
            
            Integer key = null;
            try {
                ChannelInfo channelInfo = service.executeHttpRequest(req);
                key = channelInfo.getKey();
                if (key == null) {
                    throw new RuntimeException("key is not provided");
                }
                ctx.attr(customValueKey).set(channelInfo.getCustomValues());
            } catch (Exception e) {
                log.info("execute http failed", e);
                sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, FORBIDDEN));
            }

            // Handshake
            WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(
                    getWebSocketLocation(req), null, true);
            handshaker = wsFactory.newHandshaker(req);
            if (handshaker == null) {
                WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
            } else {
                handshaker.handshake(ctx.channel(), req);
            }

            channelMap.put(key, ctx.channel());
        }

        private void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {

            // Check for closing frame
            if (frame instanceof CloseWebSocketFrame) {
                handshaker.close(ctx.channel(), (CloseWebSocketFrame) frame.retain());
                return;
            }
            if (frame instanceof PingWebSocketFrame) {
                ctx.channel().write(new PongWebSocketFrame(frame.content().retain()));
                return;
            }
            if (!(frame instanceof BinaryWebSocketFrame)) {
                throw new UnsupportedOperationException(
                        String.format("%s frame types not supported", frame.getClass().getName()));
            }

            // Send the uppercase string back.
            ByteBuf data = ((BinaryWebSocketFrame) frame).content();
            service.executeData(data, ctx.attr(customValueKey).get());
            System.out.printf("%s received %s%n", ctx.channel(), data);
//            ctx.channel().write(new TextWebSocketFrame(request.toUpperCase()));
        }

        private void sendHttpResponse(
                ChannelHandlerContext ctx, FullHttpRequest req, FullHttpResponse res) {
            // Generate an error page if response getStatus code is not OK (200).
            if (res.getStatus().code() != 200) {
                ByteBuf buf = Unpooled.copiedBuffer(res.getStatus().toString(), CharsetUtil.UTF_8);
                res.content().writeBytes(buf);
                buf.release();
                HttpHeaders.setContentLength(res, res.content().readableBytes());
            }

            // Send the response and close the connection if necessary.
            ChannelFuture f = ctx.channel().writeAndFlush(res);
            if (!HttpHeaders.isKeepAlive(req) || res.getStatus().code() != 200) {
                f.addListener(ChannelFutureListener.CLOSE);
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            cause.printStackTrace();
            ctx.close();
        }

        private String getWebSocketLocation(FullHttpRequest req) {
            String location =  req.headers().get(HOST) + WEBSOCKET_PATH;
            if (isSSL) {
                return "wss://" + location;
            } else {
                return "ws://" + location;
            }
        }

    }

}
