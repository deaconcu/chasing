package com.prosper.chasing.data.boot;

import java.beans.PropertyVetoException;
import java.sql.SQLException;

import javafx.beans.DefaultProperty;

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

import com.prosper.chasing.common.bean.client.ZkClient;
import com.prosper.chasing.common.bean.wrapper.ThriftRPCServer;
import com.prosper.chasing.common.bean.wrapper.ThriftRPCServer.Type;
import com.prosper.chasing.common.boot.RuntimeSpringBeans;
import com.prosper.chasing.data.util.Config;

@Configuration
@EnableAspectJAutoProxy
@EnableTransactionManagement
@EnableScheduling
@PropertySources({
    @PropertySource("classpath:app.properties"),
    @PropertySource(value="classpath:application.properties", ignoreResourceNotFound=true),
    @PropertySource(value="file:config/application.properties", ignoreResourceNotFound=true)
})
@ComponentScan(basePackages = {
        "com.prosper.chasing.common.bean.client",
        "com.prosper.chasing.data"
})
@RuntimeSpringBeans(mode = "dataRPCServer")
public class RPCBeans {

    @Bean(name="propertySources")
    public PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Bean(name="dataSource")
    public DataSource dataSource(Config config) throws SQLException {
        DriverManagerDataSource instance = new DriverManagerDataSource();
        instance.setDriverClassName("com.mysql.jdbc.Driver");
        instance.setUrl("jdbc:mysql://" + config.dbIp + ":" + config.dbPort + "/" + config.dbName
                +"?useUnicode=true&characterEncoding=utf8");
        instance.setUsername(config.dbUserName);                                  
        instance.setPassword(config.dbPassword);
        instance.getConnection();
        return instance;
    }

    @Bean(name="sqlSessionFactory")
    public SqlSessionFactoryBean sqlSessionFactory(@Qualifier("dataSource") DataSource dataSource) 
            throws PropertyVetoException, SQLException {
        SqlSessionFactoryBean instance = new SqlSessionFactoryBean();
        instance.setDataSource(dataSource);
        instance.setConfigLocation(new ClassPathResource("mybatis.xml"));
        return instance;
    }

    @Bean
    public MapperScannerConfigurer mapperScannerConfigurer() {
        MapperScannerConfigurer configurer = new MapperScannerConfigurer();
        configurer.setSqlSessionFactoryBeanName("sqlSessionFactory");
        configurer.setBasePackage("com.prosper.chasing.data.mapper");
        return configurer;
    }

    @Bean
    public DataSourceTransactionManager transactionManager(@Qualifier("dataSource") DataSource dataSource)
            throws PropertyVetoException, SQLException {
        return new DataSourceTransactionManager(dataSource);
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
        return new ThriftRPCServer(config.appPackage, config.rpcPort, Type.block);
    }

}
