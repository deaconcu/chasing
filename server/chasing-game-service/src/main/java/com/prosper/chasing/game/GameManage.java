package com.prosper.chasing.game;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;

import com.prosper.chasing.common.client.ThriftClient;
import com.prosper.chasing.common.interfaces.data.GameDataService;
import com.prosper.chasing.common.interfaces.data.GameTr;
import com.prosper.chasing.common.interfaces.data.PropDataService;
import com.prosper.chasing.common.interfaces.data.UserPropTr;
import com.prosper.chasing.common.interfaces.data.UserTr;
import com.prosper.chasing.common.util.ViewTransformer;
import com.prosper.chasing.game.base.Game;
import com.prosper.chasing.game.base.GameInfo;
import com.prosper.chasing.game.base.MetaGameAnno;
import com.prosper.chasing.game.base.Prop;
import com.prosper.chasing.game.base.User;
import com.prosper.chasing.game.message.Message;
import com.prosper.chasing.game.message.MessageParser;

public class GameManage {

    private Logger log = LoggerFactory.getLogger(getClass());

    private static String gameImplScanPackage = "com.prosper.chasing.game.base.impl";
    private Map<String, Class<? extends Game>> gameClassMap = new HashMap<>();
    private Map<Integer, Game> gameMap = new HashMap<>();
    private BlockingQueue<Message> messageQueue;

    @Autowired
    ThriftClient thriftClient;
    @Autowired
    MessageParser messageParser;

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
                gameClass.newInstance();
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
        
        messageQueue = new LinkedBlockingQueue<>();
    }

    /**
     * 创建游戏，并放到game map里
     * @param gameTr 领取到的游戏
     */
    public void createGame(GameTr gameTr) {
        try {
            // create game
            GameInfo gameInfo = ViewTransformer.transferObject(gameTr, GameInfo.class);
            String metagameCode = gameInfo.getMetagameId();

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
            GameDataService.Client gameDataServiceClient = thriftClient.getGameDataServiceClient();
            PropDataService.Client propDataServiceClient = thriftClient.getPropDataServiceClient();
            List<UserTr> userTrList = gameDataServiceClient.getGameUsers(gameInfo.getId());
            List<User> userList = ViewTransformer.transferList(userTrList, User.class);

            Map<Integer, User> userMap = new HashMap<>();
            for (User user: userList) {
                List<UserPropTr> propList = propDataServiceClient.getUserProp(user.getId());
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
        } catch (Exception e) {
            log.error("create game failed", e);
        }
    }

    /**
     * 接收游戏消息
     */
    public void recieveData(int gameId, int userId, ByteBuffer message) {
        try {
            messageQueue.put(new Message(gameId, userId, message));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 执行游戏消息
     */
    public void executeData() {
        while(true) {
            try {
                Message message = messageQueue.take();
                Message parsedMessage = messageParser.parse(message);
                
                int gameId = parsedMessage.getGameId();
                Game game = gameMap.get(gameId);
                game.executeMessage(parsedMessage);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
}
