package com.prosper.chasing.game;

import com.prosper.chasing.common.bean.ThriftTransportPool;
import com.prosper.chasing.common.bean.client.ThriftClient;
import com.prosper.chasing.common.bean.client.ZkClient;
import com.prosper.chasing.common.bean.wrapper.ThriftRPCServer;
import com.prosper.chasing.game.util.Config;
import org.springframework.context.annotation.*;
import redis.clients.jedis.Jedis;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@ComponentScan(basePackages = {
        "com.prosper.chasing.common.bean.client",
        "com.prosper.chasing.game"
})
public class TestBeans {

    @Bean
    public Jedis jedis(Config config) {
        return new Jedis(config.redisIp, config.redisPort);
    }

    @Bean
    public ThriftRPCServer thriftRPCServer(Config config) {
        return new ThriftRPCServer(config.appPackage, config.rpcPort, ThriftRPCServer.Type.TThreadPoolServer);
    }

    @Bean
    public ExecutorService executorService() {
        // TODO
//        return Executors.newCachedThreadPool();
        return Executors.newFixedThreadPool(1);
    }


    @Bean
    public ZkClient zkClient() {
        return new ZkClient("120.27.112.99:2181");
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
