package com.prosper.chasing.common.boot;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;

import org.slf4j.MDC;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import com.prosper.chasing.common.runtime.base.DefaultBatchBeans;
import com.prosper.chasing.common.runtime.base.DefaultBeans;

public abstract class DefaultBatchApplication extends Application {

    @Override
    public void execute(String[] args) {
        ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(RuntimeSpringBeans.class));
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
        
        MDC.put("logFileName", "batch");
        ApplicationContext context = new AnnotationConfigApplicationContext(
                new Class[]{DefaultBeans.class, DefaultBatchBeans.class, beanClass});
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
        
    }
    
}
