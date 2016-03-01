package com.prosper.chasing.connection.game;

import io.netty.channel.Channel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChannelMap {
    
    public static Map<Long, Channel> channelMap;
    
    public static Map<Long, Channel> instance() {
        return channelMap;
    }
    
    public static void init() {
        channelMap = new ConcurrentHashMap<>();
    }
    
    private ChannelMap() {}

}
