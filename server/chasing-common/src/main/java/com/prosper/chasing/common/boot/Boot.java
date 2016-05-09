package com.prosper.chasing.common.boot;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;

import com.prosper.chasing.common.exception.SpringExceptionHandlers;

public class Boot {
	
	private static final Logger log = LoggerFactory.getLogger(SpringExceptionHandlers.class);

	public static void main(String[] args) {
		String name = System.getProperty("name");
		if (name == null) {
			throw new RuntimeException("name is empty");
		}

		log.info("starting application, please wait ...");
		ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
		scanner.addIncludeFilter(new AssignableTypeFilter(Application.class));
		Set<BeanDefinition> beanSet = scanner.findCandidateComponents("com.prosper.chasing");
		if (beanSet.size() < 1) {
			throw new RuntimeException("no application class exist");
		}
		
		Application application = null;
		for (BeanDefinition bootBeanDefinition: beanSet) {
	        try {
	            Class<?> clazz = Class.forName(bootBeanDefinition.getBeanClassName());
	            application = (Application)clazz.newInstance();
	        } catch (Exception e) {
	            throw new RuntimeException("cannot create application object, exception:" + e.getMessage());
	        }
	        if (application.getName() != null && application.getName().equals(name)) {
	            break;
	        } else {
	            application = null;
	            continue;
	        }
		}
        if (application == null) {
            throw new RuntimeException("no appliction support name: " + name);
        }
        application.run(args);
	}
}