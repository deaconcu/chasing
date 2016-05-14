package com.prosper.chasing.game.boot;

import org.apache.zookeeper.CreateMode;
import org.springframework.context.ApplicationContext;

import com.prosper.chasing.common.boot.DefaultRPCApplication;
import com.prosper.chasing.common.client.ZkClient;
import com.prosper.chasing.game.util.Config;

public class GameServer extends DefaultRPCApplication {

    @Override
    public String getPackage() {
        return "com.prosper.chasing.game";
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

        zkClient.createPath(config.gameServerZKName, CreateMode.PERSISTENT);
        zkClient.createNode(config.gameServerZKName + "/" + config.serverIp + ":" + config.serverPort, null, CreateMode.EPHEMERAL, false);
    }
}
