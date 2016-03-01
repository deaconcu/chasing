package com.prosper.chasing.common.client;

import java.util.concurrent.CountDownLatch;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;

public class ZkClient {

    //private String hosts = "localhost:4180,localhost:4181,localhost:4182";  
    private String hosts;
    
    private static final int SESSION_TIMEOUT = 5000;  
    private CountDownLatch connectedSignal = new CountDownLatch(1);  
    protected ZooKeeper zk;
    
    public static ZkClient zkClient;
    
    public static ZkClient instance() {
        return zkClient;
    }
    
    public static void init(String hosts) throws RuntimeException {
        try {
            zkClient = new ZkClient(hosts);
            zkClient.connect();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public ZkClient(String hosts) {
        this.hosts = hosts;
    }

    public void connect() throws Exception {  
        zk = new ZooKeeper(hosts, SESSION_TIMEOUT, new ConnWatcher());  
        connectedSignal.await();  
    }

    public class ConnWatcher implements Watcher {  
        public void process(WatchedEvent event) {  
            if (event.getState() == KeeperState.SyncConnected) {  
                connectedSignal.countDown();  
            }  
        }  
    }

    public void create(String nodePath, byte[] data) throws Exception {  
        zk.create(nodePath, data, Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);  
    }  

    public boolean exists(String path) {
        try {
            if (zk.exists(path, false) == null) {
                return false;
            }
            return true;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public byte[] get(String path) {
        try {
            return zk.getData(path, false, null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }  
    }
    
    public void set(String path, byte[] data, int version) {
        try {
            zk.setData(path, data, version);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }  
    }

    public void delete(String path, int version) {
        try {
            zk.delete(path, version);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }  
    }
}
