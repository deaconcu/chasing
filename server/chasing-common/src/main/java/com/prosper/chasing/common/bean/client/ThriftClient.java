package com.prosper.chasing.common.bean.client;

import java.util.List;
import java.util.Random;

import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TMultiplexedProtocol;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.prosper.chasing.common.interfaces.data.GameDataService;
import com.prosper.chasing.common.interfaces.data.MetagameDataService;
import com.prosper.chasing.common.interfaces.data.PropDataService;
import com.prosper.chasing.common.interfaces.data.UserDataService;
import com.prosper.chasing.common.interfaces.game.GameService;
import com.prosper.chasing.common.util.CommonConstant;
import com.prosper.chasing.common.util.Pair;

@Component
public class ThriftClient {
    
    private static String gameDataServerZkName = "/gameDataServer/serverList"; 
    private static String gameServerZkName = "/gameServer/serverList"; 
    private static String gameDataServiceRegisterName = "GameDataServiceImpl";
    private static String metagameDataServiceRegisterName = "MetagameDataServiceImpl";
    private static String userDataServiceRegisterName = "UserDataServiceImpl";
    private static String propDataServiceRegisterName = "PropDataServiceImpl";
    private static String gameServiceRegisterName = "GameServiceImpl";

    @Autowired
    private ZkClient zkClient;

    public GameDataService.Client getGameDataServiceClient() {
        try {
            Pair<String, Integer> ipAndPort = getServiceAddr(gameDataServerZkName);
            TTransport transport = new TSocket(ipAndPort.getX(), ipAndPort.getY());
            transport.open();

            TBinaryProtocol protocol = new TBinaryProtocol(transport);
            TMultiplexedProtocol mp = new TMultiplexedProtocol(protocol, gameDataServiceRegisterName);
            GameDataService.Client service = new GameDataService.Client(mp);
            return service;
        } catch (TTransportException e) {
            throw new RuntimeException(e);
        }
    }
    
    public MetagameDataService.Client getMetagameDataServiceClient() {
        try {
            Pair<String, Integer> ipAndPort = getServiceAddr(gameDataServerZkName);
            TTransport transport = new TSocket(ipAndPort.getX(), ipAndPort.getY());
            transport.open();

            TBinaryProtocol protocol = new TBinaryProtocol(transport);
            TMultiplexedProtocol mp = new TMultiplexedProtocol(protocol, metagameDataServiceRegisterName);
            MetagameDataService.Client service = new MetagameDataService.Client(mp);
            return service;
        } catch (TTransportException e) {
            throw new RuntimeException(e);
        }
    }

    public UserDataService.Client getUserDataServiceClient() {
        try {
            Pair<String, Integer> ipAndPort = getServiceAddr(gameDataServerZkName);
            TTransport transport = new TSocket(ipAndPort.getX(), ipAndPort.getY());
            transport.open();

            TBinaryProtocol protocol = new TBinaryProtocol(transport);
            TMultiplexedProtocol mp = new TMultiplexedProtocol(protocol, userDataServiceRegisterName);
            UserDataService.Client service = new UserDataService.Client(mp);
            return service;
        } catch (TTransportException e) {
            throw new RuntimeException(e);
        }
    }

    public PropDataService.Client getPropDataServiceClient() {
        try {
            Pair<String, Integer> ipAndPort = getServiceAddr(gameDataServerZkName);
            TTransport transport = new TSocket(ipAndPort.getX(), ipAndPort.getY());
            transport.open();

            TBinaryProtocol protocol = new TBinaryProtocol(transport);
            TMultiplexedProtocol mp = new TMultiplexedProtocol(protocol, propDataServiceRegisterName);
            PropDataService.Client service = new PropDataService.Client(mp);
            return service;
        } catch (TTransportException e) {
            throw new RuntimeException(e);
        }
    }
    
    public GameService.Client getGameServiceClient() {
        try {
            Pair<String, Integer> ipAndPort = getServiceAddr(gameServerZkName);
            TTransport transport = new TSocket(ipAndPort.getX(), ipAndPort.getY());
            transport.open();

            TBinaryProtocol protocol = new TBinaryProtocol(transport);
            TMultiplexedProtocol mp = new TMultiplexedProtocol(protocol, gameServiceRegisterName);
            GameService.Client service = new GameService.Client(mp);
            return service;
        } catch (TTransportException e) {
            throw new RuntimeException(e);
        }
    }
    
    public GameService.Client getGameServiceClient(String ip, int port) {
        try {
            if (!isInList(ip, port, gameServerZkName)) {
                return null;
            }
            TTransport transport = new TSocket(ip, port);
            transport.open();

            TBinaryProtocol protocol = new TBinaryProtocol(transport);
            TMultiplexedProtocol mp = new TMultiplexedProtocol(protocol, gameServiceRegisterName);
            GameService.Client service = new GameService.Client(mp);
            return service;
        } catch (TTransportException e) {
            throw new RuntimeException(e);
        }
    }

    private Pair<String, Integer> getServiceAddr(String serverListZKName) {
        List<String> addrList = zkClient.getChild(serverListZKName, true);
        if (addrList.size() < 1) {
            return null;
        } else if (addrList.size() == 1) {
            String[] ipAndPort = addrList.get(0).split(":");
            return new Pair<String, Integer>(ipAndPort[0], Integer.parseInt(ipAndPort[1]));
        } else {
            int choosed = new Random().nextInt(addrList.size());
            String[] ipAndPort = addrList.get(choosed).split(":");
            return new Pair<String, Integer>(ipAndPort[0], Integer.parseInt(ipAndPort[1]));
        }
    }
    
    private boolean isInList(String ip, int port, String serverListZKName) {
        List<String> addrList = zkClient.getChild(serverListZKName, true);
        String addr = ip + ":" + port;
        if (addrList.contains(addr)) {
            return true;
        } else {
            return false;
        }
    }

    public ZkClient getZkClient() {
        return zkClient;
    }

    public void setZkClient(ZkClient zkClient) {
        this.zkClient = zkClient;
    }

}
