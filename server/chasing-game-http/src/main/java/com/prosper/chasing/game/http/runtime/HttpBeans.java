package com.prosper.chasing.game.http.runtime;

import java.beans.PropertyVetoException;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.mapper.MapperScannerConfigurer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.prosper.chasing.game.http.runtime.base.EnableHttp;
import com.prosper.chasing.game.http.util.Config;

@EnableHttp
@ComponentScan(basePackages = {
		"com.prosper.chasing.game.http.mapper",
		"com.prosper.chasing.game.http.controll",
		"com.prosper.chasing.game.http.service",
		"com.prosper.chasing.game.http.util",
		"com.prosper.chasing.game.http.validation",
		"com.prosper.chasing.game.http.aspect",
		"com.prosper.chasing.game.http.exception"
})
public class HttpBeans {
	
	@DependsOn("config")
	@Bean(name="dataSource")
	public DataSource dataSource(@Qualifier("config") Config config) throws SQLException {
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
		configurer.setBasePackage("com.prosper.chasing.game.http.mapper");
		return configurer;
	}
	
	@Bean
	public DataSourceTransactionManager transactionManager(@Qualifier("dataSource") DataSource dataSource)
			throws PropertyVetoException, SQLException {
		return new DataSourceTransactionManager(dataSource);
	}
	
}
