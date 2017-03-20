package com.prosper.chasing.connection;

import com.prosper.chasing.common.bean.ThriftTransportPool;
import com.prosper.chasing.common.bean.client.ThriftClient;
import com.prosper.chasing.common.bean.client.ZkClient;
import com.prosper.chasing.common.bean.wrapper.NettyUDPServer;
import com.prosper.chasing.common.bean.wrapper.ThriftRPCServer;
import com.prosper.chasing.common.bean.wrapper.UDPService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import redis.clients.jedis.Jedis;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@ComponentScan(basePackages = {
        "com.prosper.chasing.common.bean.client",
        "com.prosper.chasing.connection"
})
public class TestBeans {

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
    public ZkClient zkClient() {
        return new ZkClient("120.27.112.99:2181");
    }

    @Bean
    public ExecutorService executorService() {
        return Executors.newCachedThreadPool();
    }

    @Bean
    public NettyUDPServer webUDPServer(Config config, UDPService UDPService) {
        return new NettyUDPServer(config.serverPort, UDPService);
    }

    @Bean
    public ThriftRPCServer thriftRPCServer(Config config) {
        return new ThriftRPCServer(config.appPackage, config.rpcPort, ThriftRPCServer.Type.TThreadedSelectorServer);
    }

    @Bean
    public ThriftClient thriftClient() {
        return new ThriftClient();
    }

    @Bean
    public ThriftTransportPool thriftTransportPool() {
        return new ThriftTransportPool();
    }

}
