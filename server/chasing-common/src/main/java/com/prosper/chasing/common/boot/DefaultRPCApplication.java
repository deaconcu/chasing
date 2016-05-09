package com.prosper.chasing.common.boot;

import java.lang.reflect.Constructor;
import java.util.Set;

import org.apache.thrift.TMultiplexedProcessor;
import org.apache.thrift.TProcessor;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.NoUniqueBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import com.prosper.chasing.common.boot.Application;
import com.prosper.chasing.common.boot.RPCSpringRuntimeBeans;
import com.prosper.chasing.common.client.ZkClient;
import com.prosper.chasing.common.util.Constant;

public abstract class DefaultRPCApplication implements Application {

    Logger log = LoggerFactory.getLogger(getClass());
    
    @Override
    public void run(String[] args){
        ClassPathScanningCandidateComponentProvider rpcServiceScanner =
                new ClassPathScanningCandidateComponentProvider(false);
        rpcServiceScanner.addIncludeFilter(new AnnotationTypeFilter(RPCService.class));
        Set<BeanDefinition> serviceBeanSet = rpcServiceScanner.findCandidateComponents(getPackage());
        if (serviceBeanSet.size() < 1) {
            throw new RuntimeException("there is no rpc service exist");
        }
        
        ClassPathScanningCandidateComponentProvider beansScanner =
                new ClassPathScanningCandidateComponentProvider(false);
        beansScanner.addIncludeFilter(new AnnotationTypeFilter(RPCSpringRuntimeBeans.class));
        Set<BeanDefinition> beanSet = beansScanner.findCandidateComponents(getPackage());
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
        
        log.info("Starting spring context ...");
        ApplicationContext context = new AnnotationConfigApplicationContext(new Class[]{beanClass});
        
        ZkClient zkClient = null;
        String ZKNodePath = "/" + Constant.RPCServerZkName + "/" + getName() + "/" + getIP() + ":" + getPort();
        try {
            zkClient = context.getBean(ZkClient.class);
        } catch(NoSuchBeanDefinitionException e) {
        }
        
        try {
            TMultiplexedProcessor processor = new TMultiplexedProcessor();
            for (BeanDefinition serviceBean: serviceBeanSet) {
                beanClass = Class.forName(serviceBean.getBeanClassName());
                Class<?>[] interfaces = beanClass.getInterfaces();
                if (interfaces.length != 1) {
                    throw new RuntimeException("the interface of service is not correct");
                }
                Class<?> serviceInterface = interfaces[0];
                Object beanObject = context.getBean(beanClass);
                RPCService rpcServiceAnnotation = beanClass.getAnnotation(RPCService.class);
                Class<? extends TProcessor> clazz = rpcServiceAnnotation.processorClass();
                Constructor<? extends TProcessor> constructor = clazz.getConstructor(serviceInterface);
                constructor.newInstance(beanObject);
                
                processor.registerProcessor(beanClass.getSimpleName(), constructor.newInstance(beanObject));
                log.info("add RPC service:" + beanClass.getSimpleName());
            }
            
            TServerSocket serverTransport = new TServerSocket(getPort());
            TServer server = new TThreadPoolServer(new TThreadPoolServer.Args(serverTransport).processor(processor));
            
            if (zkClient != null) {
                zkClient.create(ZKNodePath, "".getBytes());
            }
            log.info("Starting server on port 8811 ...");
            server.serve();
        } catch (Exception e) {
            if (zkClient != null) {
                try {
                    zkClient.delete(ZKNodePath, -1);
                } catch(RuntimeException re) {
                    log.warn("delete zookeeper node failed, path" + ZKNodePath);
                }
            }
            log.error("start RPC server failed", e);
        }
    }
}
