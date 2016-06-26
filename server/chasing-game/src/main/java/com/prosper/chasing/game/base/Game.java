package com.prosper.chasing.game.base;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.prosper.chasing.common.interfaces.data.UserPropTr;
import com.prosper.chasing.common.interfaces.game.GameException;
import com.prosper.chasing.game.base.ActionChange.Action;
import com.prosper.chasing.game.base.ActionChange.FieldChange;
import com.prosper.chasing.game.base.ActionChange.PositionChange;
import com.prosper.chasing.game.base.ActionChange.PropAction;
import com.prosper.chasing.game.base.ActionChange.SkillAction;
import com.prosper.chasing.game.base.ActionChange.BuffChange;
import com.prosper.chasing.game.base.ActionChange.StateChange;
import com.prosper.chasing.game.base.User.UserState;
import com.prosper.chasing.game.message.Message;
import com.prosper.chasing.game.message.QuitCompleteMessage;
import com.prosper.chasing.game.message.Message;
import com.prosper.chasing.game.message.PositionMessage;
import com.prosper.chasing.game.message.PropMessage;
import com.prosper.chasing.game.message.QuitMessage;
import com.prosper.chasing.game.message.ReplyMessage;
import com.prosper.chasing.game.message.SkillMessage;
import com.prosper.chasing.game.prop.BaseProp;
import com.prosper.chasing.game.util.ByteBuilder;
import com.prosper.chasing.game.util.Constant.GameLoadingState;
import com.prosper.chasing.common.util.CommonConstant.GameState;
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
        userMap = new HashMap<>();
    }
    
    /**
     * 处理进入的消息，主要进行分发
     */
    public void executeMessage(Message message) {
        ActionChange actionChange = null;
        if (message instanceof QuitMessage) {
            QuitMessage quitMessage = (QuitMessage) message;
            actionChange = executeQuitMessage(quitMessage);
        } else if (message instanceof PositionMessage) {
            PositionMessage positionMessage = (PositionMessage) message;
            actionChange = executePositionMessage(positionMessage);
        } else if (message instanceof PropMessage) {
            PropMessage propMessage = (PropMessage) message;
            actionChange = executePropMessage(propMessage);
        } else if (message instanceof SkillMessage){
            SkillMessage skillMessage = (SkillMessage) message;
            actionChange = executeSkillMessage(skillMessage);
        } else if (message instanceof QuitCompleteMessage){
            QuitCompleteMessage quitCompleteMessage = (QuitCompleteMessage) message;
            actionChange = executeQuitCompleteMessage(quitCompleteMessage);
        } else {
            log.warn("undifined message type:" + message.getClass().getName());
        }
        sync(actionChange);
        
        if (state == GameState.FINISHED) {
            gameManage.finishGame(gameInfo.getId());
        }
    }

    /**
     * 同步变化
     */
    private void sync(ActionChange actionChange) {
        sendSyncMessage(actionChange);
        sendTargetActionMessage(actionChange);
        sendSourceActionMessage(actionChange);
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
     * buffChange: fieldType(4bit)|buffId(4bit)|action(4bit)
     */
    private void sendSourceActionMessage(ActionChange actionChange) {
        ByteBuilder byteBuilder =  new ByteBuilder();
        byteBuilder.append(1);
        byteBuilder.append(actionChange.getUserId());
        
        Action action = actionChange.getAction();
        if (action == null) {
            return;
        }
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
                } else if (fieldChange instanceof BuffChange) {
                    BuffChange buffChange = (BuffChange) fieldChange;
                    byteBuilder.append(buffChange.buffId);
                    byteBuilder.append(buffChange.action);
                } else {
                    throw new RuntimeException("unknown field change");
                }
            }
        }
        
        ReplyMessage replyMessage = new ReplyMessage();
        replyMessage.setUserId(actionChange.getUserId());
        replyMessage.setContent(ByteBuffer.wrap(byteBuilder.getBytes()));
        gameManage.replyData(replyMessage);
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
    private void sendTargetActionMessage(ActionChange actionChange) {
        ByteBuilder actionByteBuilder =  new ByteBuilder();
        actionByteBuilder.append(actionChange.getUserId());
        
        Action action = actionChange.getAction();
        if (action == null) {
            return;
        }
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
                } else if (fieldChange instanceof BuffChange) {
                    BuffChange stateChange = (BuffChange) fieldChange;
                    byteBuilder.append(stateChange.buffId);
                    byteBuilder.append(stateChange.action);
                } else {
                    throw new RuntimeException("unknown field change");
                }
            }
            
            ReplyMessage replyMessage = new ReplyMessage();
            replyMessage.setUserId(userId);
            replyMessage.setContent(ByteBuffer.wrap(byteBuilder.getBytes()));
            gameManage.replyData(replyMessage);
        }
    }

    /**
     * 发送需要同步的用户数据
     * 消息格式如下
     * messageType(4bit)|userId(4bit)|positionX(4bit)|positionY(4bit)|buffSize(4bit)|buff[]|state
     * buff: buffId(4bit)|count(4bit)
     */
    private void sendSyncMessage(ActionChange actionChange) {
        for (int userId: userMap.keySet()) {
            Map<Integer, List<FieldChange>> changeMap = actionChange.getChangeMap();
            for (Integer changedUserId :changeMap.keySet()) {
                
                
                ByteBuilder byteBuilder =  new ByteBuilder();
                User changedUser = userMap.get(changedUserId);
                byteBuilder.append(changedUser.getId());
                byteBuilder.append(changedUser.getPosition().x);
                byteBuilder.append(changedUser.getPosition().y);
                byteBuilder.append(changedUser.getPosition().z);
                
                Map<Integer, Integer> buffMap = changedUser.getBuffMap();
                if (buffMap != null) {
                    for (Integer buffId: buffMap.keySet()) {
                        byteBuilder.append(buffId);
                        byteBuilder.append(buffMap.get(buffId));
                    }
                }
                byteBuilder.append(changedUser.getState());
                
                ReplyMessage replyMessage = new ReplyMessage();
                replyMessage.setUserId(userId);
                replyMessage.setContent(ByteBuffer.wrap(byteBuilder.getBytes()));
                gameManage.replyData(replyMessage);
            }
        }
    }
    
    /**
     * 处理退出消息
     */
    private ActionChange executeQuitMessage(QuitMessage message) {
        // 将user状态置为正在删除
        User user = getUser(message.getUserId(), true);
        int sourceState = user.getState();
        int targetState = UserState.QUITING;
        user.setState(targetState);
        
        // 插入用户待同步队列，如果插入失败，回滚初始状态
        if (!gameManage.addUserForDataDB(user)) {
            user.setState(sourceState);
        }
        
        // 添加一条退出的变更消息
        ActionChange actionChange = new ActionChange();
        actionChange.setUserId(message.getUserId());
        actionChange.setAction(null);
        actionChange.putChange(message.getUserId(), new StateChange(sourceState, targetState));
        
        return actionChange;
    }
    
    /**
     * 处理位置消息
     */
    public ActionChange executePositionMessage(PositionMessage message) {
        User user = getUser(message.getUserId(), true);
        
        // TODO check if could move
        Position position = new Position(message.positionX, message.positionY, message.positionZ);
        user.setPosition(position);
        
        ActionChange actionChange = new ActionChange();
        actionChange.setUserId(message.getUserId());
        actionChange.setAction(null);
        
        actionChange.putChange(message.getUserId(), new PositionChange());
        return actionChange;
    }
    
    /**
     * 处理使用道具消息
     */
    public ActionChange executePropMessage(PropMessage message) {
        ActionChange actionChange = new ActionChange();
        
        User user = getUser(message.getUserId(), true);
        User toUser = getUser(message.getToUserId(), false);
        // check if user prop is enough
        int propId = message.getPropId();
        user.checkProp(propId, 1);
        
        PropAction action = new ActionChange.PropAction();
        actionChange.setAction(action);
        action.propId = propId;
        
        BaseProp baseProp = BaseProp.getProp(propId);
        baseProp.testUse(user, toUser, userMap, action);
        
        if(action.opCode == 0) {
            baseProp.use(user, toUser, userMap, actionChange);
        }
        return actionChange;
    }
    
    /**
     * 处理使用技能消息
     */
    public ActionChange executeSkillMessage(SkillMessage message) {
        return null;
    }
    
    /**
     * 处理退出完成消息
     */
    private ActionChange executeQuitCompleteMessage(QuitCompleteMessage quitCompleteMessage) {
        int userId = quitCompleteMessage.getUserId();
        User user = getUser(userId);
        
        int sourceState = user.getState();
        int targetState = UserState.QUIT;
        user.setState(targetState);
        
        ActionChange actionChange = new ActionChange();
        actionChange.setUserId(quitCompleteMessage.getUserId());
        
        FieldChange fieldChange = new StateChange(sourceState, sourceState);
        actionChange.putChange(userId, fieldChange);
        
        // 判断是否全部用户都已退出游戏，如果是，将游戏状态置为完成
        boolean allQuit = true;
        for (User userInGame: userMap.values()) {
            if (userInGame.getState() == UserState.ACTIVE) {
                allQuit = false;
                break;
            }
        }
        
        if (allQuit) {
            state = GameState.FINISHED;
        }
        return actionChange;
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
    
    public boolean closeGame(String gameId) throws GameException, TException {
        // TODO Auto-generated method stub
        return false;
    }
    
    /**
     * 获取用户
     */
    public User getUser(int userId) {
        return userMap.get(userId);
    }
    
    /**
     * 加载用户
     */
    public void loadUser(List<User> userList) {
        for (User user: userList) {
            Position position = new Position(0, 0, 0);
            user.setPosition(position);
            userMap.put(user.getId(), user);
        }
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
    


}
