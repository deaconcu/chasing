package com.prosper.game.http.runtime;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.embedded.jetty.JettyEmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.jetty.JettyServerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.bind.annotation.ControllerAdvice;

@Configuration
@EnableAspectJAutoProxy
@EnableTransactionManagement
@ComponentScan(
		basePackages = {
				"com.youku.java.raptor.auth", 
				"com.youku.java.raptor.aspect", 
				"com.youku.java.raptor.client", 
				"com.youku.java.raptor.util", 
				"com.youku.java.raptor.exception", 
				"com.youku.java.raptor.validation"
		},
		includeFilters = @ComponentScan.Filter(
				value= ControllerAdvice.class, type = FilterType.ANNOTATION))
public class DefaultBeans {

}
