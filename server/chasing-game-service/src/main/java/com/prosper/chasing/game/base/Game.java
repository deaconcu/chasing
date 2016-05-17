package com.prosper.chasing.game.base;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.thrift.TException;
import org.eclipse.jetty.security.PropertyUserStore.UserListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonObjectFormatVisitor;
import com.prosper.chasing.common.boot.RPCService;
import com.prosper.chasing.common.interfaces.data.GameDataService;
import com.prosper.chasing.common.interfaces.game.GameException;
import com.prosper.chasing.common.interfaces.game.GameService;
import com.prosper.chasing.game.GameManage;
import com.prosper.chasing.game.message.Message;
import com.prosper.chasing.game.message.PositionMessage;
import com.prosper.chasing.game.message.PropMessage;
import com.prosper.chasing.game.message.SkillMessage;
import com.prosper.chasing.game.prop.BaseProp;
import com.prosper.chasing.game.util.Constant.GameLoadingState;
import com.prosper.chasing.common.util.JsonUtil;

@Component
public abstract class Game {
    
    private Logger log = LoggerFactory.getLogger(getClass());
    
    private int state;
    private GameInfo gameInfo;
    private Map<Integer, User> userMap;
    
    private GameManage gameManage;
    private JsonUtil jsonUtil = new JsonUtil();
    
    public Game() {
        this.state = GameLoadingState.START;
    }
    
    /**
     * 处理进入的消息
     */
    public void executeMessage(Message message) {
        UserChangeMap userChangeMap = new UserChangeMap();
        
        if (message instanceof PositionMessage) {
            PositionMessage positionMessage = (PositionMessage) message;
            executePositionMessage(positionMessage, userChangeMap);
        } else if (message instanceof PropMessage) {
            PropMessage propMessage = (PropMessage) message;
            executePropMessage(propMessage, userChangeMap);
        } else if (message instanceof SkillMessage){
            SkillMessage skillMessage = (SkillMessage) message;
            executeSkillMessage(skillMessage, userChangeMap);
        } else {
            log.warn("undifined message type:" + message.getClass().getName());
        }
        sync(message.getUserId(), userChangeMap);
    }
    
    /**
     * 同步变化
     */
    private void sync(int userId, UserChangeMap userChangeMap) {
        
        
    }

    /**
     * 处理位置消息
     */
    public void executePositionMessage(PositionMessage message, UserChangeMap userChangeMap) {
        User user = getUser(message.getUserId(), true);
        
        // TODO check if could move
        Position position = new Position(0, 0);
        user.setPosition(position);
    }
    
    /**
     * 处理使用道具消息
     */
    public void executePropMessage(PropMessage message, UserChangeMap userChangeMap) {
        User user = getUser(message.getUserId(), true);
        // check if user prop is enough
        int propId = message.getPropId();
        user.checkProp(propId, 1);
        
        BaseProp baseProp = BaseProp.getProp(propId);
        baseProp.use(userMap);
    }
    
    /**
     * 处理使用技能消息
     */
    public void executeSkillMessage(SkillMessage message, UserChangeMap userChangeMap) {
    }
    
    /**
     * 获取用户
     * @isThrow 在用户不存在的时候是否抛出异常
     */
    private User getUser(int userId, boolean isThrow) {
        User user = userMap.get(userId);
        if (user == null && isThrow) {
            throw new RuntimeException("user is not exist, user id:" + userId);
        } 
        return user;
    }
    
    public abstract void onChange(User user);

    public boolean closeGame(String gameId) throws GameException, TException {
        // TODO Auto-generated method stub
        return false;
    }

    public void setGameManage(GameManage gameManage) {
        this.gameManage = gameManage;
    }

    public GameInfo getGameInfo() {
        return gameInfo;
    }

    public void setGameInfo(GameInfo gameInfo) {
        this.gameInfo = gameInfo;
    }

    public int getState() {
        return state;
    }

    public Map<Integer, User> getUserMap() {
        return userMap;
    }

    public void setUserMap(Map<Integer, User> userMap) {
        this.userMap = userMap;
    }

}
