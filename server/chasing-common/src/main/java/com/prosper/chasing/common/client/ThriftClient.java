package com.prosper.chasing.common.client;

import java.util.List;
import java.util.Random;

import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransportException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TMultiplexedProtocol;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.prosper.chasing.common.interfaces.data.GameDataService;
import com.prosper.chasing.common.interfaces.data.PropDataService;
import com.prosper.chasing.common.interfaces.data.UserDataService;
import com.prosper.chasing.common.util.Constant;
import com.prosper.chasing.common.util.Pair;

@Component
public class ThriftClient {

    @Autowired
    private ZkClient zkClient;

    public GameDataService.Client getGameDataServiceClient(String registerName) {
        try {
            Pair<String, Integer> ipAndPort = getDataServiceAddr();
            TSocket transport = new TSocket(ipAndPort.getX(), ipAndPort.getY());
            transport.open();

            TBinaryProtocol protocol = new TBinaryProtocol(transport);
            TMultiplexedProtocol mp = new TMultiplexedProtocol(protocol, registerName);
            GameDataService.Client service = new GameDataService.Client(mp);
            return service;
        } catch (TTransportException e) {
            throw new RuntimeException(e);
        }
    }

    public UserDataService.Client getUserDataServiceClient(String ip, short port, String registerName) {
        try {
            Pair<String, Integer> ipAndPort = getDataServiceAddr();
            TSocket transport = new TSocket(ipAndPort.getX(), ipAndPort.getY());
            transport.open();

            TBinaryProtocol protocol = new TBinaryProtocol(transport);
            TMultiplexedProtocol mp = new TMultiplexedProtocol(protocol, registerName);
            UserDataService.Client service = new UserDataService.Client(mp);
            return service;
        } catch (TTransportException e) {
            throw new RuntimeException(e);
        }
    }

    public PropDataService.Client getPropDataServiceClient(String ip, short port, String registerName) {
        try {
            Pair<String, Integer> ipAndPort = getDataServiceAddr();
            TSocket transport = new TSocket(ipAndPort.getX(), ipAndPort.getY());
            transport.open();

            TBinaryProtocol protocol = new TBinaryProtocol(transport);
            TMultiplexedProtocol mp = new TMultiplexedProtocol(protocol, registerName);
            PropDataService.Client service = new PropDataService.Client(mp);
            return service;
        } catch (TTransportException e) {
            throw new RuntimeException(e);
        }
    }

    private Pair<String, Integer> getDataServiceAddr() {
        List<String> addrList = zkClient.getChild(Constant.RPCServerZkName);
        if (addrList.size() < 1) {
            return null;
        } else if (addrList.size() == 1) {
            String[] ipAndPort = addrList.get(0).split(",");
            return new Pair<String, Integer>(ipAndPort[0], Integer.parseInt(ipAndPort[1]));
        } else {
            int choosed = new Random().nextInt(addrList.size());
            String[] ipAndPort = addrList.get(choosed).split(",");
            return new Pair<String, Integer>(ipAndPort[0], Integer.parseInt(ipAndPort[1]));
        }
    }

    public ZkClient getZkClient() {
        return zkClient;
    }

    public void setZkClient(ZkClient zkClient) {
        this.zkClient = zkClient;
    }

}
