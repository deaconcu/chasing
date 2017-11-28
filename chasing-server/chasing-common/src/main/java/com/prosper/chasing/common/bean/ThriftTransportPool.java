package com.prosper.chasing.common.bean;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.thrift.transport.TNonblockingSocket;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StopWatch;

public class ThriftTransportPool {

    private Logger log = LoggerFactory.getLogger(getClass());

    public enum Type {
        tSocket, tNonblockingSocket
    }

    private Map<String, ObjectPool<TTransport>> poolMap;

    public ThriftTransportPool() {
        poolMap = new HashMap<String, ObjectPool<TTransport>>();
    }

    public TTransport borrowObject(String ip, Integer port) {
        return borrowObject(ip, port, Type.tSocket);
    }

    public TTransport borrowObject(String ip, Integer port, Type type) {
        String key = ip + ":" + port + ":" + type.name();
        if (!poolMap.containsKey(key)) {
            GenericObjectPool<TTransport> transportPool = new GenericObjectPool<>(new TTransportFactory(ip, port, type));
//            transportPool.setTestOnBorrow(true);
            poolMap.put(key, transportPool);
        }
        TTransport transport = null;
        try {
            transport = poolMap.get(key).borrowObject();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return transport;
    }

    public void removeObject(String ip, Integer port, TTransport transport) {
        removeObject(ip, port, Type.tSocket, transport);
    }
    
    public void removeObject(String ip, Integer port, Type type, TTransport transport) {
        try {
            String addr = ip + ":" + port + ":" + type.name();
            if (poolMap.containsKey(addr)) {
                transport.close();
                poolMap.get(addr).invalidateObject(transport);
            } 
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void returnObject(String ip, Integer port, TTransport transport) {
        returnObject(ip, port, Type.tSocket, transport);
    }

    public void returnObject(String ip, Integer port, Type type, TTransport transport) {
        String addr = ip + ":" + port + ":" + type.name();
        if (!poolMap.containsKey(addr)) {
            ObjectPool<TTransport> transportPool = new GenericObjectPool<>(new TTransportFactory(ip, port, type));
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
        private Type type;

        public TTransportFactory(String ip, Integer port, Type type) {
            this.ip = ip;
            this.port = port;
            this.type = type;
        }

        @Override
        public TTransport create() {
            try {
                TTransport transport = null;
                if (type == Type.tSocket) {
                    transport =  new TSocket(ip, port);
                    transport.open();
                } else if (type == Type.tNonblockingSocket){
                    TNonblockingSocket tNonblockingSocket = new TNonblockingSocket(ip, port);
                    tNonblockingSocket.open();
                    transport = (TTransport) tNonblockingSocket;
                }
                if (transport != null) {
                    log.info("create transport successfully from factory");
                    return transport;
                }
                throw new RuntimeException("transport create error");
            } catch (Exception e) {
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
                if (transport instanceof TSocket || transport instanceof TNonblockingSocket) {  
                    TSocket tSocket = (TSocket) transport;
                    //                    tSocket.getSocket().getOutputStream().write(1);
                    return true;  
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
