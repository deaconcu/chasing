package com.prosper.chasing.game.base;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;

import javax.annotation.PostConstruct;

import com.prosper.chasing.game.message.*;
import com.prosper.chasing.game.util.ByteBuilder;
import com.prosper.chasing.game.util.Constant;
import org.apache.thrift.TException;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.stereotype.Component;

import com.prosper.chasing.common.bean.client.ThriftClient;
import com.prosper.chasing.common.bean.client.ZkClient;
import com.prosper.chasing.common.interfaces.data.GameTr;
import com.prosper.chasing.common.interfaces.data.MetagameTr;
import com.prosper.chasing.common.interfaces.data.UserPropTr;
import com.prosper.chasing.common.interfaces.data.UserTr;
import com.prosper.chasing.common.util.ViewTransformer;
import com.prosper.chasing.common.util.CommonConstant.GameState;
import com.prosper.chasing.game.util.Config;
import redis.clients.jedis.Jedis;

@Component
public class GameManage {

    private Logger log = LoggerFactory.getLogger(getClass());

    private static String gameImplScanPackage = "com.prosper.chasing.game.games";
    private Map<String, Class<? extends Game>> gameClassMap = new HashMap<>();
    private Map<Integer, Game> gameMap = new HashMap<>();

    // 进入的消息队列
    private BlockingQueue<Message> receiveMessageQueue;
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
        receiveMessageQueue = new LinkedBlockingQueue<>();
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

            for (User user: userList) {
                List<UserPropTr> propList = thriftClient.propDataServiceClient().getUserProp(user.getId());
                Map<Byte, Byte> propMap = new HashMap<>();
                for (UserPropTr propTr: propList) {
                    propMap.put((byte)propTr.getPropId(), (byte)propTr.getCount());
                }
                user.setPropMap(propMap);
                user.setState(Constant.UserState.LOADED);
                user.setGame(game);
            }
            game.loadUser(userList);
            game.setState(GameState.PREPARE);

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
            log.info("game finished, game: {}", game.getGameInfo().getId());
        } catch (TException e) {
            log.error("finish game failed, game id: " + gameId, e);
        }
    }

    /**
     * 接收游戏消息
     * @return true 添加成功
     * @return false 添加失败
     */
    public boolean recieveData(Message message) {
        return receiveMessageQueue.offer(message);
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

    /**
     * 创建游戏
     */
    @PostConstruct
    public void createGame() {
        new Thread(() -> {
                while(true) {
                    try {
                        //log.info("start creating game ...");
                        final ThriftClient.GameDataServiceClient gameDataServiceClient =
                                thriftClient.gameDataServiceClient();
                        List<GameTr> gameTrList =
                                gameDataServiceClient.ClaimGame(config.serverIp, config.rpcPort, 100);

                        if (gameTrList.size() == 0) {
                            Thread.sleep(1000);
                        } else {
                            for (final GameTr gameTr : gameTrList) {
                                try {
                                    createGame(gameTr);
                                    gameTr.setState(GameState.PROCESSING);
                                    // todo restore
                                    gameDataServiceClient.updateGame(gameTr);
                                } catch (Exception e) {
                                    log.error("create game failed", e);
                                }
                            }
                        }
                    } catch (Exception e) {
                        log.error("create game failed", e);
                        e.printStackTrace();
                    }
                }
        }).start();
    }

    /**
     * 执行游戏逻辑
     */
    @PostConstruct
    public void executeGameLogic() {
        new Thread(() -> {
            while(true) {
                try {
                    //log.info("start dispatch message ...");
                    Message message = receiveMessageQueue.poll();
                    while (message != null) {
                        Message parsedMessage = null;
                        if (message instanceof UserMessage) {
                            UserMessage userMessage = messageParser.parseUserMessage((UserMessage) message);
                            parsedMessage = userMessage;
                        } else if (message instanceof SystemMessage) {
                            SystemMessage systemMessage = (SystemMessage) message;
                            parsedMessage = systemMessage;
                        }

                        Integer gameId = message.getGameId();
                        if (gameId != null) {
                            Game game = gameMap.get(gameId);
                            // 如果game不存在，返回一个游戏不存在的消息
                            if (game == null) {
                                log.warn("game is not exist, game id: {}", gameId);
                                if (message instanceof UserMessage) {
                                    ByteBuilder byteBuilder =  new ByteBuilder();
                                    UserMessage userMessage = (UserMessage) parsedMessage;
                                    byteBuilder.append(0);
                                    byteBuilder.append(Constant.MessageType.NO_GAME);
                                    ReplyMessageQueue.offer(new ReplyMessage(
                                            userMessage.getUserId(), 0, ByteBuffer.wrap(byteBuilder.getBytes())));
                                }
                            } else {
                                log.info("dispatch message to game: {}", gameId);
                                game.offerMessage(parsedMessage);
                            }
                        }
                        message = receiveMessageQueue.poll();
                    }

                    long start = System.currentTimeMillis();
                    for (Game game: gameMap.values()) {
                        // 如果游戏已是等待销毁状态，则销毁游戏, 否则执行游戏逻辑
                        if (game.getState() == GameState.DESTROYING) {
                            finishGame(game.getGameInfo().getId());
                        } else {
                            if (game.getState() == GameState.PREPARE) {
                                game.generatePrepareMessage();
                                game.setState(GameState.PROCESSING);
                            } else if (game.getState() == GameState.PROCESSING) {
                                // 执行玩家消息
                                game.executeMessage();
                                // 执行游戏逻辑
                                game.logic();
                                // 检查, 比如玩家是否掉线, 游戏是否结束
                                game.check();
                            } else if (game.getState() == GameState.FINISHED) {
                                game.generateResultMessage();
                                game.setState(GameState.RESULT_INFORMED);
                            } else if (game.getState() == GameState.RESULT_INFORMED) {
                                // 执行玩家退出消息
                                game.executeMessage();
                                game.check();
                            }
                            // 发送玩家同步消息
                            game.generateUserMessage();
                            game.syncUser();
                        }
                    }
                    long cost = System.currentTimeMillis() - start;
                    if (cost < 15) {
                        // 如果一次处理时间不到15毫秒，则sleep一段时间
                        Thread.sleep(15 - cost);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * 将发送消息队列的数据发送到connection server
     */
    @PostConstruct
    public void replyData() {
        new Thread(() -> {
                while(true) {
                    try {
                        //log.info("start reply data ...");
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

                        /*
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
                        */

                        thriftClient.connectionServiceClient(ip, port).
                                executeData(userId, message.getContent());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
            }
        }).start();
    }

    /**
     * 将需要同步的用户数据写回到数据库
     */
    @PostConstruct
    public void writeUser() {
        new Thread(() -> {
                while(true) {
                    //log.info("start write user ...");
                    User user = null;
                    try {
                        user = userQueue.take();
                        // TODO try 3 times
                        UserTr userTr = ViewTransformer.transferObject(user, UserTr.class);
                        Map<Byte, Byte> propMap = user.getPropMap();
                        List<UserPropTr> propList = new LinkedList<>();
                        for (Map.Entry<Byte, Byte> entry: propMap.entrySet()) {
                            UserPropTr userPropTr = new UserPropTr();
                            userPropTr.setPropId(entry.getKey());
                            userPropTr.setCount(entry.getValue());
                            propList.add(userPropTr);
                        }

                        userTr.setState((byte)1);
                        userTr.setGameId(-1);
                        thriftClient.wrapperServiceClient().updateUserProp(userTr, propList);
                        thriftClient.UserDataServiceClient().updateUser(userTr);
                        recieveData(new QuitCompleteMessage(user.getGame().getGameInfo().getId(), user.getId()));
                    } catch(Exception e) {
                        if (user != null) {
                            user.setState(Constant.UserState.ACTIVE);
                        }
                        e.printStackTrace();
                    }
                }
        }).start();
    }
}
