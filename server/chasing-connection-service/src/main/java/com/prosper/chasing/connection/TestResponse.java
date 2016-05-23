package com.prosper.chasing.connection;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import java.util.Map;

public class TestResponse extends Thread {

    private Map<Long, Channel> channelMap;

    public void setChannelMap(Map<Long, Channel> channelMap) {
        this.channelMap = channelMap;
    }

    @Override
    public void run() {
        while(true) {
            System.out.println("check");
            if (!channelMap.isEmpty()) {
                System.out.println("no empty");
                for (Long id: channelMap.keySet()) {
                    Channel channel = channelMap.get(id);
                    System.out.println(channel);
                    channel.writeAndFlush(new TextWebSocketFrame("test success"));
                }
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

}
