package com.prosper.chasing.common.runtime.base;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.embedded.jetty.JettyEmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.jetty.JettyServerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableAutoConfiguration
@EnableScheduling
@PropertySource("classpath:common-config.properties")
@ComponentScan(basePackages = {"com.youku.java.raptor.controll"})
public class DefaultHttpBeans {

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
	
}
