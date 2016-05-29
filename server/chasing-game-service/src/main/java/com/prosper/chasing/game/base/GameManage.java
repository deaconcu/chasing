package com.prosper.chasing.game.base;

import java.lang.annotation.Annotation;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.annotation.PostConstruct;

import org.apache.thrift.async.AsyncMethodCallback;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.stereotype.Component;

import com.prosper.chasing.common.bean.client.ThriftClient;
import com.prosper.chasing.common.bean.client.ThriftClient.ConnectionServiceAsyncClient;
import com.prosper.chasing.common.bean.client.ZkClient;
import com.prosper.chasing.common.interfaces.connection.ConnectionService.AsyncClient;
import com.prosper.chasing.common.interfaces.data.GameDataService;
import com.prosper.chasing.common.interfaces.data.GameTr;
import com.prosper.chasing.common.interfaces.data.MetagameDataService;
import com.prosper.chasing.common.interfaces.data.MetagameTr;
import com.prosper.chasing.common.interfaces.data.PropDataService;
import com.prosper.chasing.common.interfaces.data.UserPropTr;
import com.prosper.chasing.common.interfaces.data.UserTr;
import com.prosper.chasing.common.util.ViewTransformer;
import com.prosper.chasing.game.message.Message;
import com.prosper.chasing.game.message.MessageParser;
import com.prosper.chasing.game.util.Config;

@Component
public class GameManage {

    private Logger log = LoggerFactory.getLogger(getClass());

    private static String gameImplScanPackage = "com.prosper.chasing.game.base.impl";
    private Map<String, Class<? extends Game>> gameClassMap = new HashMap<>();
    private Map<Integer, Game> gameMap = new HashMap<>();
    private BlockingQueue<Message> recieveMessageQueue;
    private BlockingQueue<Message> sendMessageQueue;

    @Autowired
    private ThriftClient thriftClient;
    @Autowired
    private ZkClient zkClient;
    @Autowired
    private MessageParser messageParser;
    @Autowired
    private Config config;

    /**
     * 初始化游戏管理，主要实现把现有的游戏类加载到一个map里
     */
    public GameManage() {
        ClassPathScanningCandidateComponentProvider rpcServiceScanner =
                new ClassPathScanningCandidateComponentProvider(false);
        rpcServiceScanner.addIncludeFilter(new AssignableTypeFilter(Game.class));

        Set<BeanDefinition> gameBeanSet = rpcServiceScanner.findCandidateComponents(gameImplScanPackage);
        for (BeanDefinition  beanDefinition: gameBeanSet) {
            String className = beanDefinition.getBeanClassName();
            Class<? extends Game> gameClass = null;
            try {
                gameClass = (Class<? extends Game>)Class.forName(className);
                Object object = gameClass.newInstance();
            } catch (Exception e) {
                log.warn("create game object failed:" + className, e);
                continue;
            }
            MetaGameAnno anno = gameClass.getAnnotation(MetaGameAnno.class);
            if (anno != null && anno.value() != null && !anno.value().equals("")) {
                String metagameCode = anno.value();
                Object object = gameClassMap.get(metagameCode);
                if (object != null) {
                    log.warn("metagame type exist, skip game implement:" + className);
                } else {
                    gameClassMap.put(metagameCode, gameClass);
                }
            }
        }
        
        recieveMessageQueue = new LinkedBlockingQueue<>();
        sendMessageQueue = new LinkedBlockingQueue<>();
    }

    /**
     * 创建游戏，并放到game map里
     * @param gameTr 领取到的游戏
     */
    public void createGame(GameTr gameTr) {
        try {
            // create game
            GameInfo gameInfo = ViewTransformer.transferObject(gameTr, GameInfo.class);
            
            List<Integer> metagameIdList = new LinkedList<Integer>();
            metagameIdList.add(gameInfo.getMetagameId());
            List<MetagameTr> metagameTrList = thriftClient.metagameDataServiceClient().getMetagame(metagameIdList);
            
            String metagameCode = metagameTrList.get(0).getCode();
            
            Class<? extends Game> gameClass = gameClassMap.get(metagameCode);
            if (gameClass == null) {
                log.error("metagame implement is not exist:" + metagameCode);
            }
            Game game = null;
            try {
                game = gameClass.newInstance();
            } catch (Exception e) {
                log.error("create game failed, game class:" + Game.class.getName());
            }

            // load game info, user and prop
            game.setGameInfo(gameInfo);
            game.setGameManage(this);
            List<UserTr> userTrList = thriftClient.gameDataServiceClient().getGameUsers(gameInfo.getId());
            List<User> userList = ViewTransformer.transferList(userTrList, User.class);

            Map<Integer, User> userMap = new HashMap<>();
            for (User user: userList) {
                List<UserPropTr> propList = thriftClient.propDataServiceClient().getUserProp(user.getId());
                Map<Integer, Prop> propMap = new HashMap<>();
                for (UserPropTr propTr: propList) {
                    Prop prop = new Prop();
                    prop.setCount(propTr.getCount());
                    propMap.put(propTr.getPropId(), prop);
                }
                user.setPropMap(propMap);
                userMap.put(user.getId(), user);
            }
            game.setUserMap(userMap);
            // put game into map
            gameMap.put(gameInfo.getId(), game);
            
            String serverAddr = config.serverIp + ":" + config.rpcPort;
            zkClient.createNode(config.gameZkName + "/" + gameInfo.getId(), 
                    serverAddr.getBytes(), CreateMode.PERSISTENT, true);
        } catch (Exception e) {
            log.error("create game failed", e);
        }
    }

    /**
     * 接收游戏消息
     */
    public void recieveData(int gameId, int userId, ByteBuffer message) {
        try {
            recieveMessageQueue.put(new Message(gameId, userId, message));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 发送游戏消息
     */
    public void sendData(Message message) {
        try {
            sendMessageQueue.put(message);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 执行游戏消息
     */
    @PostConstruct
    public void executeData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(true) {
                    try {
                        Message message = recieveMessageQueue.take();
                        Message parsedMessage = messageParser.parse(message);
                        
                        int gameId = parsedMessage.getGameId();
                        Game game = gameMap.get(gameId);
                        game.executeMessage(parsedMessage);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }
    
    @PostConstruct
    public void sendData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(true) {
                    try {
                        Message message = sendMessageQueue.take();
                        final int userId = message.getUserId();

                        String addr = new String(zkClient.get(config.userZkName + "/" + userId, true));
                        String ipAndPort[] = addr.split(":");
                        String ip = ipAndPort[0];
                        int port = Integer.parseInt(ipAndPort[1]);
                        
                        ConnectionServiceAsyncClient asyncClient = thriftClient.connectionServiceAsyncClient(ip, port);
                        asyncClient.executeData(userId, message.getContent(), new AsyncMethodCallback<Object>() {
                            @Override
                            public void onComplete(Object response) {
                                log.info("response success, user id:" + userId);
                            }
                            @Override
                            public void onError(Exception exception) {
                                log.info("response failed, user id:" + userId, exception);
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    public BlockingQueue<Message> getSendMessageQueue() {
        return sendMessageQueue;
    }

    public void setSendMessageQueue(BlockingQueue<Message> sendMessageQueue) {
        this.sendMessageQueue = sendMessageQueue;
    }
    
}
