package com.prosper.chasing.common.boot;

import java.util.Set;

import org.slf4j.MDC;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import com.prosper.chasing.common.runtime.base.DefaultBeans;
import com.prosper.chasing.common.runtime.base.DefaultCronBeans;

public abstract class DefaultCronApplication extends Application {

    @Override
    public void execute(String[] args) {
        ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(HttpSpringRuntimeBeans.class));
        Set<BeanDefinition> beanSet = scanner.findCandidateComponents("com.prosper.chasing");
        if (beanSet.size() != 1) {
            throw new RuntimeException(
                    "there is no runtime bean or more than one runtime bean, count:" + beanSet.size());
        }
        BeanDefinition bd = (BeanDefinition)beanSet.toArray()[0];
        Class<?> beanClass = null;
        try {
            beanClass = Class.forName(bd.getBeanClassName());
        } catch (ClassNotFoundException e1) {
            throw new RuntimeException("can't get class:" + bd.getBeanClassName());
        }
        
        MDC.put("logFileName", "cron");
        
        @SuppressWarnings("unused")
        ApplicationContext context = new AnnotationConfigApplicationContext(
                new Class[]{DefaultBeans.class, DefaultCronBeans.class, beanClass});
    }
    
}
