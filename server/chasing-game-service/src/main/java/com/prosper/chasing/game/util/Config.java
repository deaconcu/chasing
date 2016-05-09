package com.prosper.chasing.game.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Primary
@Component
public class Config {

    @Value("${redis.ip}")
    public String redisIp;
    
    @Value("${redis.port}")
    public int redisPort;
    
    @Value("${zookeeper.addrs}")
    public String zookeeperAddrs;
		
}
