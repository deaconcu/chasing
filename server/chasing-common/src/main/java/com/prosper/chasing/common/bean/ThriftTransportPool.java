package com.prosper.chasing.common.bean;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

public class ThriftTransportPool {

    private Map<String, ObjectPool<TTransport>> poolMap;

    public ThriftTransportPool() {
        poolMap = new HashMap<String, ObjectPool<TTransport>>();
    }

    public TTransport borrowObject(String ip, Integer port) {
        String addr = ip + ":" + port;
        if (!poolMap.containsKey(addr)) {
            ObjectPool<TTransport> transportPool = new GenericObjectPool<>(new TTransportFactory(ip, port));
            poolMap.put(addr, transportPool);
        } 
        try {
            return poolMap.get(addr).borrowObject();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public void returnObject(String ip, Integer port, TTransport transport) {
        String addr = ip + ":" + port;
        if (!poolMap.containsKey(addr)) {
            ObjectPool<TTransport> transportPool = new GenericObjectPool<>(new TTransportFactory(ip, port));
            poolMap.put(addr, transportPool);
        } 
        try {
            poolMap.get(addr).returnObject(transport);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public class TTransportFactory extends BasePooledObjectFactory<TTransport> {
        
        private String ip;
        private Integer port;
        
        public TTransportFactory(String ip, Integer port) {
            this.ip = ip;
            this.port = port;
        }

        @Override
        public TTransport create() {
            try {
                TTransport transport =  new TSocket(ip, port);
                transport.open();
                return transport;
            } catch (TTransportException e) {
                throw new RuntimeException("create transport failed", e);
            }
        }
        
        @Override
        public void destroyObject(PooledObject<TTransport> pooledObject) {
            if (pooledObject.getObject().isOpen()) {
                pooledObject.getObject().close();
            }
        }
        
        @Override  
        public boolean validateObject(PooledObject<TTransport> pooledObject) {  
            try { 
                TTransport transport = pooledObject.getObject();
                if (transport instanceof TSocket) {  
                    TSocket thriftSocket = (TSocket) transport;  
                    if (thriftSocket.isOpen()) {  
                        return true;  
                    } else {  
                        return false;  
                    }  
                } else {  
                    return false;  
                }  
            } catch (Exception e) {  
                return false;  
            }  
        }

        @Override
        public PooledObject<TTransport> wrap(TTransport transport) {
            return new DefaultPooledObject<TTransport>(transport);
        }

        @Override
        public void passivateObject(PooledObject<TTransport> pooledObject) {
        }

    }
}
