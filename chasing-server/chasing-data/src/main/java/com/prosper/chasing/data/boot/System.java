package com.prosper.chasing.data.boot;

import javax.annotation.PostConstruct;

import org.apache.zookeeper.CreateMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.prosper.chasing.common.bean.client.ZkClient;
import com.prosper.chasing.data.util.Config;
import com.prosper.chasing.data.util.Constant;

@Component
public class System  {

    @Autowired
    private ZkClient zkClient;
    @Autowired
    private Config config;

    @PostConstruct
    public void init() {
        zkClient.createPath(Constant.SERVE_LIST_ZK_NAME, CreateMode.PERSISTENT);
        // TODO CREATEMODE
        zkClient.createNode(Constant.SERVE_LIST_ZK_NAME + "/" + config.serverIp + ":" + config.rpcPort, null, CreateMode.PERSISTENT, false);
    }

    

}
