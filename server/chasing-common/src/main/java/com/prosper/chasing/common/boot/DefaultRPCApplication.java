package com.prosper.chasing.common.boot;

import java.lang.reflect.Constructor;
import java.util.Set;

import org.apache.thrift.TMultiplexedProcessor;
import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TSimpleServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
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
import com.prosper.chasing.common.util.CommonConstant;

public abstract class DefaultRPCApplication extends Application {

    Logger log = LoggerFactory.getLogger(getClass());
    
    private ApplicationContext applicationContext;
    
    private int serverPort;
    
    /**
     * 设置启动端口
     */
    public abstract void loadPort();
    
    /**
     * base package，用来扫描需要的类
     */
    public abstract String getPackage();
    
    @Override
    public void execute(String[] args){
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
        applicationContext = new AnnotationConfigApplicationContext(new Class[]{beanClass});
        
        loadPort();
        try {
            TMultiplexedProcessor processor = new TMultiplexedProcessor();
            for (BeanDefinition serviceBean: serviceBeanSet) {
                beanClass = Class.forName(serviceBean.getBeanClassName());
                Class<?>[] interfaces = beanClass.getInterfaces();
                if (interfaces.length != 1) {
                    throw new RuntimeException("the interface of service is not correct");
                }
                Class<?> serviceInterface = interfaces[0];
                Object beanObject = applicationContext.getBean(beanClass);
                RPCService rpcServiceAnnotation = beanClass.getAnnotation(RPCService.class);
                Class<? extends TProcessor> clazz = rpcServiceAnnotation.processorClass();
                Constructor<? extends TProcessor> constructor = clazz.getConstructor(serviceInterface);
                constructor.newInstance(beanObject);
                
                processor.registerProcessor(beanClass.getSimpleName(), constructor.newInstance(beanObject));
                log.info("add RPC service:" + beanClass.getSimpleName());
            }
            TServerTransport serverTransport = new TServerSocket(getServerPort());
            final TServer server = new TThreadPoolServer(
                    new TThreadPoolServer
                    .Args(serverTransport)
                    .processor(processor)
                    .protocolFactory(new TBinaryProtocol.Factory()));
            
            log.info("starting server on port " + getServerPort() + " ...");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    server.serve();
                }
            }).start();
            
            while(server.isServing()) {
                Thread.sleep(100);
            }
            log.info("server started");
        } catch (Exception e) {
            log.error("start RPC server failed", e);
        }
    }
    
    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public int getServerPort() {
        return serverPort;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

}
