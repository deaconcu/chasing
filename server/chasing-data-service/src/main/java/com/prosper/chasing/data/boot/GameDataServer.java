package com.prosper.chasing.data.boot;

import org.apache.zookeeper.CreateMode;
import org.springframework.context.ApplicationContext;

import com.prosper.chasing.common.boot.DefaultRPCApplication;
import com.prosper.chasing.common.client.ZkClient;
import com.prosper.chasing.data.util.Config;
import com.prosper.chasing.data.util.Constant;

public class GameDataServer extends DefaultRPCApplication {

    @Override
    public String getPackage() {
        return "com.prosper.chasing.data";
    }

    @Override
    public void loadPort() {
        ApplicationContext applicationContext = getApplicationContext();
        Config config = applicationContext.getBean(Config.class);
        setServerPort(config.serverPort);
    }
    
    @Override
    public void beforeExecute(String[] args) {
    }

    @Override
    public void afterExecute(String[] args) {
        ApplicationContext applicationContext = getApplicationContext();
        ZkClient zkClient = applicationContext.getBean(ZkClient.class);
        Config config = applicationContext.getBean(Config.class);

        zkClient.createPath(Constant.SERVE_LIST_ZK_NAME, CreateMode.PERSISTENT);
        zkClient.createNode(Constant.SERVE_LIST_ZK_NAME + "/" + config.serverIp + ":" + config.serverPort, null, CreateMode.EPHEMERAL, false);
    }

    

}
