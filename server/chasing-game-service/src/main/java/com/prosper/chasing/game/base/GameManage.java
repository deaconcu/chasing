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
import javax.swing.plaf.basic.BasicInternalFrameTitlePane.SystemMenuBar;

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
import com.prosper.chasing.game.base.User.UserState;
import com.prosper.chasing.game.message.Message;
import com.prosper.chasing.game.message.QuitCompleteMessage;
import com.prosper.chasing.game.message.SystemMessage;
import com.prosper.chasing.game.message.UserMessage;
import com.prosper.chasing.game.message.MessageParser;
import com.prosper.chasing.game.util.Config;

@Component
public class GameManage {

    private Logger log = LoggerFactory.getLogger(getClass());

    private static String gameImplScanPackage = "com.prosper.chasing.game.base.impl";
    private Map<String, Class<? extends Game>> gameClassMap = new HashMap<>();
    private Map<Integer, Game> gameMap = new HashMap<>();

    // 进入的消息队列
    private BlockingQueue<Message> recieveMessageQueue;
    // 发送的消息队列
    private BlockingQueue<UserMessage> sendMessageQueue;
    // 退出时需要写回到数据库的用户队列
    private BlockingQueue<User> userQueue;

    @Autowired
    private ThriftClient thriftClient;
    @Autowired
    private ZkClient zkClient;
    @Autowired
    private MessageParser messageParser;
    @Autowired
    private Config config;

    /**
     * 初始化游戏管理，主要实现把现有的游戏类加载到game class map里
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
                    prop.setId(propTr.getPropId());
                    prop.setCount(propTr.getCount());
                    propMap.put(propTr.getPropId(), prop);
                }
                user.setPropMap(propMap);
                user.setState(UserState.ACTIVE);
                user.setGameId(game.getGameInfo().getId());
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
     * @return true 添加成功
     * @return false 添加失败
     */
    public boolean recieveData(Message message) {
        return recieveMessageQueue.offer(message);
    }

    /**
     * 发送游戏消息
     * @return true 添加成功
     * @return false 添加失败
     */
    public boolean sendData(UserMessage message) {
        return sendMessageQueue.offer(message);
    }

    /**
     * 添加需要同步的用户
     * @return true 添加成功
     * @return false 添加失败
     */
    public boolean addUserForDataDB(User user) {
        return userQueue.offer(user);
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
                        Integer gameId = null;
                        if (message instanceof UserMessage) {
                            UserMessage userMessage = messageParser.parseUserMessage((UserMessage) message);
                            gameId = userMessage.getGameId();
                        } else if (message instanceof SystemMessage) {
                            SystemMessage systemMessage = (SystemMessage) message;
                            gameId = systemMessage.getGameId();
                        }

                        if (gameId != null) {
                            Game game = gameMap.get(gameId);
                            game.executeMessage(message);
                        } else {
                            log.warn("message type not supported");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    /**
     * 将发送消息队列的数据发送到connection server
     */
    @PostConstruct
    public void sendData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(true) {
                    try {
                        UserMessage message = sendMessageQueue.take();
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

    /**
     * 将需要同步的用户数据写回到数据库
     */
    @PostConstruct
    public void writeUser() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(true) {
                    User user = null;
                    try {
                        user = userQueue.take();
                        // TODO try 3 times
                        UserTr userTr = ViewTransformer.transferObject(user, UserTr.class);
                        Map<Integer, Prop> propMap = user.getPropMap();
                        List<UserPropTr> propList = new LinkedList<>();
                        for (Prop prop: propMap.values()) {
                            UserPropTr userPropTr = new UserPropTr();
                            userPropTr.setPropId(prop.getId());
                            userPropTr.setCount(prop.getCount());
                            propList.add(userPropTr);
                        }
                        
                        thriftClient.wrapperServiceClient().updateUserProp(userTr, propList);
                        user.setState(UserState.QUIT);
                    } catch(Exception e) {
                        if (user != null) {
                            user.setState(UserState.ACTIVE);
                        }
                    }
                    recieveData(new QuitCompleteMessage(user.getGameId(), user.getId()));
                }
            }
        }).start();
    }


    public BlockingQueue<UserMessage> getSendMessageQueue() {
        return sendMessageQueue;
    }

    public void setSendMessageQueue(BlockingQueue<UserMessage> sendMessageQueue) {
        this.sendMessageQueue = sendMessageQueue;
    }

}
