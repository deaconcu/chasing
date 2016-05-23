package com.prosper.chasing.game.boot;

import javax.annotation.PostConstruct;

import org.apache.zookeeper.CreateMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.prosper.chasing.common.bean.client.ZkClient;
import com.prosper.chasing.game.util.Config;

@Component
public class System {
    
    @Autowired
    private ZkClient zkClient;
    @Autowired
    private Config config;

    @PostConstruct
    public void init() {
        zkClient.createPath(config.gameServerZKName, CreateMode.PERSISTENT);
        zkClient.createNode(config.gameServerZKName + "/" + config.serverIp + ":" + config.serverPort, null, CreateMode.EPHEMERAL, false);
    }
}
