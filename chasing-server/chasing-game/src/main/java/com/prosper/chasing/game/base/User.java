package com.prosper.chasing.game.base;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.prosper.chasing.game.map.SpecialSection;
import com.prosper.chasing.game.message.ReplyMessage;
import com.prosper.chasing.game.util.ByteBuilder;
import com.prosper.chasing.game.util.Constant;
import com.prosper.chasing.game.util.Enums;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.*;

import static com.prosper.chasing.game.base.Game.FROZEN_TIME;

public class User extends GameObject {

    private Logger log = LoggerFactory.getLogger(getClass());

    private static final int NEAR_DISTANCE = 10;
    private static final int REPLY_GAP = 1000;

    // 体力最大值
    private static final int STRENGTH_MAX = 100000;
    // 体力值每秒恢复速度
    private static final int STRENGTH_RECOVER_RATE = 1;
    // 体力值每秒消耗与速度的比值
    private static final int STRENGTH_CONSUME_SPEED_RATE = 1;

    private static final int PROP_PACKAGE_MAX_SIZE = 10;

    // 用户所属的游戏
    @JsonIgnore
    private Game game;
    
    // 用户id
    private int id;

    // 角色类型
    private byte roleType;

    // 用户名称
    private String name;

    // 队伍id
    private byte groupId;

    // 运动姿态,比如跑步或者步行
    public byte moveState;

    // 运动步数
    private int steps;

    // 当前速度
    private int speed = 0;

    // 体力值
    private int strength;

    // 上次计算体力值的时间
    private long lastStrengthCountTime;

    // 上次同步体力给客户端的时间
    private long lastStrengthInfoTime;

    // 上次陷入沉睡的时间
    private long lastDreamTime;

    // 距离上次计算体力值后消耗的体力值
    private int strengthConsumed;

    // 速度百分比，有一些buff可以造成速度降低或者加快，用这个值来表示。
    // 100表示正常速度。200表示正常速度的两倍，50表示正常速度的一半，以此类推
    private short speedRate = 100;

    // 生命值
    private short life = 1;

    // 生命最大值
    protected short maxLife = 1;

    // 钱
    private int money;

    // 聚焦对象
    private User focusUser;

    // 所拥有的道具
    private Map<Short, Short> propMap = new HashMap<>();

    // Buff Map
    private List<Buff> buffList = new LinkedList<>();

    private List<UsePropAction> usePropList = new LinkedList<>();

    private List<UsePropAction> usePropIncList = new LinkedList<>();

    // 用户状态 @see #Constant.UserState
    private byte state;

    // 是否在灵魂状态
    private boolean isGhost;

    // 一些不太容易变化的数据是否有更新，包括生命值，状态，队伍
    boolean isBaseInfoChanged = false;

    // 自定义属性是否有变化
    public boolean isCustomPropertyChanged = false;

    // 是否需要同步位置
    private boolean positionReseted;

    /*
    // 生命值是否有修改
    boolean isLifeChanged = false;
    // 状态是否有修改
    boolean isStateChanged = false;
    */

    // 速度是否有修改
    boolean isSpeedRateChanged = false;

    // 目标对象是否有修改
    boolean isTargetChanged = false;

    // 聚焦对象是否有修改
    boolean isFocusChanged = false;

    // 体力是否有修改
    boolean isStrengthChanged = false;

    // 结束游戏的时间
    public long finishTime;

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
    //private Point3 targetPoint;

    int nextBuffId = 0;

    private Object targetObject;

    private Object systemTargetObject;

    private Map<Integer, ChasingInfo> chasingInfoMap = new HashMap<>();

    public User() {
        this.lastStrengthCountTime = System.currentTimeMillis();
    }

    public void init() {
        setStrength(STRENGTH_MAX);
    }


    public Map<Integer, ChasingInfo> getChasingInfoMap() {
        return chasingInfoMap;
    }

    public byte getRoleType() {
        return roleType;
    }

    public long getLastDreamTime() {
        return lastDreamTime;
    }

    public void setLastDreamTime(long lastDreamTime) {
        this.lastDreamTime = lastDreamTime;
    }

    public boolean isGhost() {
        return isGhost;
    }

    public void setGhost(boolean ghost) {
        isGhost = ghost;
        isBaseInfoChanged = true;
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
     * 重新设定位置 ，需要客户端同步
     * @param point3
     */
    public void resetPoint(Point3 point3) {
        setPoint3(point3);
        positionReseted = true;
    }

    /**
     * 获得当前目标对象
     * @return null 如果目标对象不存在返回null
     */
    public Object getTargetObject() {
        return targetObject;
    }

    public GameObject getTargetObject(Enums.TargetType type, int id) {
        if (type == Enums.TargetType.PROP) {
            return game.getProp(id);
        } else if (type == Enums.TargetType.STATIONARY) {
            return game.getStationary(id);
        } else if (type == Enums.TargetType.INTERACTIVE) {
            return game.getInteractive(id);
        } else if (type == Enums.TargetType.USER) {
            return game.getUser(id);
        }
        return null;
    }

    public Enums.TargetType getTargetType() {
        if (targetObject instanceof Point3) return Enums.TargetType.POSITION;
        else if (targetObject instanceof EnvProp) return Enums.TargetType.PROP;
        else if (targetObject instanceof User) return Enums.TargetType.USER;
        else if (targetObject instanceof Stationary) return Enums.TargetType.STATIONARY;
        else if (targetObject instanceof InteractiveObjects.Interactive) return Enums.TargetType.INTERACTIVE;
        else return Enums.TargetType.NONE;
    }

    /**
     * 设置目标对象
     * @return 修改成功返回true，否则返回false
     */
    public boolean setTarget(Enums.TargetType type, int id, Point3 point3) {
        if (type == Enums.TargetType.POSITION)  {
            if (targetObject != null && targetObject.equals(point3)) {
                return false;
            }
            // 设置当前目标
            targetObject = point3;
        } else {
            // 如果target和当前目标一致，不做处理
            Object intendTargetObject = getTargetObject(type, id);
            if (targetObject != null && targetObject.equals(intendTargetObject) || intendTargetObject == null) {
                return false;
            }

            if (targetObject != null && targetObject instanceof User) {
                chasingInfoMap.remove(((User) targetObject).getId());
            }

            if (intendTargetObject instanceof User) {
                User intendTargetUser = (User) intendTargetObject;
                intendTargetUser.chasingInfoMap.put(getId(), new ChasingInfo(this));
            }

            // 设置当前目标
            targetObject = intendTargetObject;
        }

        isTargetChanged = true;
        return true;
    }

    public Object getSystemTargetObject() {
        return systemTargetObject;
    }

    public void setSystemTargetObject(Object systemTargetObject) {
        this.systemTargetObject = systemTargetObject;
        isTargetChanged = true;
    }

    /**
     * 清除目标
     */
    public void clearTarget() {
        targetObject = null;
        isTargetChanged = true;
    }

    /**
     * 设置聚焦对象
     */
    public void setFocus(User user) {
        if (focusUser != user) {
            focusUser = user;
            isFocusChanged = true;
        }
    }

    /**
     * 允许进入睡眠
     */
    public boolean allowIntoDream() {
        if (System.currentTimeMillis() - lastDreamTime > 3000) {
            return true;
        }
        return false;
    }

    public void countEnergy() {
        //if (hasBuff(BuffConfig.FLASH_LEVEL_1) || hasBuff(BuffConfig.FLASH_LEVEL_2)) return;

        long currentTime = System.currentTimeMillis();
        long timeGap = System.currentTimeMillis() - lastStrengthCountTime;
        this.strengthConsumed += (int)(((float)this.speed / 1000 *
                STRENGTH_CONSUME_SPEED_RATE  - STRENGTH_RECOVER_RATE) * timeGap);
        this.lastStrengthCountTime = currentTime;

        int strength = this.strength - strengthConsumed;
        if (strength < 0)   {
            strength = 0;
        }
        else if (strength > STRENGTH_MAX) strength = STRENGTH_MAX;
        setStrength(strength);
        this.strengthConsumed = 0;
    }

    public void countSpeedRate() {
        short speedRate = 100;
        if (hasBuff(BuffConfig.SPEED_UP_LEVEL_2)) {
            speedRate += 40;
        } else if (hasBuff(BuffConfig.SPEED_UP_LEVEL_1)) {
            speedRate += 20;
        } else if (hasBuff(BuffConfig.SPEED_DOWN_LEVEL_2)) {
            speedRate -= 40;
        } else if (hasBuff(BuffConfig.SPEED_DOWN_LEVEL_1)) {
            speedRate -= 20;
        } else if (hasBuff(BuffConfig.HOLD_SCEPTER)) {
            speedRate += 100;
        }

        if (hasBuff(BuffConfig.SPEED_DOWN_LEVEL_1_TERRAIN)) {
            speedRate -= 20;
        } else if (hasBuff(BuffConfig.SPEED_DOWN_LEVEL_2_TERRAIN)) {
            speedRate -= 40;
        } else if (hasBuff(BuffConfig.SPEED_DOWN_LEVEL_3_TERRAIN)) {
            speedRate -= 60;
        } else if (hasBuff(BuffConfig.SLEEPY_LEVEL_1)) {
            speedRate -= 25;
        } else if (hasBuff(BuffConfig.SLEEPY_LEVEL_2)) {
            speedRate -= 50;
        }

        if (hasBuff(BuffConfig.HOLD_POSITION)) {
            speedRate = 0;
        }

        if (speedRate < 0) speedRate = 0;
        setSpeedRate(speedRate);
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
                int distance = position.point3.distance(movableObject.position.point3);
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
                int distance = position.point3.distance(movableObject.position.point3);
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
        int propCount = getProp(propId);
        if (propCount < count) {
            return false;
        }

        if (propCount == 0 && count == 0) return true;
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
        setProp(propId, (short) (getProp(propId) + count));
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

    /**
     * 获取用户拥有的某一类型道具数量
     */
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
            PropConfig.PropOld propOld = PropConfig.getProp(propTypeId);
            if (propOld.isInPackage) {
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

    /*
    public void addBuff(byte buffId, Object... values) {
        if (BuffConfig.getBuff(buffId) == null) {
            log.warn("buff is not exist" + buffId);
        }

        Buff buff = new Buff(BuffConfig.getBuff(buffId), values);
        buffMap.put(Byte.toString(buffId), buff);
        buffChangedSet.add(buff);
    }
    */
    private int getNextBuffId() {
        return nextBuffId ++;
    }


    /**
     * 增加地形造成的buff
     * @param buffTypeId buff objectId
     * @param groupId group objectId 地形的组id
     */
    public boolean addBuff(byte buffTypeId, int groupId)  {
        if (groupId <= 0 || hasBuff(buffTypeId, groupId)) return false;
        Buff buff = new Buff.SpcSectionBuff(getNextBuffId(), buffTypeId, groupId);
        buffList.add(buff);
        buffChangedSet.add(buff);
        return true;
    }

    /**
     * 增加buff
     * @param typeId buff的类型id
     * @param last buff持续时间, 单位为秒, last < 0 时表示buff没有时间限制
     * @param refresh 当user已存在该类型buff时，是否刷新该buff时间
     * @return 修改或者添加了buff时返回true，否则返回false
     */
    public boolean addBuff(byte typeId, short last, boolean refresh, Object... objects) {
        Buff buff = getBuff(typeId);
        if (buff != null && !refresh) return false;

        if (buff == null) {
            buff = new Buff(getNextBuffId(), typeId, last);
            buffList.add(buff);
        } else {
            buff.refresh(last);
        }
        buffChangedSet.add(buff);
        return true;
    }

    /**
     * 移除buff
     */
    public void removeBuff(byte buffTypeId) {
        for (Buff buff: buffList) {
            if (buff.typeId == buffTypeId) {
                buff.expire();
                buffChangedSet.add(buff);
            }
        }
    }

    public void clearBuff() {
        for (Buff buff: buffList) {
            buff.expire();
            buffChangedSet.add(buff);
        }
    }

    /**
     * 清除用户身上的地形buff
     */
    public void clearTerrainBuff() {
        for (Buff buff: buffList) {
            if (buff instanceof Buff.SpcSectionBuff) {
                buff.expire();
                buffChangedSet.add(buff);
            }
        }
    }

    /**
     * 判断用户是否有buff
     */
    public boolean hasBuff(byte buffTypeId) {
        for (Buff buff: buffList) {
            if (buff.typeId == buffTypeId) return true;
        }
        return false;
    }

    /**
     * 判断用户是否有某一个类型的地形buff
     */
    public boolean hasBuff(byte buffTypeId, int groupId) {
        for (Buff buff: buffList) {
            if (!(buff instanceof Buff.SpcSectionBuff)) continue;
            Buff.SpcSectionBuff spcSectionBuff = (Buff.SpcSectionBuff) buff;
            if (spcSectionBuff.typeId == buffTypeId && spcSectionBuff.groupId == groupId) return true;
        }
        return false;
    }

    /**
     * 获取用户身上已存在的buff信息
     * @param typeId buff的类型id
     * @return 已存在的buff对象，如果不存在，返回null
     */
    public Buff getBuff(byte typeId) {
        for (Buff buff: buffList) {
            if (buff.typeId == typeId) {
                return buff;
            }
        }
        return null;
    }

    /**
     * 用32个bit位表示buff
     */
    public int getBuffBytes() {
        int value = 0;
        for (Buff buff: buffList) {
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
        isBaseInfoChanged = true;
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

        /*
        long currentTime = System.currentTimeMillis();
        float timeGap = (System.currentTimeMillis() - lastStrengthCountTime) / 1000;
        this.strengthConsumed += (int)(this.speed * STRENGTH_CONSUME_SPEED_RATE * timeGap) - STRENGTH_RECOVER_RATE * timeGap;
        this.lastStrengthCountTime = currentTime;
        */
        this.speed = speed;
    }

    @Override
    public Enums.GameObjectType getObjectType() {
        return Enums.GameObjectType.PLAYER;
    }

    public byte getGroupId() {
        return groupId;
    }

    public void setGroupId(byte groupId) {
        this.groupId = groupId;
        this.isBaseInfoChanged = true;
    }

    public short getSpeedRate() {
        return speedRate;
    }

    public void setSpeedRate(short speedRate) {
        if (speedRate == this.speedRate) return;
        this.speedRate = speedRate;
        isSpeedRateChanged = true;
    }

    public int getStrength() {
        return strength;
    }

    public void setStrength(int strength) {
        if (this.strength == strength) return;
        this.strength = strength;
        this.isStrengthChanged = true;
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
            this.isBaseInfoChanged = true;
        }
    }

    public int getMoney() {
        return money;
    }

    public boolean modifyMoney(int amount) {
        if (money + amount >= 0) {
            money += amount;
            this.isBaseInfoChanged = true;
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
     * 获取有变化的数据用来同步给其它玩家
     * @param byteBuilder
     */
    public void appendBytes(ByteBuilder byteBuilder) {
        byteBuilder.append(getSyncAction().getValue());
        byteBuilder.append(getObjectType().getValue());
        byteBuilder.append(id);
        byteBuilder.append(isMoved() ? (byte)1 : (byte)0);
        if (isMoved()) {
            byteBuilder.append(moveState);
            byteBuilder.append(getPoint3().x);
            byteBuilder.append(getPoint3().y);
            byteBuilder.append(getPoint3().z);
            byteBuilder.append(getRotateY());
            // TODO 临时放一下，需要移出去
            byteBuilder.append(getGroupId());
        }

        byteBuilder.append(buffChangedSet.size() != 0 ? (byte)1 : (byte)0);
        if (buffChangedSet.size() != 0) {
            byte count = 0;
            for (Buff buff: getBuffList()) {
                if (buff.getRemainSecond() > 0) {
                    count ++;
                }
            }
            byteBuilder.append(count);
            for (Buff buff: getBuffList()) {
                if (buff.getRemainSecond() > 0) {
                    byteBuilder.append(buff.typeId);
                }
            }
        }
    }

    /**
     * 获取有变化的数据用来同步给自己
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
     * propCount(1)|list<PropOld>
     * customProperty 详见各自定义user类
     * userCount(1)|list<UserPosition>
     * userCount(1)|list<UserBuff>
     *
     * sign: gameChanges|focusUserInfo|target|RESERVED|RESERVED|RESERVED|action|baseInfo|
     *       position|strength|speedRate|buff|prop|customProperty|RESERVED|RESERVED|
     * EnvProp: objectId(2)|seqId(4)|positionX(4)|positionY(4)|positionZ(4)|remainSecond(4)|
     * NPCOld: objectId(1)|seqId(4)|moveState(1)|positionX(4)|positionY(4)|positionZ(4)|rotateY(4)
     * Action: objectId(2)|code(1)|type(1)|value(4)|
     * Buff: buffId(1)|remainSecond(4)|
     * PropOld: propId(2)|count(2)|
     * UserPosition userId(4)|moveState(1)|positionX(4)|positionY(4)|positionZ(4)|rotateY(4)
     * UserBuff userId(4)|buffByte(4)
     */
    public ByteBuilder bytesOfChangesToMe() {
        ByteBuilder byteBuilder =  new ByteBuilder();
        int seqId = 0;
        byteBuilder.append(seqId);
        byteBuilder.append(Constant.MessageType.USER);
        short sign = 0;
        byteBuilder.append(sign);
        byteBuilder.append(System.currentTimeMillis());

        if (isFocusChanged) {
            if (byteBuilder == null) {
                byteBuilder =  new ByteBuilder();
            }
            sign = (short) (sign | 16384);
            if (focusUser != null) {
                byteBuilder.append(focusUser.getId());
                byteBuilder.append(focusUser.getStrength());
                byteBuilder.append((byte)1);
                byteBuilder.append((byte)focusUser.buffList.size());
                for (Buff buff: focusUser.buffList) {
                    byteBuilder.append(buff.id);
                    byteBuilder.append(buff.typeId);
                }
            } else {
                byteBuilder.append(0);
            }
        } else if (focusUser != null && (focusUser.isStrengthChanged || focusUser.buffChangedSet.size() > 0)) {
            if (byteBuilder == null) {
                byteBuilder =  new ByteBuilder();
            }
            sign = (short) (sign | 16384);
            byteBuilder.append(focusUser.getId());
            byteBuilder.append(focusUser.getStrength());
            /*
            if (focusUser.buffChangedSet.distance() > 0)  {
                byteBuilder.append((byte)1);
                byteBuilder.append((byte)focusUser.buffList.distance());
                for (Buff buff: focusUser.buffList) {
                    byteBuilder.append(buff.objectId);
                    byteBuilder.append(buff.typeId);
                }
            } else {
                byteBuilder.append((byte)0);
            }
            */
        }

        if (isTargetChanged) {
            if (byteBuilder == null) {
                byteBuilder =  new ByteBuilder();
            }
            sign = (short) (sign | 8192);

            Enums.TargetType targetType = getTargetType();
            byteBuilder.append(targetType.getValue());
            if (targetObject instanceof Point3) {
                byteBuilder.append(((Point3) targetObject).x);
                byteBuilder.append(((Point3) targetObject).y);
                byteBuilder.append(((Point3) targetObject).z);
            } else if (targetObject != null){
                byteBuilder.append(((GameObject) targetObject).getId());
            }

            if (systemTargetObject instanceof Point3) {
                byteBuilder.append((byte) 1);
                byteBuilder.append(((Point3) systemTargetObject).x);
                byteBuilder.append(((Point3) systemTargetObject).y);
                byteBuilder.append(((Point3) systemTargetObject).z);
            } else {
                byteBuilder.append((byte) 0);
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
        if (isBaseInfoChanged) {
            if (byteBuilder == null) {
                byteBuilder =  new ByteBuilder();
            }
            sign = (short) (sign | 256);
            byteBuilder.append(state);
            byteBuilder.append(getMoney());
            byteBuilder.append(getGroupId());
            byteBuilder.append(getLife());
            byteBuilder.append(isGhost ? (byte)1 : (byte)0);
        }
        /*
        if (isStateChanged) {
            if (byteBuilder == null) {
                byteBuilder =  new ByteBuilder();
            }
            sign = (short) (sign | 256);
            byteBuilder.append(state);
        }
        */
        if (isMoved()) {
            if (byteBuilder == null) {
                byteBuilder =  new ByteBuilder();
            }
            sign = (short) (sign | 128);
            byteBuilder.append(moveState);
            byteBuilder.append(getPoint3().x);
            byteBuilder.append(getPoint3().y);
            byteBuilder.append(getPoint3().z);
            byteBuilder.append(getRotateY());
            if (positionReseted) {
                byteBuilder.append((byte)1);
            } else {
                byteBuilder.append((byte)0);
            }
        }
        if (isStrengthChanged && (java.lang.System.currentTimeMillis() - lastStrengthInfoTime > 1000)) {
            if (byteBuilder == null) {
                byteBuilder =  new ByteBuilder();
            }
            sign = (short) (sign | 64);
            byteBuilder.append(getStrength());
            lastStrengthInfoTime = java.lang.System.currentTimeMillis();
            isStrengthChanged = false;
        }
        if (isSpeedRateChanged) {
            if (byteBuilder == null) {
                byteBuilder =  new ByteBuilder();
            }
            sign = (short) (sign | 32);
            byteBuilder.append(getSpeedRate());
        }
        if (buffChangedSet.size() != 0) {
            if (byteBuilder == null) {
                byteBuilder =  new ByteBuilder();
            }
            sign = (short) (sign | 16);
            byteBuilder.append((byte)buffChangedSet.size());
            for (Buff buff: buffChangedSet) {
                byteBuilder.append(buff.id);
                byteBuilder.append(buff.typeId);
                byteBuilder.append(buff.last);
                byteBuilder.append(buff.startSecond);
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
        //isLifeChanged = false;
        isSpeedRateChanged = false;
        //isStateChanged = false;
        isBaseInfoChanged = false;
        isCustomPropertyChanged = false;
        isFocusChanged = false;
        positionReseted = false;

        setMoved(false);
        setCrossZone(false);

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
        Iterator<Buff> iterator = buffList.iterator();
        while (iterator.hasNext()) {
            Buff buff = iterator.next();
            if (!checkBuff(buff)) {
                // 如果是道具buff，需要移除该道具
                if (buff instanceof Buff.PropBuff) {
                    reduceProp(((Buff.PropBuff)buff).propTypeId, (short)1);
                }
                buffChangedSet.add(buff);
                iterator.remove();
            }
        }

        // 如果队列中存在消息，说明用户有延时，用延时的时长来判断是否掉线
        ReplyMessage replyMessage = firstMessage();
        if (replyMessage != null) {
            // 如果用户在规定时间内都没有响应的消息，判定为掉线，不再发送同步消息，等待用户重新连接
            if (System.currentTimeMillis() - replyMessage.getTimestamp() > FROZEN_TIME &&
                    getState() != Constant.UserState.OFFLINE) {
                //setState(Constant.UserState.OFFLINE);
                //log.info("user is offline, user objectId: {}", getId());
            }
        }

        checkIfEnd();
    }

    /**
     * 当buff无效时，返回false, 否则返回true
     */
    protected boolean checkBuff(Buff buff) {
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
            setState(Constant.UserState.FINISHED);
            finishTime = System.currentTimeMillis();
        }
    }

    public List<Buff> getBuffList() {
        return buffList;
    }


}
