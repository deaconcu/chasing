package com.prosper.chasing.common.bean.client;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Random;

import org.apache.thrift.TException;
import org.apache.thrift.async.AsyncMethodCallback;
import org.apache.thrift.async.TAsyncClientManager;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TMultiplexedProtocol;
import org.apache.thrift.transport.TNonblockingSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.springframework.beans.factory.annotation.Autowired;

import com.prosper.chasing.common.bean.ThriftTransportPool;
import com.prosper.chasing.common.bean.ThriftTransportPool.Type;
import com.prosper.chasing.common.bean.wrapper.NettyWebSocketServer;
import com.prosper.chasing.common.interfaces.connection.ConnectionService;
import com.prosper.chasing.common.interfaces.data.GameDataService;
import com.prosper.chasing.common.interfaces.data.GameTr;
import com.prosper.chasing.common.interfaces.data.MetagameDataService;
import com.prosper.chasing.common.interfaces.data.MetagameTr;
import com.prosper.chasing.common.interfaces.data.PropDataService;
import com.prosper.chasing.common.interfaces.data.UserDataService;
import com.prosper.chasing.common.interfaces.data.UserPropTr;
import com.prosper.chasing.common.interfaces.data.UserTr;
import com.prosper.chasing.common.interfaces.game.GameException;
import com.prosper.chasing.common.interfaces.game.GameService;
import com.prosper.chasing.common.util.Pair;

public class ThriftClient {
    
    private static String gameDataServerZkName = "/gameDataServer/serverList"; 
    private static String gameServerZkName = "/gameServer/serverList"; 
    private static String gameDataServiceRegisterName = "GameDataServiceImpl";
    private static String metagameDataServiceRegisterName = "MetagameDataServiceImpl";
    private static String userDataServiceRegisterName = "UserDataServiceImpl";
    private static String propDataServiceRegisterName = "PropDataServiceImpl";
    private static String gameServiceRegisterName = "GameServiceImpl";
    
    @Autowired
    private ThriftTransportPool thriftTransportPool;
    
    public GameDataServiceClient gameDataServiceClient() {
        return new GameDataServiceClient();
    }
    
    public MetagameDataServiceClient metagameDataServiceClient() {
        return new MetagameDataServiceClient(); 
    }
    
    public PropDataServiceClient propDataServiceClient() {
        return new PropDataServiceClient();
    }
    
    public UserDataServiceClient UserDataServiceClient() {
        return new UserDataServiceClient();
    }
    
    public GameServiceClient gameServiceClient(String ip, int port) {
        return new GameServiceClient(ip, port);
    }
    
    public ConnectionServiceAsyncClient connectionServiceAsyncClient(String ip, int port) {
        return new ConnectionServiceAsyncClient(ip, port);
    }
    
    @Autowired
    private ZkClient zkClient;
    
    public class GameDataServiceClient implements GameDataService.Iface {
        
        @Override
        public List<GameTr> ClaimGame(String ip, int port, int count) throws TException {
            TTransport transport = null;
            Pair<String, Integer> ipAndPort = getServiceAddr(gameDataServerZkName);
            try {
                transport = thriftTransportPool.borrowObject(ipAndPort.getX(), ipAndPort.getY());
                TBinaryProtocol protocol = new TBinaryProtocol(transport);
                TMultiplexedProtocol mp = new TMultiplexedProtocol(protocol, gameDataServiceRegisterName);
                GameDataService.Client service = new GameDataService.Client(mp);
                
                return service.ClaimGame(ip, port, count);
            } catch (TTransportException e) {
                throw new RuntimeException(e);
            } finally {
                if (transport != null) {
                    thriftTransportPool.returnObject(ipAndPort.getX(), ipAndPort.getY(), transport);
                }
            }
        }

        @Override
        public void updateGame(GameTr gameTr) throws TException {
            TTransport transport = null;
            Pair<String, Integer> ipAndPort = getServiceAddr(gameDataServerZkName);
            try {
                transport = thriftTransportPool.borrowObject(ipAndPort.getX(), ipAndPort.getY());
                TBinaryProtocol protocol = new TBinaryProtocol(transport);
                TMultiplexedProtocol mp = new TMultiplexedProtocol(protocol, gameDataServiceRegisterName);
                GameDataService.Client service = new GameDataService.Client(mp);
                
                service.updateGame(gameTr);
            } catch (TTransportException e) {
                throw new RuntimeException(e);
            } finally {
                if (transport != null) {
                    thriftTransportPool.returnObject(ipAndPort.getX(), ipAndPort.getY(), transport);
                }
            }
        }

        @Override
        public List<UserTr> getGameUsers(int gameId) throws TException {
            TTransport transport = null;
            Pair<String, Integer> ipAndPort = getServiceAddr(gameDataServerZkName);
            try {
                transport = thriftTransportPool.borrowObject(ipAndPort.getX(), ipAndPort.getY());
                TBinaryProtocol protocol = new TBinaryProtocol(transport);
                TMultiplexedProtocol mp = new TMultiplexedProtocol(protocol, gameDataServiceRegisterName);
                GameDataService.Client service = new GameDataService.Client(mp);
                
                return service.getGameUsers(gameId);
            } catch (TTransportException e) {
                throw new RuntimeException(e);
            } finally {
                if (transport != null) {
                    thriftTransportPool.returnObject(ipAndPort.getX(), ipAndPort.getY(), transport);
                }
            }
        }
    }
    
    public class MetagameDataServiceClient implements MetagameDataService.Iface {
        
        @Override
        public List<MetagameTr> getMetagame(List<Integer> metagameIdList) throws TException {
            TTransport transport = null;
            Pair<String, Integer> ipAndPort = getServiceAddr(gameDataServerZkName);
            try {
                transport = thriftTransportPool.borrowObject(ipAndPort.getX(), ipAndPort.getY());
                TBinaryProtocol protocol = new TBinaryProtocol(transport);
                TMultiplexedProtocol mp = new TMultiplexedProtocol(protocol, metagameDataServiceRegisterName);
                MetagameDataService.Client service = new MetagameDataService.Client(mp);
                
                return service.getMetagame(metagameIdList);
            } catch (TTransportException e) {
                throw new RuntimeException(e);
            } finally {
                if (transport != null) {
                    thriftTransportPool.returnObject(ipAndPort.getX(), ipAndPort.getY(), transport);
                }
            }
        }

    }
    
    public class PropDataServiceClient implements PropDataService.Iface {
        
        @Override
        public List<UserPropTr> getUserProp(int userId) throws TException {
            TTransport transport = null;
            Pair<String, Integer> ipAndPort = getServiceAddr(gameServerZkName);
            try {
                transport = thriftTransportPool.borrowObject(ipAndPort.getX(), ipAndPort.getY());
                TBinaryProtocol protocol = new TBinaryProtocol(transport);
                TMultiplexedProtocol mp = new TMultiplexedProtocol(protocol, propDataServiceRegisterName);
                PropDataService.Client service = new PropDataService.Client(mp);
                
                return service.getUserProp(userId);
            } catch (TTransportException e) {
                throw new RuntimeException(e);
            } finally {
                if (transport != null) {
                    thriftTransportPool.returnObject(ipAndPort.getX(), ipAndPort.getY(), transport);
                }
            }
        }

        @Override
        public void updateUserProp(int userId, List<UserPropTr> usedUserPropList)
                throws TException {
            // TODO Auto-generated method stub
        }


    }
    
    public class UserDataServiceClient implements UserDataService.Iface {
        
        @Override
        public UserTr getUser(int userId) throws TException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void updateUser(UserTr user) throws TException {
            // TODO Auto-generated method stub
            
        }
    }
    
    public class GameServiceClient implements GameService.Iface {
        
        private String ip;
        private int port;
        
        public GameServiceClient(String ip, int port) {
            this.ip = ip;
            this.port = port;
        }
        
        @Override
        public void executeData(int gameId, int userId, ByteBuffer message) 
                throws GameException, TException {
            TTransport transport = null;
            try {
                transport = thriftTransportPool.borrowObject(ip, port);
                TBinaryProtocol protocol = new TBinaryProtocol(transport);
                TMultiplexedProtocol mp = new TMultiplexedProtocol(protocol, gameServiceRegisterName);
                GameService.Client service = new GameService.Client(mp);
                
                service.executeData(gameId, userId, message);
            } catch (TTransportException e) {
                throw new RuntimeException(e);
            } finally {
                if (transport != null) {
                    thriftTransportPool.returnObject(ip, port, transport);
                }
            }
        }

    }
    
    public class ConnectionServiceAsyncClient implements ConnectionService.AsyncIface {
        
        private String ip;
        private int port;
        
        public ConnectionServiceAsyncClient(String ip, int port) {
            this.ip = ip;
            this.port = port;
        }
        
//        public void executeData(int userId, ByteBuffer message) throws TException {
//            TTransport transport = null;
//            try {
//                transport = thriftTransportPool.borrowObject(ip, port);
//                TBinaryProtocol protocol = new TBinaryProtocol(transport);
//                ConnectionService.AsyncClient service = new ConnectionService.AsyncClient(
//                        new TBinaryProtocol.Factory(), new TAsyncClientManager(), transport);
//                
//                service.executeData(userId, message);
//            } catch (TTransportException e) {
//                throw new RuntimeException(e);
//            } finally {
//                if (transport != null) {
//                    thriftTransportPool.returnObject(ip, port, transport);
//                }
//            }
//        }

        @Override
        public void executeData(int userId, ByteBuffer message, AsyncMethodCallback resultHandler) throws TException {
            TTransport transport = null;
            try {
                transport = thriftTransportPool.borrowObject(ip, port, Type.tNonblockingSocket);
                TNonblockingSocket tNonblockingSocket = (TNonblockingSocket) transport;
                ConnectionService.AsyncClient service = new ConnectionService.AsyncClient(
                        new TBinaryProtocol.Factory(), new TAsyncClientManager(), tNonblockingSocket);
                
                service.executeData(userId, message, resultHandler);
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                if (transport != null) {
                    thriftTransportPool.returnObject(ip, port, transport);
                }
            }
            
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
    
}


