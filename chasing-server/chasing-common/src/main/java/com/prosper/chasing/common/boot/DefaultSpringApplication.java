package com.prosper.chasing.common.boot;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import com.prosper.chasing.common.util.Args;

public class DefaultSpringApplication extends Application {
    
    private Logger log = LoggerFactory.getLogger(getClass());

    @Override
    public void execute(String[] args) {
        String mode = System.getProperty("mode");
        
        if (mode == null) {
            throw new RuntimeException("no mode name provided");
        }
        
        Args.args = args;
        
        ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(RuntimeSpringBeans.class));
        Set<BeanDefinition> beanDefinitionSet = scanner.findCandidateComponents("com.prosper.chasing");
        Class<?> beanClass = null;
        boolean withWeb = false;
        for (BeanDefinition beanDefinition: beanDefinitionSet) {
            try {
                beanClass = Class.forName(beanDefinition.getBeanClassName());
            } catch (ClassNotFoundException e) {
                log.warn("class load failed: " + beanClass.getName());
            }
            RuntimeSpringBeans an = beanClass.getAnnotation(RuntimeSpringBeans.class);
            if (mode.equals(an.mode())) {
                withWeb = an.withWeb();
                break;
            }
        }
        
        if (beanClass == null) {
            throw new RuntimeException("cannot find class match with mode provided, mode:" + mode);
        }
        
        MDC.put("logFileName", mode);
        new SpringApplicationBuilder(beanClass).web(withWeb).showBanner(false).run();
    }

    @Override
    public void beforeExecute(String[] args) {
    }

    @Override
    public void afterExecute(String[] args) {
    }
    
}
