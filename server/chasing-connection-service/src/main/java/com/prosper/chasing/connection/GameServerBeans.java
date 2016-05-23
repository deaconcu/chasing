package com.prosper.chasing.connection;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import redis.clients.jedis.Jedis;

import com.prosper.chasing.common.bean.client.ZkClient;
import com.prosper.chasing.common.bean.wrapper.NettyWebSocketServer;
import com.prosper.chasing.common.bean.wrapper.ThriftRPCServer;
import com.prosper.chasing.common.bean.wrapper.WebSocketService;
import com.prosper.chasing.common.boot.RuntimeSpringBeans;

@Configuration
@EnableAspectJAutoProxy
@EnableScheduling
@PropertySources({
    @PropertySource("classpath:app.properties"),
    @PropertySource(value = "classpath:app.properties", ignoreResourceNotFound=true),
    @PropertySource(value = "file:config/app.properties", ignoreResourceNotFound=true)
})
@ComponentScan(basePackages = {
        "com.prosper.chasing.common.bean.client",
        "com.prosper.chasing.connection"
})
@RuntimeSpringBeans(mode = "connectionServer")
public class GameServerBeans {

    @Bean(name="propertySources")
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Bean
    public Jedis jedis(Config config) {
        return new Jedis(config.redisIp, config.redisPort);
        //        Set<HostAndPort> jedisClusterNodes = new HashSet<HostAndPort>();
        //        jedisClusterNodes.add(new HostAndPort(ip, port));
        //        JedisCluster jc = new JedisCluster(jedisClusterNodes);
        //        return jc;
    }
    
    @Bean
    public ZkClient zkClient(Config config) {
        return new ZkClient(config.zookeeperAddrs);
    }
    
    @Bean
    public ThriftRPCServer thriftRPCServer(Config config) {
        return new ThriftRPCServer(config.appPackage, config.rpcPort);
    }
    
    @Bean
    public NettyWebSocketServer webSocketServer(Config config, WebSocketService webSocketService) {
        return new NettyWebSocketServer(config.serverPort, false, webSocketService);
    }
    
    @Bean
    public ExecutorService executorService() {
        return Executors.newCachedThreadPool();
    }

}
