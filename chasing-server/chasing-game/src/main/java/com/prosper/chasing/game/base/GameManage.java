package com.prosper.chasing.game.base;

import java.lang.annotation.Annotation;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;

import javax.annotation.PostConstruct;
import javax.swing.plaf.basic.BasicInternalFrameTitlePane.SystemMenuBar;

import org.apache.thrift.TException;
import org.apache.thrift.async.AsyncMethodCallback;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.scheduling.annotation.Scheduled;
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
import com.prosper.chasing.common.util.CommonConstant.GameState;
import com.prosper.chasing.game.base.User.UserState;
import com.prosper.chasing.game.message.Message;
import com.prosper.chasing.game.message.QuitCompleteMessage;
import com.prosper.chasing.game.message.ReplyMessage;
import com.prosper.chasing.game.message.SystemMessage;
import com.prosper.chasing.game.message.UserMessage;
import com.prosper.chasing.game.message.MessageParser;
import com.prosper.chasing.game.util.Config;
import redis.clients.jedis.Jedis;

@Component
public class GameManage {

    private Logger log = LoggerFactory.getLogger(getClass());

    private static String gameImplScanPackage = "com.prosper.chasing.game.base.impl";
    private Map<String, Class<? extends Game>> gameClassMap = new HashMap<>();
    private Map<Integer, Game> gameMap = new HashMap<>();

    // 进入的消息队列
    private BlockingQueue<Message> recieveMessageQueue;
    // 发送的消息队列
    private BlockingQueue<ReplyMessage> ReplyMessageQueue;
    // 需要写回到数据库的用户队列
    private BlockingQueue<User> userQueue;

    @Autowired
    private ThriftClient thriftClient;
    @Autowired
    private ZkClient zkClient;
    @Autowired
    private MessageParser messageParser;
    @Autowired
    private Config config;
    @Autowired
    ExecutorService executorService;
    @Autowired
    Jedis jedis;

    /**
     * 初始化游戏管理，主要实现把现有的游戏类加载到game class map里
     */
    @SuppressWarnings("unchecked")
    public GameManage() {
        // 扫描game的子类，拿到一个set
        ClassPathScanningCandidateComponentProvider rpcServiceScanner =
                new ClassPathScanningCandidateComponentProvider(false);
        rpcServiceScanner.addIncludeFilter(new AssignableTypeFilter(Game.class));
        Set<BeanDefinition> gameBeanSet = rpcServiceScanner.findCandidateComponents(gameImplScanPackage);

        // 把获取到的game class，写到一个map里，key为game上注解标注的名字
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

        // 初始化消息队列，回答队列，用户队列
        recieveMessageQueue = new LinkedBlockingQueue<>();
        ReplyMessageQueue = new LinkedBlockingQueue<>();
        userQueue = new LinkedBlockingQueue<>();
        log.info("game manage started");
    }

    /**
     * 创建游戏，并放到game map里
     * @param gameTr 领取到的游戏
     */
    public void createGame(GameTr gameTr) {
        try {
            // 获取game的信息
            GameInfo gameInfo = ViewTransformer.transferObject(gameTr, GameInfo.class);

            // 通过rpc获取meta game的信息
            List<Integer> metagameIdList = new LinkedList<Integer>();
            metagameIdList.add(gameInfo.getMetagameId());
            List<MetagameTr> metagameTrList = thriftClient.metagameDataServiceClient().getMetagame(metagameIdList);

            // 获取metagame的code
            String metagameCode = metagameTrList.get(0).getCode();

            // 通过metagame code或者game class，然后初始化game
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
            game.setGameInfo(gameInfo);
            game.setGameManage(this);

            // 加载游戏用户
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
                user.setState(UserState.LOADED);
                user.setGameId(game.getGameInfo().getId());
            }
            game.loadUser(userList);

            // 把加载好的游戏放到map中
            gameMap.put(gameInfo.getId(), game);

            // 创建zookeeper节点
            String serverAddr = config.serverIp + ":" + config.rpcPort;
            zkClient.createNode(config.gameZkName + "/" + gameInfo.getId(),
                    serverAddr.getBytes(), CreateMode.PERSISTENT, true);
            log.info("create game success, game id:" + gameInfo.getId());
        } catch (Exception e) {
            log.error("create game failed", e);
        }
    }

    /**
     * 完成游戏
     */
    public void finishGame(int gameId) {
        Game game = gameMap.get(gameId);
        GameInfo gameInfo = game.getGameInfo();
        gameInfo.setState(game.getState());
        GameTr gameTr = ViewTransformer.transferObject(gameInfo, GameTr.class);
        try {
            thriftClient.gameDataServiceClient().updateGame(gameTr);
            gameMap.remove(gameId);
        } catch (TException e) {
            log.error("finish game failed, game id:" + gameId, e);
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
    public boolean replyData(ReplyMessage message) {
        return ReplyMessageQueue.offer(message);
    }

    /**
     * 添加需要同步的用户
     * @return true 添加成功
     * @return false 添加失败
     */
    public boolean addUserForDataDB(User user) {

        return userQueue.offer(user);
    }

    @PostConstruct
    public void createGame() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(true) {
                    try {
                        final ThriftClient.GameDataServiceClient gameDataServiceClient =
                                thriftClient.gameDataServiceClient();
                        List<GameTr> gameTrList =
                                gameDataServiceClient.ClaimGame(config.serverIp, config.rpcPort, 100);

                        if (gameTrList.size() > 0) {
                            System.out.println("11111");
                        }

                        for (final GameTr gameTr : gameTrList) {
                            try {
                                createGame(gameTr);
                                gameTr.setState((byte) GameState.PROCESSING);
                                // todo restore
                                gameDataServiceClient.updateGame(gameTr);
                            } catch (Exception e) {
                                log.error("create game failed", e);
                            }
                        }
                    } catch (Exception e) {
                        log.error("create game failed", e);
                    }
                }
            }
        }).start();
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
                        Message parsedMessage = null;
                        Integer gameId = null;
                        if (message instanceof UserMessage) {
                            UserMessage userMessage = messageParser.parseUserMessage((UserMessage) message);
                            gameId = userMessage.getGameId();
                            parsedMessage = userMessage;
                        } else if (message instanceof SystemMessage) {
                            SystemMessage systemMessage = (SystemMessage) message;
                            gameId = systemMessage.getGameId();
                            parsedMessage = systemMessage;
                        }

                        if (gameId != null) {
                            Game game = gameMap.get(gameId);
                            game.executeMessage(parsedMessage);
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
    public void replyData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(true) {
                    try {
                        ReplyMessage message = ReplyMessageQueue.take();
                        final int userId = message.getUserId();
                        long begin = System.currentTimeMillis();

                        // TODO temporary
                        //String addr = jedis.get("user-" + userId);
                        String addr = "127.0.0.1:8202";
                        /*
                        byte[] bytes = zkClient.get(config.userZkName + "/" + userId, false);
                        long cost = System.currentTimeMillis() - begin;
                        log.info("cost:" + cost);

                        String addr = new String(bytes);
                        */
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

                        userTr.setState((byte)1);
                        userTr.setGameId(-1);
                        thriftClient.wrapperServiceClient().updateUserProp(userTr, propList);
                        thriftClient.UserDataServiceClient().updateUser(userTr);
                        recieveData(new QuitCompleteMessage(user.getGameId(), user.getId()));
                    } catch(Exception e) {
                        if (user != null) {
                            user.setState(UserState.ACTIVE);
                        }
                    }
                }
            }
        }).start();
    }

    /**
     * 检查已完成的游戏
     */
    @PostConstruct
    public void checkGame() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(true) {
                    for (Map.Entry<Integer, Game> gameEntry: gameMap.entrySet()) {
                        if (gameEntry.getValue().getState() == GameState.FINISHED) {
                            log.info("finishing game: " + gameEntry.getKey());
                            finishGame(gameEntry.getKey());
                        }
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

}
