package com.prosper.chasing.game.base;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.prosper.chasing.common.interfaces.game.GameException;
import com.prosper.chasing.game.base.ActionChange.Action;
import com.prosper.chasing.game.base.ActionChange.FieldChange;
import com.prosper.chasing.game.base.ActionChange.PositionChange;
import com.prosper.chasing.game.base.ActionChange.PropAction;
import com.prosper.chasing.game.base.ActionChange.SkillAction;
import com.prosper.chasing.game.base.ActionChange.StateChange;
import com.prosper.chasing.game.message.Message;
import com.prosper.chasing.game.message.PositionMessage;
import com.prosper.chasing.game.message.PropMessage;
import com.prosper.chasing.game.message.SkillMessage;
import com.prosper.chasing.game.prop.BaseProp;
import com.prosper.chasing.game.util.ByteBuilder;
import com.prosper.chasing.game.util.Constant.GameLoadingState;
import com.prosper.chasing.common.util.JsonUtil;

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
        ActionChange syncMessage = null;
        if (message instanceof PositionMessage) {
            PositionMessage positionMessage = (PositionMessage) message;
            syncMessage = executePositionMessage(positionMessage);
        } else if (message instanceof PropMessage) {
            PropMessage propMessage = (PropMessage) message;
            syncMessage = executePropMessage(propMessage);
        } else if (message instanceof SkillMessage){
            SkillMessage skillMessage = (SkillMessage) message;
            syncMessage = executeSkillMessage(skillMessage);
        } else {
            log.warn("undifined message type:" + message.getClass().getName());
        }
        sync(message, syncMessage);
    }
    
    /**
     * 同步变化
     */
    private void sync(Message message, ActionChange actionChange) {
        sendSyncMessage(message, actionChange);
        sendTargetActionMessage(message, actionChange);
        sendSourceActionMessage(message, actionChange);
    }

    /**
     * 发送动作发起方需要得到的消息
     * 消息格式如下
     * 成功的消息：
     * messageType(4bit)|userId(4bit)|action|resultSize(4bit)|result[]
     * 
     * action:actionType(4bit)|actionResult(4bit)|actionItemId(4bit)
     * result:toUserId(4bit)|fieldResultSize(4bit)|fieldChange[]
     * positionChange: fieldType(4bit)|name(4bit)|value(4bit)
     * stateChange: fieldType(4bit)|stateId(4bit)|action(4bit)
     */
    private void sendSourceActionMessage(Message message, ActionChange actionChange) {
        Message sendMessage = new Message();
        sendMessage.setUserId(message.getUserId());
        
        ByteBuilder byteBuilder =  new ByteBuilder();
        byteBuilder.append(1);
        byteBuilder.append(actionChange.getUserId());
        
        Action action = actionChange.getAction();
        if (action instanceof PropAction) {
            PropAction propAction = (PropAction) action;
            byteBuilder.append(1);
            byteBuilder.append(propAction.opCode);
            if (propAction.opCode == 0) {
                byteBuilder.append(propAction.propId);
            }
        } else if (action instanceof SkillAction) {
            SkillAction skillAction = (SkillAction) action;
            byteBuilder.append(1);
            byteBuilder.append(skillAction.opCode);
            if (skillAction.opCode == 0) {
                byteBuilder.append(skillAction.skillId);
            }
        } else {
            throw new RuntimeException("unknown action");
        }
        
        Map<Integer, List<FieldChange>> changeMap = actionChange.getChangeMap();
        byteBuilder.append(changeMap.size());
        for (Integer userId :changeMap.keySet()) {
            byteBuilder.append(userId);
            List<FieldChange> fieldChangeList = changeMap.get(userId);
            byteBuilder.append(fieldChangeList.size());
            for (FieldChange fieldChange: fieldChangeList) {
                if (fieldChange instanceof PositionChange) {
                    PositionChange positionChange = (PositionChange) fieldChange;
                    byteBuilder.append(positionChange.name);
                    byteBuilder.append(positionChange.value);
                } else if (fieldChange instanceof StateChange) {
                    StateChange stateChange = (StateChange) fieldChange;
                    byteBuilder.append(stateChange.stateId);
                    byteBuilder.append(stateChange.action);
                } else {
                    throw new RuntimeException("unknown field change");
                }
            }
        }
        
        message.setContent(ByteBuffer.wrap(byteBuilder.getBytes()));
        gameManage.sendData(message);
    }

    /**
     * 发送动作接收方需要得到的消息
     * 消息格式如下
     * 成功的消息：
     * messageType(4bit)|userId(4bit)|action|result
     * 
     * action:actionType(4bit)|actionResult(4bit)|actionItemId(4bit)
     * result:toUserId(4bit)|fieldResultSize(4bit)|fieldChange[]
     * positionChange: fieldType(4bit)|name(4bit)|value(4bit)
     * stateChange: fieldType(4bit)|stateId(4bit)|action(4bit)
     */
    private void sendTargetActionMessage(Message message, ActionChange actionChange) {
        ByteBuilder actionByteBuilder =  new ByteBuilder();
        actionByteBuilder.append(actionChange.getUserId());
        
        Action action = actionChange.getAction();
        if (action instanceof PropAction) {
            PropAction propAction = (PropAction) action;
            actionByteBuilder.append(1);
            actionByteBuilder.append(propAction.opCode);
            if (propAction.opCode != 0) {
                return;
            }
            actionByteBuilder.append(propAction.propId);
        } else if (action instanceof SkillAction) {
            SkillAction skillAction = (SkillAction) action;
            actionByteBuilder.append(1);
            actionByteBuilder.append(skillAction.opCode);
            if (skillAction.opCode != 0) {
            }
            actionByteBuilder.append(skillAction.skillId);
        } else {
            throw new RuntimeException("unknown action");
        }
        
        Map<Integer, List<FieldChange>> changeMap = actionChange.getChangeMap();
        for (Integer userId :changeMap.keySet()) {
            Message sendMessage = new Message();
            sendMessage.setUserId(userId);
            
            ByteBuilder byteBuilder =  new ByteBuilder();
            byteBuilder.append(2);
            byteBuilder.append(actionByteBuilder.getBytes());
            byteBuilder.append(userId);
            List<FieldChange> fieldChangeList = changeMap.get(userId);
            byteBuilder.append(fieldChangeList.size());
            for (FieldChange fieldChange: fieldChangeList) {
                if (fieldChange instanceof PositionChange) {
                    PositionChange positionChange = (PositionChange) fieldChange;
                    byteBuilder.append(positionChange.name);
                    byteBuilder.append(positionChange.value);
                } else if (fieldChange instanceof StateChange) {
                    StateChange stateChange = (StateChange) fieldChange;
                    byteBuilder.append(stateChange.stateId);
                    byteBuilder.append(stateChange.action);
                } else {
                    throw new RuntimeException("unknown field change");
                }
            }
            message.setContent(ByteBuffer.wrap(byteBuilder.getBytes()));
            gameManage.sendData(message);
        }
    }

    /**
     * 发送需要同步的用户数据
     * 消息格式如下
     * messageType(4bit)|userId(4bit)|positionX(4bit)|positionY(4bit)|stateSize(4bit)|state[]
     * state: stateId(4bit)|count(4bit)
     */
    private void sendSyncMessage(Message message, ActionChange actionChange) {
        for (int userId: userMap.keySet()) {
            Map<Integer, List<FieldChange>> changeMap = actionChange.getChangeMap();
            for (Integer changedUserId :changeMap.keySet()) {
                Message sendMessage = new Message();
                sendMessage.setUserId(userId);
                
                ByteBuilder byteBuilder =  new ByteBuilder();
                User changedUser = userMap.get(changedUserId);
                byteBuilder.append(2);
                byteBuilder.append(changedUser.getPosition().getX());
                byteBuilder.append(changedUser.getPosition().getY());
                
                Map<Integer, Integer> stateMap = changedUser.getStateMap();
                for (Integer stateId: stateMap.keySet()) {
                    byteBuilder.append(stateId);
                    byteBuilder.append(stateMap.get(stateId));
                }
                message.setContent(ByteBuffer.wrap(byteBuilder.getBytes()));
                gameManage.sendData(message);
            }
        }
    }

    /**
     * 处理位置消息
     * @return 
     */
    public ActionChange executePositionMessage(PositionMessage message) {
        User user = getUser(message.getUserId(), true);
        
        // TODO check if could move
        Position position = new Position(0, 0);
        user.setPosition(position);
        return null;
    }
    
    /**
     * 处理使用道具消息
     * @return 
     */
    public ActionChange executePropMessage(PropMessage message) {
        ActionChange syncMessage = new ActionChange();
        
        User user = getUser(message.getUserId(), true);
        User toUser = getUser(message.getToUserId(), false);
        // check if user prop is enough
        int propId = message.getPropId();
        user.checkProp(propId, 1);
        
        PropAction action = new ActionChange.PropAction();
        syncMessage.setAction(action);
        action.propId = propId;
        
        BaseProp baseProp = BaseProp.getProp(propId);
        baseProp.testUse(user, toUser, userMap, action);
        
        if(action.opCode == 0) {
            baseProp.use(user, toUser, userMap, syncMessage);
        }
        return syncMessage;
    }
    
    /**
     * 处理使用技能消息
     * @return 
     */
    public ActionChange executeSkillMessage(SkillMessage message) {
        return null;
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
