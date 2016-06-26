package com.prosper.chasing.http.runtime;

import java.beans.PropertyVetoException;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import javax.sql.DataSource;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.mapper.MapperScannerConfigurer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.embedded.jetty.JettyEmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.jetty.JettyServerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import redis.clients.jedis.Jedis;

import com.prosper.chasing.common.bean.client.ZkClient;
import com.prosper.chasing.common.boot.RuntimeSpringBeans;
import com.prosper.chasing.http.util.Config;

@Configuration
@EnableAutoConfiguration
@EnableAspectJAutoProxy
@EnableTransactionManagement
@EnableScheduling
@PropertySources({
    @PropertySource("classpath:app.properties"),
    @PropertySource(value = "classpath:app.properties", ignoreResourceNotFound=true),
    @PropertySource(value = "file:config/app.properties", ignoreResourceNotFound=true)
})
@ComponentScan(basePackages = {
        "com.prosper.chasing.common.bean.client",
        "com.prosper.chasing.data.mapper",
        "com.prosper.chasing.data.service",
        "com.prosper.chasing.data.exception",
        "com.prosper.chasing.data.util",
        "com.prosper.chasing.http.controll",
        "com.prosper.chasing.http.util",
        "com.prosper.chasing.http.validation",
        "com.prosper.chasing.http.aspect"
})
@RuntimeSpringBeans(mode = "httpServer", withWeb = true)
public class HttpBeans {

    @Bean(name="dataSource")
    public DataSource dataSource(Config config) throws SQLException {
        DriverManagerDataSource instance = new DriverManagerDataSource();
        instance.setDriverClassName("com.mysql.jdbc.Driver");
        instance.setUrl("jdbc:mysql://" + config.dbIp + ":" + config.dbPort + "/" + config.dbName
                +"?useUnicode=true&characterEncoding=utf8");
        System.out.println(instance.getUrl());
        instance.setUsername(config.dbUserName);                                  
        instance.setPassword(config.dbPassword);
        instance.getConnection();
        return instance;
    }

    @Bean(name="sqlSessionFactory")	
    @DependsOn("dataSource")
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
    public Jedis jedis(@Value("${redis.ip}") String ip, @Value("${redis.port}") int port) {
        return new Jedis(ip, port);
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
    public JettyEmbeddedServletContainerFactory jettyEmbeddedServletContainerFactory(
            @Value("${server.port:8080}") final String port,
            @Value("${jetty.threadPool.maxThreads:100}") final String maxThreads,
            @Value("${jetty.threadPool.minThreads:10}") final String minThreads,
            @Value("${jetty.threadPool.idleTimeout:60000}") final String idleTimeout) {
        final JettyEmbeddedServletContainerFactory factory =  
                new JettyEmbeddedServletContainerFactory(Integer.valueOf(port));
        factory.addServerCustomizers(new JettyServerCustomizer() {
            @Override
            public void customize(final Server server) {
                // Tweak the connection pool used by Jetty to handle incoming HTTP connections
                final QueuedThreadPool threadPool = server.getBean(QueuedThreadPool.class);
                threadPool.setMaxThreads(Integer.valueOf(maxThreads));
                threadPool.setMinThreads(Integer.valueOf(minThreads));
                threadPool.setIdleTimeout(Integer.valueOf(idleTimeout));
            }
        });
        return factory;
    }
    
    @Bean
    public PropertySourcesPlaceholderConfigurer getPropertySourcesPlaceholderConfigurer() {
        PropertySourcesPlaceholderConfigurer source = new PropertySourcesPlaceholderConfigurer();
        return source;
    }
    
}
