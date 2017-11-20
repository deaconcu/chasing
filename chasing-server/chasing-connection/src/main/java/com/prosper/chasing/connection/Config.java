package com.prosper.chasing.connection;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component("connectionConfig")
public class Config {

    @Value("${application.package}")
    public String appPackage;  //用来标识当前项目的package，用来查找service
    
    @Value("${rpc.port}")
    public int rpcPort;  //rpc服务端口，发送用户同步消息
    
    @Value("${server.ip}")
    public String serverIp;  //服务器ip
    
    @Value("${udp.port}")
    public int serverPort;  //udp服务端口，接收用户消息
    
    @Value("${redis.ip}")
    public String redisIp;  //redis服务器ip
    
    @Value("${redis.port}")
    public int redisPort;  //redis接口
    
    @Value("${zookeeper.addrs}")
    public String zookeeperAddrs;  //zookeeper 地址，[ip:port,...]

    //TODO 以下都需要修改为common的静态变量
    @Value("${zkName.gameServer}")
    public String gameServerZKName;  //gameserver 在zookeeper的节点路径

    @Value("${zkName.gameDataServer}")
    public String gameDataServerZKName;  //gameDateServer 在zookeeper的节点路径

    //TODO 以下需要考虑修改为使用mysql或者redis
    @Value("${zkName.game}")
    public String gameZKName;  //game的节点路径
    
    @Value("${zkName.user}")
    public String userZKName;  //user的节点路径
		
}
