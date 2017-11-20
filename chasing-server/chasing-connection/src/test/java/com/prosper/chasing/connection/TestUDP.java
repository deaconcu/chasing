package com.prosper.chasing.connection;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.util.AttributeKey;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.FutureListener;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

public class TestUDP {
    
    Channel channel;
    
    public static void main(String[] args) throws InterruptedException {
        TestUDP testUDP = new TestUDP();
        testUDP.start();
        int count = 0;
        while (true) {
            ByteBuffer buffer = ByteBuffer.allocate(36);
            buffer.putInt(1001);
            byte[] sessionBytes = "test-session-key".getBytes();
            buffer.put(sessionBytes);
            buffer.putLong(System.currentTimeMillis());
            buffer.putInt(1);
            //buffer.putInt(1);
            //buffer.putInt(1);
            //buffer.putInt(1);
            //buffer.putInt(1);
            buffer.flip();
            byte[] data = buffer.array();
        
            //testUDP.sendData("120.27.112.99", 8201, data);
            //testUDP.sendData("192.168.1.17", 8201, data);

            testUDP.sendData("127.0.0.1", 8201, data);
            System.out.println("udp server send data, count:" + count);
            count ++;
            Thread.sleep(2);
        }
    }

    public void sendData(String ip, int port, byte[] data) {
        InetSocketAddress address = new InetSocketAddress(ip, port);
        try {
            channel.writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer(data), address)).sync();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void start() {
        try {
            EventLoopGroup group = new NioEventLoopGroup();
            try {
                Bootstrap b = new Bootstrap();
                b.group(group)
                .channel(NioDatagramChannel.class)
//                .option(ChannelOption.SO_BROADCAST, true)
                .handler(new UDPServerHandler());

                channel = b.bind(8001).sync().channel();
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
            System.out.printf("%s received %s%n", ctx.channel(), packet.content());
            int length = packet.content().readableBytes();
            byte[] data = new byte[length];
            packet.content().readBytes(data);

            System.out.print(data);
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
