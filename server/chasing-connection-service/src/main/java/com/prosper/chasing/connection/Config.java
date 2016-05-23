package com.prosper.chasing.connection;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component("connectionConfig")
public class Config {
    
    @Value("${application.package}")
    public String appPackage;
    
    @Value("${rpc.port}")
    public int rpcPort;
    
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
    
    @Value("${zkName.gameServer}")
    public String gameServerZKName;

    @Value("${zkName.gameDataServer}")
    public String gameDataServerZKName;
    
    @Value("${zkName.game}")
    public String gameZKName;
		
}
