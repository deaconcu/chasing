package com.prosper.chasing.common.bean.wrapper;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.util.AttributeKey;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

public class NettyUDPServer implements ApplicationListener<ContextRefreshedEvent>{

    private Logger log = LoggerFactory.getLogger(getClass());

    private final AttributeKey<Map<String, Object>> customValueKey = AttributeKey.valueOf("customValueKey");

    private int port;
    private Map<Integer, InetSocketAddress> sourceMap;
    private UDPService service;
    private Channel channel;

    public NettyUDPServer(int port, UDPService service) {
        this.port = port;
        this.service = service;
        sourceMap = new ConcurrentHashMap<>();
    }

    public void sendData(Integer key, byte[] data) {
        channel.writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer(data), sourceMap.get(key)));
    }

    @Override
    public void onApplicationEvent(final ContextRefreshedEvent event) {
        try {
            EventLoopGroup group = new NioEventLoopGroup();
            try {
                Bootstrap b = new Bootstrap();
                b.group(group)
                .channel(NioDatagramChannel.class)
//                .option(ChannelOption.SO_BROADCAST, true)
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

    public class UDPServerHandler extends SimpleChannelInboundHandler<DatagramPacket> {
        
        @Override
        public void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet) throws Exception {
            int key = service.executeData(packet.content());
            if (key > 0) {
                String sourceIp = packet.sender().getAddress().getHostAddress();
                int port = packet.sender().getPort();

                InetSocketAddress address = new InetSocketAddress(sourceIp, port);
                sourceMap.put(key, address);
            }
            System.out.printf("%s received %s%n", ctx.channel(), packet.content());
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
