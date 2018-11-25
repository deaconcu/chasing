package com.prosper.chasing.game.base;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.prosper.chasing.game.message.ReplyMessage;
import com.prosper.chasing.game.util.ByteBuilder;
import com.prosper.chasing.game.util.Constant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.*;

import static com.prosper.chasing.game.base.Game.FROZEN_TIME;

public class User extends GameObject {

    private Logger log = LoggerFactory.getLogger(getClass());

    private static final int NEAR_DISTANCE = 10;
    private static final int REPLY_GAP = 1000;

    private static final int PROP_PACKAGE_MAX_SIZE = 10;

    // 用户所属的游戏
    @JsonIgnore
    private Game game;
    
    // 用户id
    private int id;

    // 用户名称
    private String name;

    // 队伍id
    private byte groupId;

    // 运动姿态,比如跑步或者步行
    public byte moveState;

    // 运动步数
    private int steps;

    // 速度
    private int speed;

    // 生命值
    private short life = 1;

    // 生命最大值
    protected short maxLife = 1;

    // 钱
    private int money;
    
    // 所拥有的道具
    private Map<Short, Short> propMap = new HashMap<>();
    
    // Buff Map
    private Map<String, Buff> buffMap = new HashMap<>();

    // 周围追逐当前用户的玩家信息
    private ChasingInfo chasingInfo;
    
    // 用户状态 @see #Constant.UserState
    private byte state;

    // 生命值是否有修改
    boolean isLifeChanged = false;

    // 速度是否有修改
    boolean isSpeedChanged = false;

    // 状态是否有修改
    boolean isStateChanged = false;

    // 目标对象是否有修改
    boolean isTargetChanged = false;

    // 自定义属性是否有变化
    public boolean isCustomPropertyChanged = false;

    // 结束游戏的时间
    public long gameOverTime;

    // 动作列表
    @JsonIgnore
    List<User.Action> actionList = new LinkedList<>();

    // buff变化列表
    @JsonIgnore
    Set<Buff> buffChangedSet = new HashSet<>();

    // 道具变化列表
    @JsonIgnore
    Set<Short> propChangedSet = new HashSet<>();

    // 用户同步消息队列
    @JsonIgnore
    //LinkedList<ReplyMessage> messageQueue = new LinkedList<>();

    TreeMap<Integer, ReplyMessage> messageSendMap = new TreeMap<>();

    // 下一个消息需要设置的seqId
    int messageId = 1;

    // 当前发送完成的消息位置
    int sendSeqId = 0;

    // 队列中最早的消息id
    int earliestSeqId = 0;

    // 需要重新发送的消息seqId列表
    List<Integer> resendSeqIdList;

    // 使用类型 1：用户，2：道具，3：位置
    //private byte targetType;

    // 目标对象的id
    //private int targetId;

    // 目标位置
    //private Point targetPoint;

    private Object targetObject;

    private Map<Integer, ChasingInfo> chasingInfoMap = new HashMap<>();

    public User(int id, Point point, int rotateY) {
        super(id, point, rotateY);
    }

    public byte getGroupId() {
        return groupId;
    }

    public void setGroupId(byte groupId) {
        this.groupId = groupId;
    }

    public Map<Integer, ChasingInfo> getChasingInfoMap() {
        return chasingInfoMap;
    }



    /********************************
     * 追逐目标和追逐进度
     ********************************/

    public enum ChasingStep {
        CHASING, CATCHING
    }

    public static class Progress {
        int percent;
        long startTime;
        ChasingStep step;

        Progress() {
            percent = 0;
            startTime = System.currentTimeMillis();
            step = ChasingStep.CHASING;
        }
    }

    public static class ChasingInfo {
        User chasingUser;   // 追逐的玩家
        long startTime;     // 追逐开始的时间

        ChasingInfo(User chasingUser) {
            this.chasingUser = chasingUser;
            this.startTime = 0;
        }
    }

    /**
     * 获得当前目标对象
     * @return null 如果目标对象不存在返回null
     */
    public Object getTargetObject() {
        return targetObject;
    }

    public GameObject getTargetObject(byte type, int id) {
        if (type == Constant.TargetType.TYPE_PROP) {
            return game.getProp(id);
        } else if (type == Constant.TargetType.TYPE_NPC) {
            return game.getNPC(id);
        } else if (type == Constant.TargetType.TYPE_USER) {
            return game.getUser(id);
        }
        return null;
    }

    public byte getTargetType() {
        if (targetObject instanceof Point) return Constant.TargetType.TYPE_POSITION;
        else if (targetObject instanceof PropConfig.Prop) return Constant.TargetType.TYPE_PROP;
        else if (targetObject instanceof User) return Constant.TargetType.TYPE_USER;
        else return Constant.TargetType.TYPE_NONE;
    }

    /**
     * 设置目标对象
     * @return 修改成功返回true，否则返回false
     */
    public boolean setTarget(byte type, int id, Point point) {
        if (type == Constant.TargetType.TYPE_POSITION)  {
            if (targetObject.equals(point)) {
                return false;
            }
            // 设置当前目标
            targetObject = point;
        } else {
            // 如果target和当前目标一致，不做处理
            Object intendTargetObject = getTargetObject(type, id);
            if (targetObject.equals(intendTargetObject) || intendTargetObject == null) {
                return false;
            }

            if (targetObject instanceof User) {
                chasingInfoMap.remove(((User) targetObject).getId());
            }

            if (intendTargetObject instanceof User) {
                User intendTargetUser = (User) intendTargetObject;
                chasingInfoMap.put(intendTargetUser.getId(), new ChasingInfo(intendTargetUser));
            }

            // 设置当前目标
            targetObject = intendTargetObject;
        }

        isTargetChanged = true;
        return true;
    }

    /**
     * 计算追逐进度
     */
    /*
    public void countMovableTargetProgress() {
        Object target = getTargetObject(targetType, targetId);
        if (target != null && target instanceof MovableObject) {
            MovableObject movableObject = (MovableObject) target;
            if (movableObject.movable) {
                if (progress == null) progress = new Progress();
                int distance = position.point.distance(movableObject.position.point);
                if (distance > DISTANCE_CHASING) {
                    progress.percent = 0;
                    progress.startTime = System.currentTimeMillis();
                } else if (distance > DISTANCE_CATCHING && progress.step == ChasingStep.CHASING) {
                    float percent = (float) (System.currentTimeMillis() - progress.startTime) / SECOND_CHASING * 1000;
                    progress.percent = percent > 1 ? 1 : (int) percent * 100;
                } else if (distance > DISTANCE_CATCHING && progress.step == ChasingStep.CATCHING) {
                    progress.step = ChasingStep.CHASING;
                    progress.percent = 0;
                } else if (distance < DISTANCE_CATCHING && progress.step == ChasingStep.CHASING) {
                    progress.step = ChasingStep.CATCHING;
                    progress.percent = 0;
                } else {
                    if (progress.percent < 1) {
                        float percent = (float) (System.currentTimeMillis() - progress.startTime) / SECOND_CATCHING * 1000;
                        progress.percent = percent > 1 ? 1 : (int) percent * 100;
                    }

                    if (progress.percent == 1) {
                        movableObject.catched(this);
                    }
                }
            } else {
                int distance = position.point.distance(movableObject.position.point);
                if (distance < DISTANCE_CATCHING_STATIC) {
                    movableObject.catched(this);
                }
            }
        }
    }
    */

    /********************************
     * 动作相关
     ********************************/

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

    /********************************
     * 道具相关
     ********************************/

    /**
     * 检查道具是否满足要求的数量
     */
    public boolean checkProp(short propId, int need) {
        int count = getProp(propId);
        if (count < need) {
            return false;
        }
        return true;
    }

    /**
     * 减少道具,用于使用道具和转移道具
     * @return true 执行成功 false 执行失败
     */
    public boolean reduceProp(short propId, short count) {
        if (getProp(propId) < count) {
            return false;
        }
        setProp(propId, (short) (propMap.get(propId) - count));
        return true;
    }

    /**
     * 增加道具
     * @return true 执行成功 false 执行失败
     */
    public boolean increaseProp(short propId, short count) {
        if (getPackagePropCount() + count > PROP_PACKAGE_MAX_SIZE) {
            return false;
        }
        setProp(propId, (short) (propMap.get(propId) + count));
        return true;
    }

    public Map<Short, Short> getPropMap() {
        return propMap;
    }

    public void setPropMap(Map<Short, Short> propMap) {
        this.propMap = propMap;
    }

    public void setProp(short propId, short count) {
        propMap.put(propId, count);
        propChangedSet.add(propId);
    }

    public Short getProp(short propId) {
        Short count = propMap.get(propId);
        if (count == null) {
            return 0;
        }
        return count;
    }

    /**
     * 获取用户拥有的道具总数
     */
    public int getPackagePropCount() {
        int count = 0;
        for (short propTypeId: propMap.keySet()) {
            PropConfig.Prop prop = PropConfig.getProp(propTypeId);
            if (prop.isInPackage) {
                count += propMap.get(propTypeId);
            }
        }
        return count;
    }

    /**
     * 购买道具
     */
    public void purchaseProp(short propId, int price) {
        if (price > money) {
            return;
        }
        money -= price;
        setProp(propId, (short) (getProp(propId) + 1));
    }

    public boolean isPackageFull(int count) {
        if (getPackagePropCount() + count > PROP_PACKAGE_MAX_SIZE) {
            return true;
        }
        return false;
    }

    /********************************
     * buff 相关
     ********************************/

    /**
     * 增加buff
     * @param buffId buff id
     * @param values buff附带的一些信息, 比如跟随的时候需要跟随的用户id
     */
    public void addBuff(byte buffId, Object... values) {
        if (BuffConfig.getBuff(buffId) == null) {
            log.warn("buff is not exist" + buffId);
        }

        Buff buff = new Buff(BuffConfig.getBuff(buffId), values);
        buffMap.put(Byte.toString(buffId), buff);
        buffChangedSet.add(buff);
    }

    public void addBuff(byte buffId, int groupId, Object... values)  {
        String id = Byte.toString(buffId) + "-" + Integer.toString(groupId);
        if (buffMap.containsKey(id)) return;

        Buff buff = new Buff(BuffConfig.getBuff(buffId), groupId, values);
        buffMap.put(id, buff);
        buffChangedSet.add(buff);
    }

    /**
     * 增加buff
     */
    public void addBuff(byte buffId) {
        addBuff(buffId, null);
    }

    /**
     * 移除buff
     */
    public void removeBuff(byte buffId) {
        // TODO NEED REWRITE
        Buff buff = buffMap.remove(buffId);
        buffChangedSet.add(buff);
    }

    /**
     * 清除用户身上的地形buff
     */
    public void clearTerrainBuff() {
        List<String> removeKeyList = new LinkedList<>();
        for (String key: buffMap.keySet()) {
            if (buffMap.get(key).groupId > 0) {
                removeKeyList.add(key);
            }
        }
        for (String key: removeKeyList) {
            buffChangedSet.add(buffMap.remove(key));
        }
    }

    /**
     * 判断用户是否有buff
     */
    public boolean hasBuffer(short bufferId) {
        return buffMap.containsKey(bufferId);
    }

    /**
     * 获取buff上的信息
     */
    public List<Object> getBufferInfo(byte bufferId) {
        if (!hasBuffer(bufferId)) return null;
        return buffMap.get(bufferId).valueList;
    }

    /**
     * 用32个bit位表示buff
     */
    public int getBuffBytes() {
        int value = 0;
        for (Buff buff: buffMap.values()) {
            value = value | (1 << (buff.id - 1));
        }
        return value;
    }

    /********************************
     * 生命值相关
     ********************************/

    public short getLife() {
        return life;
    }

    public void setLife(short life) {
        if (this.life == life || life > maxLife) return;
        this.life = life;
        isLifeChanged = true;
    }

    public void addOneLife() {
        if (this.life >= maxLife) return;
        else this.life ++;
    }

    public void reduceOnelife() {
        if (this.life > 0) {
            this.life --;
        }
    }

    public void maxLife() {
        this.life = maxLife;
    }

    /********************************
     * 其他一些属性
     ********************************/

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        if (this.speed == speed) return;
        this.speed = speed;
        isSpeedChanged = true;
    }

    public byte getMoveState() {
        return moveState;
    }

    public void setMoveState(byte moveState) {
        this.moveState = moveState;
        setMoved(true);
    }




    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public int getMoney() {
        return money;
    }

    public boolean modifyMoney(int amount) {
        if (money + amount >= 0) {
            money += amount;
            return true;
        }
        return false;
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public int getSteps() {
        return steps;
    }

    public void setSteps(int steps) {
        this.steps = steps;
    }

    public void addSteps(short steps) {
        this.steps += steps;
    }


    /********************************
     * 消息相关
     ********************************/

    /**
     * 设置需要发送给用户的消息偏移
     */
    public void updateMessageSeq(int seqId) {
        NavigableMap<Integer, ReplyMessage> deleteMap = messageSendMap.headMap(seqId, true);
        List<Integer> deleteList = new LinkedList<>();
        for (int deleteSeqId: deleteMap.keySet()) {
            deleteList.add(deleteSeqId);
        }
        for (int deleteSeqId: deleteList) {
            messageSendMap.remove(deleteSeqId);
        }
        earliestSeqId = seqId + 1;
    }

    /**
     * 发送消息到发送队列
     */
    public void offerMessage(ByteBuilder bb) {
        if (bb == null || bb.getSize() == 0) {
            return;
        }
        int seqId = messageId ++;
        bb.set(seqId, 0);


        messageSendMap.put(seqId, new ReplyMessage(id, seqId, ByteBuffer.wrap(bb.getBytes())));
    }

    /**
     * 获得下一个需要发送的消息，需要发送消息的时候调用
     */
    public ReplyMessage nextMessage() {
        if (resendSeqIdList != null && resendSeqIdList.size() > 0) {
            return messageSendMap.get(resendSeqIdList.remove(0));
        } else if (sendSeqId < messageId) {
            return messageSendMap.get(sendSeqId ++);
        } else {
            return null;
        }
    }

    /**
     * 获得消息队列中的第一个最早的消息
     * @return
     */
    public ReplyMessage firstMessage() {
        return messageSendMap.get(earliestSeqId);
    }

    /**
     * 将修改写成byte[]，用来同步客户端用户数据
     * 格式：
     * seqId(4)
     * messageType(1)
     * sign(2)
     * time(8)
     * targetType(1)|targetId(4)|
     * step(1)
     * envPropCount(2)|list<EnvProp>|
     * NPCCount(2)|list<NPCOld>|
     * actionCount(2)|list<Action>
     * state(1)
     * moveState(1)|positionX(4)|positionY(4)|positionZ(4)|rotateY(4)
     * lifeValue(2)
     * speedValue(4)
     * buffCount(1)|list<Buff>
     * propCount(1)|list<Prop>
     * customProperty 详见各自定义user类
     * userCount(1)|list<UserPosition>
     * userCount(1)|list<UserBuff>
     *
     * sign: reserved(2bit)|target|step|envProp|npc|action|state|
     *       position|life|speed|buff|prop|customProperty|otherUserPosition|otherUserBuff|
     * EnvProp: id(2)|seqId(4)|positionX(4)|positionY(4)|positionZ(4)|remainSecond(4)|
     * NPCOld: id(1)|seqId(4)|moveState(1)|positionX(4)|positionY(4)|positionZ(4)|rotateY(4)
     * Action: id(2)|code(1)|type(1)|value(4)|
     * Buff: buffId(1)|remainSecond(4)|
     * Prop: propId(2)|count(2)|
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
        if (isTargetChanged) {
            if (byteBuilder == null) {
                byteBuilder =  new ByteBuilder();
            }
            sign = (short) (sign | 8192);

            byte targetType = getTargetType();
            byteBuilder.append(targetType);
            if (targetObject instanceof Point) {
                byteBuilder.append(((Point) targetObject).x);
                byteBuilder.append(((Point) targetObject).y);
                byteBuilder.append(((Point) targetObject).z);
            } else {
                byteBuilder.append(((GameObject) targetObject).getId());
            }
        }

        if (actionList.size() != 0) {
            if (byteBuilder == null) {
                byteBuilder =  new ByteBuilder();
            }
            sign = (short) (sign | 512);
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
            sign = (short) (sign | 256);
            byteBuilder.append(state);
        }
        if (isMoved()) {
            if (byteBuilder == null) {
                byteBuilder =  new ByteBuilder();
            }
            sign = (short) (sign | 128);
            byteBuilder.append(moveState);
            byteBuilder.append(getPoint().x);
            byteBuilder.append(getPoint().y);
            byteBuilder.append(getPoint().z);
            byteBuilder.append(getRotateY());
        }
        if (isLifeChanged) {
            if (byteBuilder == null) {
                byteBuilder =  new ByteBuilder();
            }
            sign = (short) (sign | 64);
            byteBuilder.append(getLife());
        }
        if (isSpeedChanged) {
            if (byteBuilder == null) {
                byteBuilder =  new ByteBuilder();
            }
            sign = (short) (sign | 32);
            byteBuilder.append(getSpeed());
        }
        if (buffChangedSet.size() != 0) {
            if (byteBuilder == null) {
                byteBuilder =  new ByteBuilder();
            }
            sign = (short) (sign | 16);
            byteBuilder.append((byte)buffChangedSet.size());
            for (Buff buff: buffChangedSet) {
                byteBuilder.append(buff.id);
                byteBuilder.append(buff.getRemainSecond());
            }
        }
        if (propChangedSet.size() != 0) {
            if (byteBuilder == null) {
                byteBuilder =  new ByteBuilder();
            }
            sign = (short) (sign | 8);
            byteBuilder.append((byte)propChangedSet.size());
            for (short propId: propChangedSet) {
                byteBuilder.append(propId);
                byteBuilder.append(propMap.get(propId));
            }
        }
        if (isCustomPropertyChanged) {
            if (byteBuilder == null) {
                byteBuilder =  new ByteBuilder();
            }
            sign = (short) (sign | 4);
            byteBuilder.append(getCustomPropertyBytes());
        }

        if (sign == 0) {
            // 如果没有需要同步的内容，返回null
            return null;
        } else {
            byteBuilder.set(sign, 5);
            return byteBuilder;
        }
    }

    public byte[] getCustomPropertyBytes() {
        return null;
        // for override
    }

    /**
     * 在同步数据后清空修改状态位和数据
     */
    public void clearAfterSync() {
        isTargetChanged = false;
        isLifeChanged = false;
        isSpeedChanged = false;
        isStateChanged = false;
        isCustomPropertyChanged = false;
        setMoved(false);

        buffChangedSet.clear();
        propChangedSet.clear();
    }

    /********************************
     * 其它
     ********************************/

    /**
     * 检查用户，比如是否掉线，是否结束游戏，移除无效buff等
     */
    public void check() {
        // 检查buff是否有效，移除无效buff
        List<Byte> removeIdList = new LinkedList<>();
        for(Buff buff: buffMap.values()) {
            if (!checkBuff(buff)) {
                removeIdList.add(buff.id);
            }
        }
        for (Byte id: removeIdList) {
            buffMap.remove(id);
        }

        // 如果队列中存在消息，说明用户有延时，用延时的时长来判断是否掉线
        ReplyMessage replyMessage = firstMessage();
        if (replyMessage != null) {
            // 如果用户在规定时间内都没有响应的消息，判定为掉线，不再发送同步消息，等待用户重新连接
            if (System.currentTimeMillis() - replyMessage.getTimestamp() > FROZEN_TIME &&
                    getState() != Constant.UserState.OFFLINE) {
                //setState(Constant.UserState.OFFLINE);
                //log.info("user is offline, user id: {}", getId());
            }
        }

        checkIfEnd();
    }

    /**
     * 当buff无效时，返回false, 否则返回true
     */
    protected boolean checkBuff(Buff buff) {
        /*
        if (buff instanceof ChasingBuff) {
            ChasingBuff chasingBuff = (ChasingBuff) buff;
            if (chasingBuff.type == ChasingBuff.USER) {
                // TODO
            } else if (chasingBuff.type == ChasingBuff.NPCOld) {
                NPCOld npc = game.getMoveableNPCMap().get(chasingBuff.targetId);
                if (npc != null) {
                    boolean isNear = game.isNear(npc.getPositionInfo().point, position.point, NEAR_DISTANCE);
                    if (!isNear) {
                        return false;
                    } else if (isNear && buff.getRemainSecond() <= 0) {
                        catchUp(npc);
                    }
                }
            }
        }
        */

        if (buff.getRemainSecond() <= 0) {
            return false;
        }
        return true;
    }

    /************************************
     * 以下是可以override的方法
     ************************************/

    /**
     * 捕获NPC后的行为
     */
    protected void catchUp(NPC npcOld) {

    }

    /**
     * 检查是否用户已经完成游戏，比如死亡或者胜利之类的，默认生命为0为结束游戏
     */
    protected void checkIfEnd() {
        if (life == 0) {
            setState(Constant.UserState.GAME_OVER);
            gameOverTime = System.currentTimeMillis();
        }
    }

}
