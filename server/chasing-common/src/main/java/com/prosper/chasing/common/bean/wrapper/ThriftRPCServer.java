package com.prosper.chasing.common.bean.wrapper;

import java.lang.reflect.Constructor;
import java.util.Set;

import org.apache.thrift.TMultiplexedProcessor;
import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.server.TNonblockingServer;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.server.TThreadedSelectorServer;
import org.apache.thrift.transport.TNonblockingServerSocket;
import org.apache.thrift.transport.TNonblockingServerTransport;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.type.filter.AnnotationTypeFilter;

public class ThriftRPCServer implements ApplicationListener<ContextRefreshedEvent>, ApplicationContextAware {

    private Logger log = LoggerFactory.getLogger(getClass());

    private ApplicationContext applicationContext;
    private String basePackage;
    private int port;
    private Type type;

    public enum Type {
        block, Nonblock
    }

    public ThriftRPCServer(String basePackage, int port, Type type) {
        this.basePackage = basePackage;
        this.port = port;
        this.type = type;
    }

    @Override
    public void onApplicationEvent(final ContextRefreshedEvent event) {
        ClassPathScanningCandidateComponentProvider rpcServiceScanner =
                new ClassPathScanningCandidateComponentProvider(false);
        rpcServiceScanner.addIncludeFilter(new AnnotationTypeFilter(ThriftRPCService.class));
        Set<BeanDefinition> serviceBeanSet = rpcServiceScanner.findCandidateComponents(basePackage);
        if (serviceBeanSet.size() < 1) {
            throw new RuntimeException("there is no rpc service exist");
        } else if (serviceBeanSet.size() == 1) {
            try {
                BeanDefinition serviceBean = (BeanDefinition)serviceBeanSet.toArray()[0];
                Class<?> beanClass = Class.forName(serviceBean.getBeanClassName());
                Class<?>[] interfaces = beanClass.getInterfaces();
                if (interfaces.length != 1) {
                    throw new RuntimeException("the interface of service is not correct");
                }
                Class<?> serviceInterface = interfaces[0];
                Object beanObject = applicationContext.getBean(beanClass);
                ThriftRPCService rpcServiceAnnotation = beanClass.getAnnotation(ThriftRPCService.class);
                Class<? extends TProcessor> clazz = rpcServiceAnnotation.processorClass();
                Constructor<? extends TProcessor> constructor = clazz.getConstructor(serviceInterface);
                constructor.newInstance(beanObject);

                final TServer server;
                if (type == Type.Nonblock) {
                    TNonblockingServerTransport serverTransport = new TNonblockingServerSocket(port);
                    server = new TThreadedSelectorServer(
                            new TThreadedSelectorServer.Args(serverTransport)
                            .processor(constructor.newInstance(beanObject))
                            .protocolFactory(new TBinaryProtocol.Factory()));

                } else {
                    TServerTransport serverTransport = new TServerSocket(port);
                    server = new TThreadPoolServer(
                            new TThreadPoolServer.Args(serverTransport)
                            .processor(constructor.newInstance(beanObject))
                            .protocolFactory(new TBinaryProtocol.Factory())
                            .minWorkerThreads(2).maxWorkerThreads(5));
                }

                log.info("starting server on port " + port + " ...");
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
        } else {
            try {
                TMultiplexedProcessor processor = new TMultiplexedProcessor();
                for (BeanDefinition serviceBean: serviceBeanSet) {
                    Class<?> beanClass = Class.forName(serviceBean.getBeanClassName());
                    Class<?>[] interfaces = beanClass.getInterfaces();
                    if (interfaces.length != 1) {
                        throw new RuntimeException("the interface of service is not correct");
                    }
                    Class<?> serviceInterface = interfaces[0];
                    Object beanObject = applicationContext.getBean(beanClass);
                    ThriftRPCService rpcServiceAnnotation = beanClass.getAnnotation(ThriftRPCService.class);
                    Class<? extends TProcessor> clazz = rpcServiceAnnotation.processorClass();
                    Constructor<? extends TProcessor> constructor = clazz.getConstructor(serviceInterface);
                    constructor.newInstance(beanObject);

                    processor.registerProcessor(beanClass.getSimpleName(), constructor.newInstance(beanObject));
                    log.info("add RPC service:" + beanClass.getSimpleName());
                }
                
                final TServer server;
                if (type == Type.Nonblock) {
                    TNonblockingServerTransport serverTransport = new TNonblockingServerSocket(port);
                    server = new TThreadedSelectorServer(
                            new TThreadedSelectorServer.Args(serverTransport)
                            .processor(processor)
                            .protocolFactory(new TBinaryProtocol.Factory()));
                } else {
                    TServerTransport serverTransport = new TServerSocket(port);
                    server = new TThreadPoolServer(
                            new TThreadPoolServer.Args(serverTransport)
                            .processor(processor)
                            .protocolFactory(new TBinaryProtocol.Factory())
                            .minWorkerThreads(2).maxWorkerThreads(5));
                }

                log.info("starting server on port " + port + " ...");
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
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext)
            throws BeansException {
        this.applicationContext = applicationContext;
    }



}
