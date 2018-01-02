package com.prosper.chasing.game.base;

import java.util.*;

import com.prosper.chasing.game.message.*;
import com.prosper.chasing.game.service.PropService;
import com.prosper.chasing.game.service.SkillService;
import com.prosper.chasing.game.util.ByteBuilder;
import com.prosper.chasing.game.util.Constant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.prosper.chasing.game.message.Message;
import com.prosper.chasing.game.util.Constant.UserState;
import com.prosper.chasing.common.util.CommonConstant.GameState;
import com.prosper.chasing.common.util.JsonUtil;

public abstract class Game {
    
    private Logger log = LoggerFactory.getLogger(getClass());

    public static final int FETCH_DISTANCE = 200;
    public static final int FROZEN_TIME = 10000;

    // 游戏加载状态
    private int state;

    // 游戏元信息
    private GameInfo gameInfo;

    // 参与的用户信息
    private Map<Integer, User> userMap;

    private Queue<Message> messageQueue = new LinkedList<>();

    // 游戏场景内的道具信息
    private List<EnvProp> envPropList = new LinkedList<>();

    // 可移动NPC的配置信息
    private List<NPC.NPCConfig> movableNPCConfigList;
    // 位置不发生变化的NPC
    private Map<Integer, NPC> staticNPCMap = new HashMap<>();
    // 可以移动的NPC
    private Map<Integer, NPC> movableNPCMap = new HashMap<>();
    // 移动NPC每种类型的数量
    private Map<Short, Integer> movableNPCCountMap;

    // prop顺序号id
    private int nextPropSeqId = 1;
    // 静止NPC顺序号id
    private int staticNPCSeqId = 1;
    // 移动NPC顺序号id
    private int movableNPCSeqId = 1;

    private PropService propService = new PropService();
    private SkillService skillService = new SkillService();

    // 处理游戏逻辑的管理器
    private GameManage gameManage;
    private JsonUtil jsonUtil = new JsonUtil();

    private Random random = new Random();

    // 每一次同步的时候有位置变化和buff变化的用户map
    List<EnvProp> envPropChangedList = new LinkedList<>();
    Set<Integer> positionChangedSet = new HashSet<>();
    Set<Integer> buffChangedSet = new HashSet<>();

    protected long startTime = System.currentTimeMillis();
    private long lastUpdateTime = System.currentTimeMillis();
    private long currentUpdateTime = System.currentTimeMillis();

    public Game() {
        this.state = GameState.CREATE;
        userMap = new HashMap<>();
    }

    public Class<? extends User> getUserClass() {
        return User.class;
    }

    public Map<Integer, User> getUserMap() {
        return userMap;
    }

    public Random getRandom() {
        return random;
    }

    public List<EnvProp> getEnvPropChangedList() {
        return envPropChangedList;
    }

    public List<EnvProp> getEnvPropList() {
        return envPropList;
    }

    public Map<Integer, NPC> getStaticNPCMap() {
        return staticNPCMap;
    }

    public Map<Integer, NPC> getMoveableNPCMap() {
        return movableNPCMap;
    }

    public void offerMessage(Message message) {
        messageQueue.offer(message);
    }

    protected void addMovableNPCConfig(NPC.NPCConfig config) {
        movableNPCConfigList.add(config);
    }

    /**
     * 获得npc相关的道具，比如捕猎时捕获一个动物，获得一个相应的动物道具
     */
    public Short getRelatedPropByNPC(short npcId) {
        return null;
    }

    /**
     * 游戏场景中的道具
     */
    public static class EnvProp {
        public short propId;
        public int seqId;
        public PositionPoint positionPoint;
        public long createTime;
        public long vanishTime;
        public boolean state;

        public EnvProp() {
            positionPoint = new PositionPoint(0, 0, 0);
        }

        public int getRemainSecond() {
            int remainSecond = (int)((vanishTime - System.currentTimeMillis()) / 1000);
            return remainSecond > 0 ? remainSecond : 0;
        }
    }

    /**
     * 结束后的成绩
     */
    public static class Result implements Comparable{
        User user;
        int score;
        int reward;

        public Result(User user, int score, int reward) {
            this.user = user;
            this.score = score;
            this.reward = reward;
        }

        @Override
        public int compareTo(Object o) {
            if (!(o instanceof Result)) {
                return -1;
            }
            return (reward > ((Result) o).reward) ? 1 : -1;
        }
    }

    /**
     * 位置点
     */
    public static class PositionPoint {
        public int x;
        public int y;
        public int z;

        public PositionPoint(int x, int y , int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof  PositionPoint)) {
                return false;
            }
            PositionPoint pp = (PositionPoint) o;
            return x == pp.x && y == pp.y && z == pp.z;
        }
    }

    /**
     * 游戏场景中的一些逻辑，比如生成道具
     */
    public void logic() {
        lastUpdateTime = currentUpdateTime;
        currentUpdateTime = System.currentTimeMillis();
        removeInvalidProp();
        fetchProp();
        generateProp();

        generateMovableNPC();
        moveNPC();
    }

    /**
     * 处理消息
     */
    public void executeMessage() {
        while(messageQueue.size() > 0) {
            executeMessage(messageQueue.poll());
        }
    }

    /**
     * 处理进入的消息，主要进行分发
     */
    private void executeMessage(Message message) {
        if (message instanceof UserMessage) {
            UserMessage userMessage = (UserMessage) message;
            log.info("execute user message: {}", Arrays.toString(userMessage.getContent().array()));
            User user = userMap.get(userMessage.getUserId());
            user.updateMessageSeq(userMessage.getSeqId());
        }

        if (state == GameState.FINISHED || state == GameState.RESULT_INFORMED) {
            // 如果游戏已结束，只接受退出的消息
            if (message instanceof QuitMessage) {
                QuitMessage quitMessage = (QuitMessage) message;
                executeQuitMessage(quitMessage);
            } else if (message instanceof QuitCompleteMessage) {
                QuitCompleteMessage quitCompleteMessage = (QuitCompleteMessage) message;
                executeQuitCompleteMessage(quitCompleteMessage);
            }
        } else {
            // 其他状态下正常处理其它的消息
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
            } else if (message instanceof PurchaseMessage) {
                PurchaseMessage purchaseMessage = (PurchaseMessage) message;
                executePurchaseMessage(purchaseMessage);
            } else if (message instanceof SkillMessage){
                SkillMessage skillMessage = (SkillMessage) message;
                executeSkillMessage(skillMessage);
            } else if (message instanceof QuitCompleteMessage){
                QuitCompleteMessage quitCompleteMessage = (QuitCompleteMessage) message;
                executeQuitCompleteMessage(quitCompleteMessage);
            } else if (message instanceof EchoMessage){
            } else {
                log.warn("undefined message type:" + message.getClass().getName());
            }
        }
    }

    /**
     * 同步用户数据
     */
    public void generateUserMessage() {
        for (User user: userMap.values()) {
            if (user.isPositionChanged) {
                positionChangedSet.add(user.getId());
            }
            if (user.buffChangedSet.size() != 0) {
                buffChangedSet.add(user.getId());
            }
        }

        for (User user: userMap.values()) {
            ByteBuilder byteBuilder = user.ChangesToBytes();
            if (byteBuilder != null) {
                user.offerMessage(byteBuilder);
            }
        }

        envPropChangedList.clear();
        positionChangedSet.clear();
        buffChangedSet.clear();
        for (User user: userMap.values()) {
            user.clearAfterSync();
        }
    }

    /**
     * 同步游戏开始之后的一些数据，比如用户名, 游戏时间等，格式如下
     *
     * seqId(4)|messageType(1)|remainTime(4)|UserCount(1)|UserList[]|NPCCount(2)|list<NPC>|
     * NPC: id(1)|seqId(4)|moveState(1)|positionX(4)|positionY(4)|positionZ(4)|rotateY(4)
     *
     * User: userId(4)|nameLength(1)|name
     */
    public void generatePrepareMessage() {
        ByteBuilder bb = new ByteBuilder();
        bb.append(0);
        bb.append(Constant.MessageType.PREPARE);
        bb.append(gameInfo.getDuration());
        bb.append((byte)userMap.size());
        for (User user: userMap.values()) {
            bb.append(user.getId());
            bb.append(user.getName().getBytes().length);
            bb.append(user.getName().getBytes());
        }

        if (getStaticNPCMap().size() != 0) {
            bb.append((short)getStaticNPCMap().size());
            for (NPC npc: getStaticNPCMap().values()) {
                bb.append(npc.getId());
                bb.append(npc.getSeqId());
                bb.append(npc.getPosition().moveState);
                bb.append(npc.getPosition().positionPoint.x);
                bb.append(npc.getPosition().positionPoint.y);
                bb.append(npc.getPosition().positionPoint.z);
                bb.append(npc.getPosition().rotateY);
            }
        }

        if (bb.getSize() > 0) {
            for (User user: userMap.values()) {
                user.offerMessage(bb);
            }
        }
    }

    /**
     * 同步游戏结果, 格式如下：
     * seqId(4)|messageType(1)|resultCount(1)|ResultList[]
     *
     * result: userId(4)|score(4)|reward(4)
     */
    public void generateResultMessage() {
        List<Result> resultList = getResultList();
        ByteBuilder bb = new ByteBuilder();
        bb.append(0);
        bb.append(Constant.MessageType.RESULT);
        for(Result result: resultList) {
            bb.append(result.user.getId());
            bb.append(result.score);
            bb.append(result.reward);
        }

        if (bb.getSize() > 0) {
            for (User user: userMap.values()) {
                user.offerMessage(bb);
            }
        }
    }

    public void syncUser() {
        for (User user: userMap.values()) {
            if (user.getState() == UserState.OFFLINE) {
                continue;
            }
            ReplyMessage replyMessage = user.nextMessage();
            //while(replyMessage != null) {
            if (replyMessage != null) {
                gameManage.replyData(replyMessage);
                log.info("reply bytes: {}", Arrays.toString(replyMessage.getContent().array()));
                //replyMessage = user.nextMessage();
            }
        }
    }

    /**
     * 判断两个位置是不是相邻
     */
    protected boolean isNear(PositionPoint pp1, PositionPoint pp2, int range) {
        if (Math.abs(pp1.x - pp2.x) <= range && Math.abs(pp1.y - pp2.y) <= range & Math.abs(pp1.z - pp2.z) <= range) {
            return true;
        }
        return false;
    }

    /**
     * 获取结果列表
     */
    protected abstract List<Result> getResultList();

    /**
     * 处理连接消息
     * @param message
     * @return
     */
    private void executeConnectMessage(ConnectMessage message) {
        User user = getUser(message.getUserId(), true);
        int sourceState = user.getState();

        byte targetState = UserState.ACTIVE;
        user.setState(targetState);
    }

    /**
     * 处理退出消息
     */
    private void executeQuitMessage(QuitMessage message) {
        // 将user状态置为正在删除
        User user = getUser(message.getUserId(), true);
        byte sourceState = user.getState();
        byte targetState = UserState.QUITING;
        user.setState(targetState);
        
        // 插入用户待同步队列，如果插入失败，回滚初始状态
        if (!gameManage.addUserForDataDB(user)) {
            user.setState(sourceState);
        } else {
            log.info("user is quiting, user: {}", user.getId());
        }
    }
    
    /**
     * 处理位置消息
     */
    public void executePositionMessage(PositionMessage message) {
        User user = getUser(message.getUserId(), true);
        
        // TODO check if could move
        Position position = new Position(
                message.moveState, new PositionPoint(message.positionX, message.positionY, message.positionZ),
                message.rotationY);
        user.setPosition(position);
    }
    
    /**
     * 处理使用道具消息
     */
    public void executePropMessage(PropMessage message) {
        User user = getUser(message.getUserId(), true);
        User toUser = getUser(message.getToUserId(), false);
        if (toUser == null) {
            toUser = user;
        }
        // check if user prop is enough
        byte propId = message.getPropId();
        if (user.checkProp(propId, (byte)1)) {
            propService.use(propId, message, user, toUser, userMap, envPropList);
        }
    }

    /**
     * 处理购买消息
     */
    public void executePurchaseMessage(PurchaseMessage message) {
        int price = getPrice(message.itemId);
        if (price == -1) {
            return;
        }
        User user = getUser(message.getUserId(), true);
        user.purchaseProp(message.itemId, price);
    }

    /**
     * 获得某个商品的价格
     */
    public int getPrice(short propId) {
        return -1;
    }

    /**
     * 处理使用技能消息
     */
    public void executeSkillMessage(SkillMessage message) {
        User user = getUser(message.getUserId(), true);
        User toUser = getUser(message.getToUserId(), false);
        if (toUser == null) {
            toUser = user;
        }
        // check if user prop is enough
        short skillId = message.getSkillId();
        skillService.use(skillId, message, user, toUser, userMap);
    }
    
    /**
     * 处理退出完成消息
     */
    private void executeQuitCompleteMessage(QuitCompleteMessage quitCompleteMessage) {
        int userId = quitCompleteMessage.getUserId();
        User user = getUser(userId);
        
        byte targetState = UserState.QUIT;
        user.setState(targetState);
        log.info("user is quited, user: {}", user.getId());
        
        // 判断是否全部用户都已退出游戏，如果是，将游戏状态置为完成
        boolean allQuit = true;
        for (User userInGame: userMap.values()) {
            if (userInGame.getState() == UserState.ACTIVE) {
                allQuit = false;
                break;
            }
        }
        
        if (allQuit) {
            state = GameState.DESTROYING;
            log.info("game is quitting, game: {}", gameInfo.getId());
        }
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

    private long getRemainTime() {
        long remainTime = gameInfo.getDuration() * 1000 - (System.currentTimeMillis() - startTime);
        return remainTime;
    }

    /**
     * 一些游戏数据的检查
     */
    public void check() {
        // 检查游戏是否结束
        if (getRemainTime() <= 0) {
            state = GameState.FINISHED;
            for (User user: userMap.values()) {
                user.setState(UserState.GAME_OVER);
            }
        }

        if (getRemainTime() <= -10000) {
            state = GameState.DESTROYING;
        }

        // 检查用户是否掉线
        for (User user: userMap.values()) {
            user.check();
        }
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
            Position position = new Position((byte)1, new PositionPoint(0, 0, 0),0);
            user.setPosition(position);
            user.setInitPosition(position);
            userMap.put(user.getId(), user);
        }
    }

    /**
     * 移除无效道具
     */
    protected void removeInvalidProp() {
        ListIterator<EnvProp> iterator = getEnvPropList().listIterator();
        while(iterator.hasNext()) {
            EnvProp envProp = iterator.next();
            if (envProp.vanishTime <= System.currentTimeMillis()) {
                envProp.state = false;
                getEnvPropChangedList().add(envProp);
                iterator.remove();
                log.info("removed prop: {}:{}:{}", gameInfo.getId(), envProp.seqId, getEnvPropChangedList());
            }
        }
    }

    /**
     * 拾取道具
     */
    protected void fetchProp() {
        ListIterator<EnvProp> iterator = getEnvPropList().listIterator();
        while(iterator.hasNext()) {
            EnvProp envProp = iterator.next();
            for (User user: getUserMap().values()) {
                if (user.getPropCount() >= 15) {
                    continue;
                }
                boolean isNear = isNear(envProp.positionPoint, user.getPosition().positionPoint, FETCH_DISTANCE);
                if (isNear) {
                    user.setProp(envProp.propId, (byte)(user.getProp(envProp.propId) + 1));
                    envProp.vanishTime = System.currentTimeMillis();
                    iterator.remove();
                    getEnvPropChangedList().add(envProp);

                    log.info("fetched prop: {}:{}:{}", gameInfo.getId(), envProp.seqId, getEnvPropChangedList());
                }
            }
        }
    }

    /**
     * 补充新的道具
     */
    protected void generateProp() {
        // TODO 需要提取出去作为配置
        int xRange = 5; // 地图上x的范围
        int zRange = 5; // 地图上z的范围
        int propSize = 3;  // 生成道具的数量
        int last = 15000; // 道具持续时间, 单位为毫秒
        short[] propIds = {1, 2, 3};  // 能够生成的道具id

        while (getEnvPropList().size() < propSize) {
            EnvProp envProp = new EnvProp();
            envProp.propId = propIds[getRandom().nextInt(propIds.length)];
            envProp.seqId = nextPropSeqId ++;
            envProp.positionPoint.x = (int) (getRandom().nextFloat() * xRange * 1000);
            envProp.positionPoint.y = 0;
            envProp.positionPoint.z = (int) (getRandom().nextFloat() * zRange * 1000);
            envProp.createTime = System.currentTimeMillis();
            envProp.vanishTime = envProp.createTime + last;
            envProp.state = true;
            getEnvPropList().add(envProp);
            getEnvPropChangedList().add(envProp);
            log.info("created prop: {}:{}-{}:{}", gameInfo.getId(), envProp.seqId, envProp.positionPoint.x, envProp.positionPoint.z);
        }
    }

    /**
     * 生成可以移动的NPC
     */
    protected void generateMovableNPC() {
        for (NPC.NPCConfig config: movableNPCConfigList) {
            generateNPC(config);
        }
    }

    private void generateNPC(NPC.NPCConfig config) {
        Integer count = movableNPCCountMap.get(config.id);
        if (count == null) {
            count = 0;
        }
        Position position = getInitPositionById(config.id);
        for (int i = 0; i < count; i++) {
            int seqId = movableNPCSeqId ++;
            getMoveableNPCMap().put(seqId, new NPC(seqId, config.id, position, config.speed));
            movableNPCCountMap.put(config.id, movableNPCCountMap.get(config.id) + 1);
        }
    }

    /**
     * 移动NPC
     */
    protected void moveNPC() {
        for (NPC npc: getMoveableNPCMap().values()) {
            npc.move(currentUpdateTime - lastUpdateTime);
        }
    }

    /**
     * 检查buff是否还有效
     */
    protected void checkBuff() {

    }

    /**
     * 生成某一种动物的初始地址
     */
    private Position getInitPositionById(short id) {
        // TODO
        return new Position((byte)0, new PositionPoint(0, 0, 1), 0);
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

    public void setState(byte state) {
        this.state = state;
    }
    
}
