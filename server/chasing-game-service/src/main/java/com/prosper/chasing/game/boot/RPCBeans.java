package com.prosper.chasing.game.boot;

import java.beans.PropertyVetoException;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.mapper.MapperScannerConfigurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import redis.clients.jedis.Jedis;

import com.prosper.chasing.common.boot.RPCSpringRuntimeBeans;
import com.prosper.chasing.common.client.ZkClient;
import com.prosper.chasing.game.util.Config;

@Configuration
@EnableAspectJAutoProxy
@EnableTransactionManagement
@EnableScheduling
@PropertySources({
    @PropertySource("classpath:app.properties"),
    @PropertySource(value="classpath:app.properties", ignoreResourceNotFound=true),
    @PropertySource(value="file:config/app.properties", ignoreResourceNotFound=true)
})
@ComponentScan(basePackages = {
        "com.prosper.chasing.game"
})
@RPCSpringRuntimeBeans
public class RPCBeans {

    @Bean(name="propertySources")
    public PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
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

}
