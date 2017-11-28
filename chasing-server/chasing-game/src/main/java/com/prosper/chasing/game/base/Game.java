package com.prosper.chasing.game.base;

import java.nio.ByteBuffer;
import java.util.*;

import com.prosper.chasing.game.message.*;
import com.prosper.chasing.game.prop.PropService;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import com.prosper.chasing.game.util.ByteBuilder;
import com.prosper.chasing.game.util.Constant.GameLoadingState;
import com.prosper.chasing.common.util.CommonConstant.GameState;
import com.prosper.chasing.common.util.JsonUtil;

public abstract class Game {
    
    private Logger log = LoggerFactory.getLogger(getClass());

    // 游戏加载状态
    private int state;

    // 游戏元信息
    private GameInfo gameInfo;

    // 参与的用户信息
    private Map<Integer, User> userMap;

    // 游戏场景内的道具信息
    private List<EnvProp> envPropList;

    // 需要同步给用户的数据
    private Map<Integer, Map<String, Object>> userChangeMap;

    private PropService propService = new PropService();

    // 处理游戏逻辑的管理器
    private GameManage gameManage;
    private JsonUtil jsonUtil = new JsonUtil();
    private Random random = new Random();

    // 每一次同步的时候有位置变化和buff变化的用户map
    List<EnvProp> envPropChangedList = new LinkedList<>();
    Set<Integer> positionChangedSet = new HashSet<>();
    Set<Integer> buffChangedSet = new HashSet<>();
    
    public Game() {
        this.state = GameLoadingState.START;
        userMap = new HashMap<>();
    }

    /**
     *游戏场景中的道具
     */
    public static class EnvProp {
        byte propId;
        int positionX;
        int positionY;
        int positionZ;
        long createTime;
        long vanishTime;
        boolean state;

        public int getRemainSecond() {
            return (int)((vanishTime - createTime) / 1000);
        }
    }

    /**
     * 游戏场景中的一些逻辑，比如生成道具
     */
    public void logic() {
        // TODO 需要提取出去作为配置
        int xRange = 100; // 地图上x的范围
        int zRange = 100; // 地图上z的范围
        int propSize = 10;  // 生成道具的数量
        int last = 10000; // 道具持续时间, 单位为毫秒
        byte[] propIds = {1, 2, 3, 4, 5};  // 能够生成的道具id

        ListIterator<EnvProp> iterator = envPropList.listIterator();
        while(iterator.hasNext()) {
            EnvProp envProp = iterator.next();
            if (envProp.vanishTime <= System.currentTimeMillis()) {
                envProp.state = false;
                envPropChangedList.add(envProp);
                iterator.remove();
            }
        }

        while (envPropList.size() < propSize) {
            EnvProp envProp = new EnvProp();
            envProp.propId = propIds[random.nextInt(propIds.length)];
            envProp.positionX = (int) random.nextFloat() * xRange * 1000;
            envProp.positionY = 0;
            envProp.positionZ = (int) random.nextFloat() * zRange * 1000;
            envProp.createTime = System.currentTimeMillis();
            envProp.vanishTime = envProp.createTime + last;
            envProp.state = true;
            envPropChangedList.add(envProp);
        }
        envPropList.addAll(envPropChangedList);
    }
    
    /**
     * 处理进入的消息，主要进行分发
     */
    public void executeMessage(Message message) {
        ActionChange actionChange = null;
        if (message instanceof ConnectMessage) {
            ConnectMessage connectMessage = (ConnectMessage) message;
            executeConnectMessage(connectMessage);
        } else if (message instanceof QuitMessage) {
            QuitMessage quitMessage = (QuitMessage) message;
            executeQuitMessage(quitMessage);
        } else if (message instanceof PositionMessage) {
            PositionMessage positionMessage = (PositionMessage) message;
            executePositionMessage(positionMessage);
        } else if (message instanceof PropMessage) {
            PropMessage propMessage = (PropMessage) message;
            executePropMessage(propMessage);
        } else if (message instanceof SkillMessage){
            SkillMessage skillMessage = (SkillMessage) message;
            executeSkillMessage(skillMessage);
        } else if (message instanceof QuitCompleteMessage){
            QuitCompleteMessage quitCompleteMessage = (QuitCompleteMessage) message;
            executeQuitCompleteMessage(quitCompleteMessage);
        } else {
            log.warn("undefined message type:" + message.getClass().getName());
        }
        //sync(actionChange);
        
        if (state == GameState.FINISHED) {
            gameManage.finishGame(gameInfo.getId());
        }
    }

    public void syncUser() {
        for (User user: userMap.values()) {
            if (user.isPositionChanged) {
                positionChangedSet.add(user.getId());
            }
            if (user.buffChangedSet.size() != 0) {
                buffChangedSet.add(user.getId());
            }
        }

        for (User user: userMap.values()) {
            gameManage.replyData(new ReplyMessage(user.getId(), user.ChangesToBytes()));
        }

        envPropChangedList.clear();
        positionChangedSet.clear();
        buffChangedSet.clear();
        for (User user: userMap.values()) {
            user.clearAfterSync();
        }
    }

    /**
     * 同步变化
     */
    private void sync(ActionChange actionChange) {
        sendPositionMessage(actionChange);
        sendSyncMessage(actionChange);
        sendTargetActionMessage(actionChange);
        sendSourceActionMessage(actionChange);
    }

    /**
     * 发送动作发起方需要得到的消息
     * 消息格式如下
     * 成功的消息：
     * messageType(1bit)|userId(4bit)|action|resultSize(4bit)|result[]
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
            return;
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
     * messageType(1bit)|userId(4bit)|action|result
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
            return;
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
     * messageType(1)|userId(4)|state(1)|time(8)|moveState(1)|positionX(4)|positionY(4)|positionZ(4)|rotationY(4)|
     * buff: buffId(4bit)|count(4bit)
     */
    private void sendPositionMessage(ActionChange actionChange) {
        Action action = actionChange.getAction();
        if (!(action instanceof ActionChange.PositionAction)) return;
        for (int userId: userMap.keySet()) {
            Map<Integer, List<FieldChange>> changeMap = actionChange.getChangeMap();
            for (Integer changedUserId :changeMap.keySet()) {
                ByteBuilder byteBuilder =  new ByteBuilder();
                byteBuilder.append((byte)3);
                User changedUser = userMap.get(changedUserId);
                byteBuilder.append(changedUser.getId());
                byteBuilder.append((byte)changedUser.getState());
                byteBuilder.append(((ActionChange.PositionAction)action).time);
                byteBuilder.append(changedUser.getPosition().moveState);
                byteBuilder.append(changedUser.getPosition().positionX);
                byteBuilder.append(changedUser.getPosition().positionY);
                byteBuilder.append(changedUser.getPosition().positionZ);
                byteBuilder.append(changedUser.getPosition().rotateY);

                ReplyMessage replyMessage = new ReplyMessage();
                replyMessage.setUserId(userId);
                replyMessage.setContent(ByteBuffer.wrap(byteBuilder.getBytes()));
                gameManage.replyData(replyMessage);
            }
        }
    }

    /**
     * 发送需要同步的用户数据
     * 消息格式如下
     * messageType(1bit)|userId(4bit)|state(1bit)|positionX(4bit)|positionY(4bit)|buffSize(4bit)|buff[]
     * buff: buffId(4bit)|count(4bit)
     */
    private void sendSyncMessage(ActionChange actionChange) {
        if (!(actionChange.getAction() instanceof ActionChange.QuitAction) &&
                !(actionChange.getAction() instanceof ActionChange.ConnectAction)) {
            return;
        }
        for (int userId: userMap.keySet()) {
            Map<Integer, List<FieldChange>> changeMap = actionChange.getChangeMap();
            for (Integer changedUserId :changeMap.keySet()) {
                ByteBuilder byteBuilder =  new ByteBuilder();
                byteBuilder.append((byte)4);
                User changedUser = userMap.get(changedUserId);
                byteBuilder.append(changedUser.getId());
                byteBuilder.append((byte)changedUser.getState());
                byteBuilder.append(changedUser.getPosition().positionX);
                byteBuilder.append(changedUser.getPosition().positionY);

                Map<Byte, User.Buff> buffMap = changedUser.getBuffMap();
                if (buffMap != null) {
                    byteBuilder.append((byte)buffMap.size());
                    for (Byte buffId: buffMap.keySet()) {
                        byteBuilder.append(buffId);
                        //byteBuilder.append(buffMap.get(buffId));
                    }
                } else {
                    byteBuilder.append((byte)0);
                }
                ReplyMessage replyMessage = new ReplyMessage();
                replyMessage.setUserId(userId);
                replyMessage.setContent(ByteBuffer.wrap(byteBuilder.getBytes()));
                gameManage.replyData(replyMessage);
            }
        }
    }

    /**
     * 处理连接消息
     * @param message
     * @return
     */
    private ActionChange executeConnectMessage(ConnectMessage message) {
        User user = getUser(message.getUserId(), true);
        int sourceState = user.getState();

        int targetState = UserState.ACTIVE;
        user.setState(targetState);

        // 添加一条退出的变更消息
        ActionChange actionChange = new ActionChange();
        actionChange.setUserId(message.getUserId());
        actionChange.setAction(new ActionChange.ConnectAction());
        actionChange.putChange(message.getUserId(), new StateChange(sourceState, targetState));

        return actionChange;
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
        actionChange.setAction(new ActionChange.QuitAction());
        actionChange.putChange(message.getUserId(), new StateChange(sourceState, targetState));
        
        return actionChange;
    }
    
    /**
     * 处理位置消息
     */
    public void executePositionMessage(PositionMessage message) {
        User user = getUser(message.getUserId(), true);
        
        // TODO check if could move
        Position position = new Position(
                message.moveState, message.positionX, message.positionY, message.positionZ, message.rotationY);
        user.setPosition(position);
    }
    
    /**
     * 处理使用道具消息
     */
    public void executePropMessage(PropMessage message) {
        User user = getUser(message.getUserId(), true);
        User toUser = getUser(message.getToUserId(), false);
        // check if user prop is enough
        int propId = message.getPropId();
        user.checkProp(propId, (byte)1);

        propService.use(propId, user, toUser, userMap);
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
            Position position = new Position((byte)1, 0, 0, 0, 0);
            user.setPosition(position);
            user.setInitPosition(position);
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
