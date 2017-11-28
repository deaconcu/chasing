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

import com.prosper.chasing.common.bean.ThriftTransportPool;
import com.prosper.chasing.common.bean.client.ThriftClient;
import com.prosper.chasing.common.bean.client.ZkClient;
import com.prosper.chasing.common.bean.wrapper.NettyUDPServer;
import com.prosper.chasing.common.bean.wrapper.NettyWebSocketServer;
import com.prosper.chasing.common.bean.wrapper.ThriftRPCServer;
import com.prosper.chasing.common.bean.wrapper.ThriftRPCServer.Type;
import com.prosper.chasing.common.bean.wrapper.UDPService;
import com.prosper.chasing.common.bean.wrapper.WebSocketService;
import com.prosper.chasing.common.boot.RuntimeSpringBeans;

@Configuration
@EnableAspectJAutoProxy
@EnableScheduling
@PropertySources({
    @PropertySource("classpath:app.properties"),
    @PropertySource(value = "file:etc/chasing-connection.properties", ignoreResourceNotFound=true)
})
@ComponentScan(basePackages = {
        "com.prosper.chasing.common.bean.client",
        "com.prosper.chasing.connection"
})
@RuntimeSpringBeans(mode = "connection-server")
public class ConnectionServerBeans {

    // 加载配置的类，在@PropertySources注解上写配置路径
    @Bean(name="propertySources")
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    // redis客户端
    @Bean
    public Jedis jedis(Config config) {
        return new Jedis(config.redisIp, config.redisPort);
        //        Set<HostAndPort> jedisClusterNodes = new HashSet<HostAndPort>();
        //        jedisClusterNodes.add(new HostAndPort(ip, port));
        //        JedisCluster jc = new JedisCluster(jedisClusterNodes);
        //        return jc;
    }

    // zookeeper客户端
    @Bean
    public ZkClient zkClient(Config config) {
        return new ZkClient(config.zookeeperAddrs);
    }
    
//    @Bean
//    public ThriftRPCServer thriftRPCServer(Config config) {
//        return new ThriftRPCServer(config.appPackage, config.rpcPort);
//    }

    //TODO 线程池，貌似没用，可以去掉
    @Bean
    public ExecutorService executorService() {
        return Executors.newCachedThreadPool();
    }

    // netty的udp server，使用udp service
    @Bean
    public NettyUDPServer webUDPServer(Config config, UDPService UDPService) {
        return new NettyUDPServer(config.serverPort, UDPService);
    }

    // thrift的rpc server, 自动扫描rpc service
    @Bean
    public ThriftRPCServer thriftRPCServer(Config config) {
        return new ThriftRPCServer(config.appPackage, config.rpcPort, Type.TThreadPoolServer);
    }

    // thrift客户端
    @Bean
    public ThriftClient thriftClient() {
        return new ThriftClient();
    }

    // thrift的连接池
    @Bean
    public ThriftTransportPool thriftTransportPool() {
        return new ThriftTransportPool();
    }
    
}
