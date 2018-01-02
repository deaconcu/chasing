package com.prosper.chasing.game.base;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.prosper.chasing.game.message.ReplyMessage;
import com.prosper.chasing.game.service.BuffService;
import com.prosper.chasing.game.util.ByteBuilder;
import com.prosper.chasing.game.util.Constant;
import com.prosper.chasing.game.base.Buff.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.*;

import static com.prosper.chasing.game.base.Game.FROZEN_TIME;

public class User {

    private Logger log = LoggerFactory.getLogger(getClass());

    private static final int NEAR_DISTANCE = 10;

    // 用户所属的游戏
    @JsonIgnore
    private Game game;
    
    // 用户id
    private int id;

    // 用户名称
    private String name;
    
    // 用户位置
    private Position position = new Position();

    // 出生位置
    private Position initPosition = new Position();

    // 速度
    private int speed;

    // 生命值
    private short life;

    // 钱
    private int money;
    
    // 所拥有的道具
    private Map<Short, Byte> propMap = new HashMap<>();
    
    // Buff Map
    private Map<Byte, BaseBuff> buffMap = new HashMap<>();
    
    // 用户状态 @see #User.UserState
    private byte state;

    // 位置是否有修改
    boolean isPositionChanged = false;

    // 生命值是否有修改
    boolean isLifeChanged = false;

    // 速度是否有修改
    boolean isSpeedChanged = false;

    // 状态是否有修改
    boolean isStateChanged = false;

    // 动作列表
    @JsonIgnore
    List<User.Action> actionList = new LinkedList<>();

    // buff变化列表
    @JsonIgnore
    Set<Byte> buffChangedSet = new HashSet<>();

    // 道具变化列表
    @JsonIgnore
    Set<Short> propChangedSet = new HashSet<>();

    // 用户同步消息队列
    @JsonIgnore
    LinkedList<ReplyMessage> messageQueue = new LinkedList<>();

    int messageId = 1;
    int messageOffsetId = 1;

    /**
     * 设置需要发送给用户的消息偏移
     */
    public void updateMessageSeq(int seqId) {
        while(messageQueue.peek() != null && messageQueue.peek().getSeqId() <= seqId) {
            messageQueue.poll();
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // 动作类
    private static class Action {
        short actionId;
        byte code;  // 动作结果标识符 0:接受方，1:成功，2-255:失败
        byte type; // 0:不需要显示影响的类型 1: 生命值变化
        Object value;

        public Action(short actionId, byte code, byte type, Object value) {
            this.code = code;
            this.actionId = actionId;
            this.type = type;
            this.value = value;
        }
    }

    public void addAction(short actionId, byte code, byte type, Object value) {
        if (actionId > 1000) {
            actionList.add(new Action(actionId, code, type, value));
        }
    }

    // 检查道具是否满足要求的数量
    public boolean checkProp(byte propId, byte need) {
        Byte count = propMap.get(propId);
        if (count == null) {
            return false;
        }
        if (count < need) {
            return false;
        }
        return true;
    }

    public void useProp(byte propId) {
        if (propMap.get(propId) > 0) {
            setProp(propId, (byte)(propMap.get(propId) - 1));
        }
    }
    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        if (position.equals(this.position)) return;
        this.position = position;
        isPositionChanged = true;
    }

    public Position getInitPosition() {
        return initPosition;
    }

    public void setInitPosition(Position initPosition) {
        this.initPosition = initPosition;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        if (this.speed == speed) return;
        this.speed = speed;
        isSpeedChanged = true;
    }

    public short getLife() {
        return life;
    }

    public void setLife(short life) {
        if (this.life == life) return;
        this.life = life;
        isLifeChanged = true;
    }

    public Map<Short, Byte> getPropMap() {
        return propMap;
    }

    public void setPropMap(Map<Short, Byte> propMap) {
        this.propMap = propMap;
    }

    public void setProp(short propId, byte count) {
        propMap.put(propId, count);
        propChangedSet.add(propId);
    }

    public byte getProp(short propId) {
        Byte count = propMap.get(propId);
        if (count == null) {
            return 0;
        }
        return count;
    }

    public Map<Byte, Buff.BaseBuff> getBuffMap() {
        return buffMap;
    }

    public void setBuff(BaseBuff buff) {
        if (buffMap.get(buff.id) != null) {
            BaseBuff exist = buffMap.get(buff.id);
            exist.last = buff.last;
            exist.startSecond = buff.startSecond;
        } else {
            buffMap.put(buff.id, buff);
        }
        buffChangedSet.add(buff.id);
    }

    public byte getState() {
        return state;
    }

    public void setState(byte state) {
        if (state != this.state) {
            this.state = state;
            this.isStateChanged = true;
        }
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public void offerMessage(ByteBuilder bb) {
        int seqId = messageId ++;
        bb.set(seqId, 0);
        messageQueue.offer(new ReplyMessage(id, seqId, ByteBuffer.wrap(bb.getBytes())));
    }

    public ReplyMessage nextMessage() {
        return messageQueue.peek();
        /*
        ReplyMessage message = messageQueue.size() > messageOffsetId ? messageQueue.get(messageOffsetId ++) : null;
        if (message == null) {
            messageOffsetId = 0;
            return null;
        } else {
            return message;
        }
        */
    }

    /**
     * 用32个bit位表示buff
     */
    private int getBuffBytes() {
        int value = 0;
        for (BaseBuff buff: buffMap.values()) {
            value = value | (1 << (buff.id - 1));
        }
        return value;
    }

    /**
     * 将修改写成byte[]，用来同步客户端用户数据
     * 格式：
     * seqId(4)
     * messageType(1)
     * sign(2)
     * time(8)
     * envPropCount(2)|list<EnvProp>|
     * NPCCount(2)|list<NPC>|
     * actionCount(2)|list<Action>
     * state(1)
     * moveState(1)|positionX(4)|positionY(4)|positionZ(4)|rotateY(4)
     * lifeValue(2)
     * speedValue(4)
     * buffCount(1)|list<Buff>
     * propCount(1)|list<Prop>
     * userCount(1)|list<UserPosition>
     * userCount(1)|list<UserBuff>
     *
     * sign: reserved(6bit)|envProp(1bit)|npc(1bit)|action(1bit)|
     *       state(1bit)|position(1bit)|life(1bit)|speed(1bit)|
     *       buff(1bit)|prop(1bit)|otherUserPosition(1bit)|otherUserBuff(1bit)|
     * EnvProp: id(2)|seqId(4)|positionX(4)|positionY(4)|positionZ(4)|remainSecond(4)|
     * NPC: id(1)|seqId(4)|moveState(1)|positionX(4)|positionY(4)|positionZ(4)|rotateY(4)
     * Action: id(2)|code(1)|type(1)|value(4)|
     * Buff: buffId(1)|remainSecond(4)|
     * Prop: propId(2)|count(1)|
     * UserPosition userId(4)|moveState(1)|positionX(4)|positionY(4)|positionZ(4)|rotateY(4)
     * UserBuff userId(4)|buffByte(4)
     */
    public ByteBuilder ChangesToBytes() {
        ByteBuilder byteBuilder =  new ByteBuilder();
        int seqId = 0;
        byteBuilder.append(seqId);
        byteBuilder.append(Constant.MessageType.USER);
        short sign = 0;
        byteBuilder.append(sign);
        byteBuilder.append(System.currentTimeMillis());
        if (game.envPropChangedList.size() != 0) {
            if (byteBuilder == null) {
                byteBuilder =  new ByteBuilder();
            }
            sign = (short) (sign | 1024);
            byteBuilder.append((short)game.envPropChangedList.size());
            for (Game.EnvProp envProp: game.envPropChangedList) {
                byteBuilder.append(envProp.propId);
                byteBuilder.append(envProp.seqId);
                byteBuilder.append(envProp.positionPoint.x);
                byteBuilder.append(envProp.positionPoint.y);
                byteBuilder.append(envProp.positionPoint.z);
                byteBuilder.append(envProp.getRemainSecond());
            }
        }
        if (game.getMoveableNPCMap().size() != 0) {
            if (byteBuilder == null) {
                byteBuilder =  new ByteBuilder();
            }
            sign = (short) (sign | 512);
            byteBuilder.append((short)game.getMoveableNPCMap().size());
            for (NPC npc: game.getMoveableNPCMap().values()) {
                if (npc.isPositionChanged()) {
                    byteBuilder.append(npc.getId());
                    byteBuilder.append(npc.getSeqId());
                    byteBuilder.append(npc.getPosition().moveState);
                    byteBuilder.append(npc.getPosition().positionPoint.x);
                    byteBuilder.append(npc.getPosition().positionPoint.y);
                    byteBuilder.append(npc.getPosition().positionPoint.z);
                    byteBuilder.append(npc.getPosition().rotateY);
                }
            }
        }
        if (actionList.size() != 0) {
            if (byteBuilder == null) {
                byteBuilder =  new ByteBuilder();
            }
            sign = (short) (sign | 256);
            byteBuilder.append((short)actionList.size());
            for (Action action: actionList) {
                byteBuilder.append(action.actionId);
                byteBuilder.append(action.code);
                byteBuilder.append(action.type);
                if (action.type == 1) {
                    byteBuilder.append((Integer)action.value);
                }
            }
        }
        if (isStateChanged) {
            if (byteBuilder == null) {
                byteBuilder =  new ByteBuilder();
            }
            sign = (short) (sign | 128);
            byteBuilder.append(state);
        }
        if (isPositionChanged) {
            if (byteBuilder == null) {
                byteBuilder =  new ByteBuilder();
            }
            sign = (short) (sign | 64);
            byteBuilder.append(position.moveState);
            byteBuilder.append(position.positionPoint.x);
            byteBuilder.append(position.positionPoint.y);
            byteBuilder.append(position.positionPoint.z);
            byteBuilder.append(position.rotateY);
        }
        if (isLifeChanged) {
            if (byteBuilder == null) {
                byteBuilder =  new ByteBuilder();
            }
            sign = (short) (sign | 32);
            byteBuilder.append(getLife());
        }
        if (isSpeedChanged) {
            if (byteBuilder == null) {
                byteBuilder =  new ByteBuilder();
            }
            sign = (short) (sign | 16);
            byteBuilder.append(getSpeed());
        }
        if (buffChangedSet.size() != 0) {
            if (byteBuilder == null) {
                byteBuilder =  new ByteBuilder();
            }
            sign = (short) (sign | 8);
            byteBuilder.append((byte)buffChangedSet.size());
            for (byte buffId: buffChangedSet) {
                BaseBuff buff = buffMap.get(buffId);
                byteBuilder.append(buff.id);
                byteBuilder.append(buff.getRemainSecond());
            }
        }
        if (propChangedSet.size() != 0) {
            if (byteBuilder == null) {
                byteBuilder =  new ByteBuilder();
            }
            sign = (short) (sign | 4);
            byteBuilder.append((byte)propChangedSet.size());
            for (short propId: propChangedSet) {
                byteBuilder.append(propId);
                byteBuilder.append(propMap.get(propId));
            }
        }
        if (game.positionChangedSet.size() != 0) {
            if (!(game.positionChangedSet.size() == 1 && game.positionChangedSet.contains(id))) {
                if (byteBuilder == null) {
                    byteBuilder =  new ByteBuilder();
                }
                sign = (short) (sign | 2);
                byteBuilder.append((byte)game.positionChangedSet.size());
                for (int userId: game.positionChangedSet) {
                    Position position = game.getUser(userId).getPosition();
                    byteBuilder.append(userId);
                    byteBuilder.append(position.moveState);
                    byteBuilder.append(position.positionPoint.x);
                    byteBuilder.append(position.positionPoint.y);
                    byteBuilder.append(position.positionPoint.z);
                    byteBuilder.append(position.rotateY);
                }
            }
        }
        if (game.buffChangedSet.size() != 0) {
            if (byteBuilder == null) {
                byteBuilder =  new ByteBuilder();
            }
            sign = (short) (sign | 1);
            byteBuilder.append((byte)game.buffChangedSet.size());
            for (int userId: game.buffChangedSet) {
                byteBuilder.append(userId);
                byteBuilder.append(game.getUser(userId).getBuffBytes());
            }
        }

        if (sign == 0) {
            // 如果没有需要同步的内容，返回null
            return null;
        } else {
            byteBuilder.set(sign, 5);
            return byteBuilder;
        }
    }

    /**
     * 在同步数据后清空修改状态位和数据
     */
    public void clearAfterSync() {
        isLifeChanged = false;
        isPositionChanged = false;
        isSpeedChanged = false;
        isStateChanged = false;

        buffChangedSet.clear();
        propChangedSet.clear();
    }

    /**
     * 获取用户拥有的道具总数
     * @return
     */
    public int getPropCount() {
        int count = 0;
        for (int value: propMap.values()) {
            count += value;
        }
        return count;
    }

    public void purchaseProp(short propId, int price) {
        if (price > money) {
            return;
        }
        money -= price;
        setProp(propId, (byte)(getProp(propId) + 1));
    }

    public void check() {
        List<Byte> removeIdList = new LinkedList<>();
        for(BaseBuff buff: buffMap.values()) {
            if (!checkBuff(buff)) {
                removeIdList.add(buff.id);
            }
        }
        for (Byte id: removeIdList) {
            buffMap.remove(id);
        }

        ReplyMessage replyMessage = nextMessage();
        if (replyMessage != null) {
            // 如果用户在规定时间内都没有响应的消息，判定为掉线，不再发送同步消息，等待用户重新连接
            if (System.currentTimeMillis() - replyMessage.getTimestamp() > FROZEN_TIME) {
                setState(Constant.UserState.OFFLINE);
                log.info("user is offline, user id: {}", getId());
            }
        }
    }

    /**
     * 当buff无效时，返回false, 否则返回true
     */
    protected boolean checkBuff(BaseBuff buff) {
        if (buff instanceof ChasingBuff) {
            ChasingBuff chasingBuff = (ChasingBuff) buff;
            if (chasingBuff.type == ChasingBuff.USER) {
                // TODO
            } else if (chasingBuff.type == ChasingBuff.NPC) {
                NPC npc = game.getMoveableNPCMap().get(chasingBuff.targetId);
                if (npc != null) {
                    boolean isNear = game.isNear(npc.getPosition().positionPoint, position.positionPoint, NEAR_DISTANCE);
                    if (!isNear) {
                        return false;
                    } else if (isNear && buff.getRemainSecond() <= 0) {
                        catchUp(npc);
                    }
                }
            }
        }

        if (buff.getRemainSecond() <= 0) {
            return false;
        }
        return true;
    }

    /**
     * 捕获NPC后的行为
     */
    protected void catchUp(NPC npc) {

    }
}
