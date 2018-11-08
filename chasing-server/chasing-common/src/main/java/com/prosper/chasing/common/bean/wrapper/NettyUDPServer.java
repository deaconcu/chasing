package com.prosper.chasing.common.bean.wrapper;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.util.AttributeKey;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.util.StopWatch;

public class NettyUDPServer implements ApplicationListener<ContextRefreshedEvent>{

    private Logger log = LoggerFactory.getLogger(getClass());

    private final AttributeKey<Map<String, Object>> customValueKey = AttributeKey.valueOf("customValueKey");

    private int port;
    private Map<Integer, InetSocketAddress> sourceMap;
    private UDPService service;
    private Channel channel;
    private ExecutorService executorService;

    public NettyUDPServer(int port, UDPService service) {
        this.port = port;
        this.service = service;
        sourceMap = new ConcurrentHashMap<>();
        executorService = Executors.newCachedThreadPool();
    }

    public void sendData(Integer key, byte[] data) {
        StopWatch watch = new StopWatch();
        watch.start();
        channel.writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer(data), sourceMap.get(key)));
        watch.stop();
        //log.info("send data cost:" + watch.getLastTaskTimeMillis());
    }

    @Override
    public void onApplicationEvent(final ContextRefreshedEvent event) {
        try {
            EventLoopGroup group = new NioEventLoopGroup(32);
            try {
                Bootstrap b = new Bootstrap();
                b.group(group)
                .channel(NioDatagramChannel.class)
                .option(ChannelOption.SO_BROADCAST, true)
                .option(ChannelOption.SO_SNDBUF, 1024 * 2048)
                .option(ChannelOption.SO_RCVBUF, 1024 * 2048)
                .handler(new UDPServerHandler());

                channel = b.bind(port).sync().channel();
                log.info("starting udp server on port " + port + " ...");
            } finally {
//                group.shutdownGracefully();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @ChannelHandler.Sharable
    public class UDPServerHandler extends ChannelInboundHandlerAdapter {
        
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            executorService.execute(
                new Runnable() {
                    @Override
                    public void run() {
                        DatagramPacket packet = (DatagramPacket) msg;
                        int key = service.executeData(packet.content());
                        if (key > 0) {
                            String sourceIp = packet.sender().getAddress().getHostAddress();
                            int port = packet.sender().getPort();

                            InetSocketAddress address = new InetSocketAddress(sourceIp, port);
                            sourceMap.put(key, address);
                        }
                        System.out.printf("%s received %s%n", ctx.channel(), packet.content());
                    }
                }
            );
        }
    
        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) {
            ctx.flush();
        }
    
        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            cause.printStackTrace();
        }
    }

}
