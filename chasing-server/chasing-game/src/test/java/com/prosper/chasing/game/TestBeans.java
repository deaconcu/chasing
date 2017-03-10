package com.prosper.chasing.game;

import com.prosper.chasing.common.bean.ThriftTransportPool;
import com.prosper.chasing.common.bean.client.ThriftClient;
import com.prosper.chasing.common.bean.client.ZkClient;
import org.springframework.context.annotation.*;

public class TestBeans {

    @Bean
    public ZkClient zkClient() {
        return new ZkClient("120.27.112.99:2181");
    }

    @Bean
    public ThriftClient thriftClient() {
        return new ThriftClient();
    }
    
    @Bean
    public ThriftTransportPool thriftTransportPool() {
        return new ThriftTransportPool();
    }
    
}
