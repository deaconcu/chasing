package com.prosper.chasing.common.bean.client;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.KeeperException.NodeExistsException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;

public class ZkClient {

    private static final int SESSION_TIMEOUT = 5000;  

    /**
     * eg:hosts = "localhost:4180,localhost:4181,localhost:4182";
     */
    private String hosts;
    protected ZooKeeper zk;

    public ZkClient(String hosts) {
        this.hosts = hosts;
        try {
            CountDownLatch connectedSignal = new CountDownLatch(1);  
            zk = new ZooKeeper(hosts, SESSION_TIMEOUT, new ConnWatcher(connectedSignal));  
            connectedSignal.await();  
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public class ConnWatcher implements Watcher {
        private CountDownLatch connectedSignal;
        public ConnWatcher(CountDownLatch connectedSignal) {
            this.connectedSignal = connectedSignal;
        }
        public void process(WatchedEvent event) {  
            if (event.getState() == KeeperState.SyncConnected) {  
                connectedSignal.countDown();  
            }
        }
    }

    public void createNode(String nodePath, byte[] data, CreateMode mode, boolean override) {  
        try {
            try {
                zk.create(nodePath, data, Ids.OPEN_ACL_UNSAFE, mode);  
            } catch (NodeExistsException ke) {
                if (override) {
                    zk.setData(nodePath, data, -1);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void createPath(String nodePath, CreateMode mode) {  
        try {
            if (!nodePath.startsWith("/")) {
                throw new RuntimeException("node path must start with '/'");
            }
            String[] paths = nodePath.substring(1).split("/");
            String path = "";
            for (String nextPath: paths) {
                path = path + "/" + nextPath;
                if (zk.exists(path, false) == null) {
                    try {
                        zk.create(path, null, Ids.OPEN_ACL_UNSAFE, mode);  
                    } catch (NodeExistsException ke) {
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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

    public byte[] get(String path, boolean watch) {
        try {
            return zk.getData(path, watch, null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }  
    }

    public List<String> getChild(String path, boolean watch) {
        try {
            return zk.getChildren(path, false);
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
