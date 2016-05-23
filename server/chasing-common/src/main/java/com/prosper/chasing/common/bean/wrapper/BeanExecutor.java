package com.prosper.chasing.common.bean.wrapper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;

import com.prosper.chasing.common.util.Args;

public class BeanExecutor implements ApplicationListener<ContextRefreshedEvent>, ApplicationContextAware {
    
    private Logger log = LoggerFactory.getLogger(getClass());
    
    private ApplicationContext applicationContext;
    
    @Override
    public void onApplicationEvent(final ContextRefreshedEvent event) {
        String[] args = Args.args;
        MDC.put("logFileName", "batch");
        String beanName = args[0];
        String functionName = args[1];
        Object[] functionArgs = Arrays.copyOfRange(args, 2, args.length);

        try {
            Object bean = applicationContext.getBean(beanName);
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
            ((ConfigurableApplicationContext)applicationContext).close();
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext)
            throws BeansException {
        this.applicationContext = applicationContext;
    }

    
    
}
