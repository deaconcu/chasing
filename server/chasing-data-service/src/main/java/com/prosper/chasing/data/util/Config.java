package com.prosper.chasing.data.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

@Component
@DependsOn("propertySources")
public class Config {

    @Value("${db.mysql.ip}")
    public String dbIp;
    
    @Value("${db.mysql.port}")
    public String dbPort;
    
    @Value("${db.mysql.dbName}")
    public String dbName;
    
    @Value("${db.mysql.userName}")
    public String dbUserName;
    
    @Value("${db.mysql.password}")
    public String dbPassword;
    
    @Value("${redis.ip}")
    public String redisIp;
    
    @Value("${redis.port}")
    public int redisPort;
    
    @Value("${zookeeper.addrs}")
    public String zookeeperAddrs;
    
}
