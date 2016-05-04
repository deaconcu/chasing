package com.prosper.chasing.common;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import com.prosper.chasing.common.exception.SpringExceptionHandlers;
import com.prosper.chasing.common.runtime.base.DefaultBatchBeans;
import com.prosper.chasing.common.runtime.base.DefaultBeans;
import com.prosper.chasing.common.runtime.base.DefaultCronBeans;
import com.prosper.chasing.common.runtime.base.DefaultHttpBeans;
import com.prosper.chasing.common.runtime.base.EnableBatch;
import com.prosper.chasing.common.runtime.base.EnableCron;
import com.prosper.chasing.common.runtime.base.EnableHttp;

public class Application {
	
	private static final Logger log = LoggerFactory.getLogger(SpringExceptionHandlers.class);

	private static ApplicationContext applicationContext;

	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		String mod = System.getProperty("mode");
		if (mod == null) {
			throw new RuntimeException("mode is empty");
		}

		log.info("starting application, please wait ...");
		Class<? extends Annotation> runtimeBeanClass = null;
		if (mod.equals("http")) {
			runtimeBeanClass = EnableHttp.class;
		} else if (mod.equals("cron")) {
			runtimeBeanClass = EnableCron.class;
		} else if (mod.equals("batch")) {
			runtimeBeanClass = EnableBatch.class;
		} else {
			throw new RuntimeException("mod is not supported, mod:" + mod);
		}
		
		ClassPathScanningCandidateComponentProvider scanner =
		        new ClassPathScanningCandidateComponentProvider(false);
		scanner.addIncludeFilter(new AnnotationTypeFilter(runtimeBeanClass));
		Set<BeanDefinition> beanSet = scanner.findCandidateComponents("com.prosper.chasing");
		if (beanSet.size() != 1) {
			throw new RuntimeException("there is no runtime bean or more than one runtime bean, "
					+ "mode:" + mod + ", bean set size:" + beanSet.size());
		}
		BeanDefinition bd = (BeanDefinition)beanSet.toArray()[0];
		Class<?> beanClass = null;
		try {
			beanClass = Class.forName(bd.getBeanClassName());
		} catch (ClassNotFoundException e1) {
			throw new RuntimeException("can't get class:" + bd.getBeanClassName());
		}
		
		ApplicationContext context = null;
		if (mod.equals("http")) {
			MDC.put("logFileName", "http");
			context = new SpringApplicationBuilder(DefaultBeans.class, DefaultHttpBeans.class, beanClass).showBanner(false).run();
//			context = SpringApplication.run(new Class[]{beanClass, CommonBeans.class});
		} else if (mod.equals("cron")) {
			MDC.put("logFileName", "cron");
			context = new AnnotationConfigApplicationContext(new Class[]{DefaultBeans.class, DefaultCronBeans.class, beanClass});
		} else if (mod.equals("batch")) {
			MDC.put("logFileName", "batch");
			context = new AnnotationConfigApplicationContext(new Class[]{DefaultBeans.class, DefaultBatchBeans.class, beanClass});
			String beanName = args[0];
			String functionName = args[1];
			Object[] functionArgs = Arrays.copyOfRange(args, 2, args.length);

			try {
				Object bean = context.getBean(beanName);
				if (bean == null) {
					throw new RuntimeException("bean is not exist");
				}
				
				Class<String>[] argClasses = new Class[functionArgs.length];
				Arrays.fill(argClasses, String.class);
				Method method = bean.getClass().getDeclaredMethod(functionName, argClasses);
				if (method == null) {
					throw new RuntimeException("method is not exist");
				}
				try {
					method.invoke(bean, functionArgs);
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					throw new RuntimeException("invoke method failed", e);
				}
			} catch (NoSuchMethodException | SecurityException e) {
				throw new RuntimeException("method is not exist", e);
			} finally {
				((AnnotationConfigApplicationContext)context).close();
			}
		} else {
			throw new RuntimeException("mod is not supported");
		}

		//System.out.println(Arrays.toString(context.getBeanDefinitionNames()));
		setApplicationContext(context);
	}
	
	public static ApplicationContext getApplicationContext() {
		return applicationContext;
	}

	public static void setApplicationContext(ApplicationContext applicationContext) {
		Application.applicationContext = applicationContext;
	}
	
}