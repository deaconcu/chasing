package com.prosper.chasing.connection.server;

import io.netty.channel.Channel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.prosper.chasing.common.client.ZkClient;
import com.prosper.chasing.connection.game.ChannelMap;
import com.prosper.chasing.connection.game.MessageManager;

public class Booter {

    public static void main(String[] args) {
        MessageManager.init(10, 100);
        ChannelMap.init();
        ZkClient.init("192.168.92.128:2181");

//        TestResponse tr = new TestResponse();
//        tr.setChannelMap(ChannelMap.instance());
//        tr.start();
        
        WebSocketServer server = new WebSocketServer(8080);
        server.run();
    }

}
