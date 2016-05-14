package com.prosper.chasing.game;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.AssignableTypeFilter;

import com.prosper.chasing.common.boot.RPCService;
import com.prosper.chasing.common.interfaces.data.GameTr;
import com.prosper.chasing.common.interfaces.game.Message;
import com.prosper.chasing.common.message.PositionMessage;
import com.prosper.chasing.common.util.ViewTransformer;
import com.prosper.chasing.game.base.Game;
import com.prosper.chasing.game.base.GameInfo;
import com.prosper.chasing.game.base.MetaGameAnno;
import com.prosper.chasing.game.base.User;

public class GameManage {
    
    private Logger log = LoggerFactory.getLogger(getClass());

    private static String gameImplScanPackage = "com.prosper.chasing.game.base.impl";
    private Map<String, Class<? extends Game>> gameClassMap = new HashMap<>();
    private Map<Integer, Game> gameMap = new HashMap<>();
    
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
    }
    
    /**
     * 创建游戏，并放到game map里
     */
    public void createGame(GameTr gameTr) {
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
        
        // todo load game info, user and prop
        
        gameMap.put(gameTr.getId(), game);
    }
    
    public void executeData(Message message) {
        if (message instanceof PositionMessage) {
            PositionMessage positionMessage = (PositionMessage) message;
            String gameId = positionMessage.getGameId();
            long userId = positionMessage.getUserId();
            
            Game game = gameMap.get(gameId);
            //User user = game.getUserList(userId);
            
            //user.addY(positionMessage.getDistance());
        } else {
            return;
        }
    }

    
    
}
