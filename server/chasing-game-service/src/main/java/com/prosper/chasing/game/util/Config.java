package com.prosper.chasing.game.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Primary
@Component
public class Config {

    @Value("${server.ip}")
    public String serverIp;
    
    @Value("${server.port}")
    public int serverPort;
    
    @Value("${redis.ip}")
    public String redisIp;
    
    @Value("${redis.port}")
    public int redisPort;
    
    @Value("${zookeeper.addrs}")
    public String zookeeperAddrs;
    
    @Value("${gameServer.list.zkName}")
    public String gameServerZKName;

    @Value("${gameDataServer.list.zkName}")
    public String gameDataServerZKName;
    
		
}
