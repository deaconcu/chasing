package com.prosper.chasing.game.base;

import java.util.*;

import com.prosper.chasing.game.message.*;
import com.prosper.chasing.game.navmesh.Navimesh;
import com.prosper.chasing.game.navmesh.Point;
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

    // 游戏的阶段
    private byte step;

    // 游戏元信息
    private GameInfo gameInfo;

    // 参与的用户信息
    private Map<Integer, User> userMap;

    // 玩家消息队列
    private Queue<Message> messageQueue = new LinkedList<>();

    // 游戏场景内的道具信息
    private List<Prop> envPropList = new LinkedList<>();

    // 可移动NPC的配置信息
    private List<NPC.NPCConfig> movableNPCConfigList = new LinkedList<>();
    // 位置不发生变化的NPC
    private Map<Integer, NPC> staticNPCMap = new HashMap<>();
    // 可以移动的NPC
    private Map<Integer, NPC> movableNPCMap = new HashMap<>();
    // 移动NPC每种类型的数量
    private Map<Short, Integer> movableNPCCountMap = new HashMap<>();

    // prop顺序号id
    private int nextPropSeqId = 1;
    // 静止NPC顺序号id
    private int staticNPCSeqId = 1;
    // 移动NPC顺序号id
    private int movableNPCSeqId = 1;

    // 游戏中的道具配置
    protected GamePropConfigMap gamePropConfigMap = new GamePropConfigMap(0);

    private PropService propService = new PropService();
    private SkillService skillService = new SkillService();

    // 处理游戏逻辑的管理器
    private GameManage gameManage;
    private JsonUtil jsonUtil = new JsonUtil();

    private Navimesh navimesh;

    private Random random = new Random();

    protected boolean isStepChanged = false;
    // 每一次同步的时候有位置变化和buff变化的用户map
    List<Prop> envPropChangedList = new LinkedList<>();
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

    public List<Prop> getEnvPropChangedList() {
        return envPropChangedList;
    }

    public List<Prop> getEnvPropList() {
        return envPropList;
    }

    public Map<Integer, NPC> getStaticNPCMap() {
        return staticNPCMap;
    }

    public Map<Integer, NPC> getMoveableNPCMap() {
        return movableNPCMap;
    }

    public byte getStep() {
        return step;
    }

    public void setStep(byte step) {
        isStepChanged = true;
        this.step = step;
    }

    public boolean isStepChanged() {
        return isStepChanged;
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

    public void setNavimesh(Navimesh navimesh) {
        this.navimesh = navimesh;
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
     * 游戏主逻辑
     * 主要包括：处理用户消息，执行预定义游戏逻辑，检查连接等
     */
    public void execute() {
        // set time
        lastUpdateTime = currentUpdateTime;
        currentUpdateTime = System.currentTimeMillis();

        // 执行玩家消息
        executeMessage();
        // 执行游戏逻辑
        logic();
        // 检查, 比如玩家是否掉线, 游戏是否结束
        check();
    }

    /**
     * 游戏场景中的一些逻辑，比如生成道具
     */
    public void logic() {
        // deal with prop
        executeProp();

        // npc
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
            } else if (message instanceof TaskMessage){
                TaskMessage taskMessage = (TaskMessage) message;
                executeTaskMessage(taskMessage);
            } else if (message instanceof QuitCompleteMessage){
                QuitCompleteMessage quitCompleteMessage = (QuitCompleteMessage) message;
                executeQuitCompleteMessage(quitCompleteMessage);
            } else if (message instanceof EchoMessage){
                EchoMessage echoMessage = (EchoMessage) message;
                executeEchoMessage(echoMessage);
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
        isStepChanged = false;
        for (User user: userMap.values()) {
            user.clearAfterSync();
        }
    }

    /**
     * 游戏开始后的准备工作
     */
    public void prepare() {
        prepareData();
        generatePrepareMessage();
    }

    /**
     * 处理游戏开始之后的一些准备数据，比如设置用户位置, NPC位置等等
     */
    public void prepareData() {
        for (User user: userMap.values()) {
            Position userPosition = new Position();
            userPosition.rotateY = 0;
            userPosition.point = navimesh.getRandomPosition();
            userPosition.moveState = Constant.MoveState.IDLE;
            user.setPosition(userPosition);
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
                bb.append(npc.getTypeId());
                bb.append(npc.getSeqId());
                bb.append(npc.getPosition().moveState);
                bb.append(npc.getPosition().point.x);
                bb.append(npc.getPosition().point.y);
                bb.append(npc.getPosition().point.z);
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
            while (replyMessage != null) {
                gameManage.replyData(replyMessage);
                log.info("reply bytes: {}", Arrays.toString(replyMessage.getContent().array()));
                replyMessage = user.nextMessage();
            }
        }
    }

    /**
     * 判断两个位置是不是相邻
     */
    protected boolean isNear(Point pp1, Point pp2, int range) {
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
                message.moveState, new Point(message.positionX, message.positionY, message.positionZ),
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
     * 处理任务消息
     */
    public void executeTaskMessage(TaskMessage message) {
        // for override
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
     * 处理echo消息
     */
    private void executeEchoMessage(EchoMessage echoMessage) {
        User user = getUser(echoMessage.getUserId());
        if (echoMessage.retryType == Constant.MessageRetryType.ALL) {
            user.sendSeqId = user.earliestSeqId;
        } else if (echoMessage.retryType == Constant.MessageRetryType.SINGLE){
            user.resendSeqIdList = echoMessage.missingMessageSeqIdList;
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
            Position position = new Position((byte)1, new Point(0, 0, 0),0);
            user.setPosition(position);
            user.setInitPosition(position);
            userMap.put(user.getId(), user);
        }
    }

    /**
     * 处理和道具相关的一些逻辑
     */
    protected  void executeProp() {
        executeExistProp();
        generateProp();
    }

    /**
     * 移动可移动道具
     * 1. 道具离玩家太近会自动跑开(暂时不做)
     * 2. 如果被玩家标记且距离太近，道具会离开，且会尝试摆脱
     * 3. 可以被多名玩家追逐
     */
    protected void executeExistProp() {
        ListIterator<Prop> iterator = getEnvPropList().listIterator();
        while(iterator.hasNext()) {
            Prop prop = iterator.next();
            // 如果道具到期，移除道具
            if (prop.vanishTime <= System.currentTimeMillis()) {
                getEnvPropChangedList().add(prop);
                iterator.remove();
                log.info("removed prop: {}:{}:{}", gameInfo.getId(), prop.id, getEnvPropChangedList());
            }

            // 移动道具
            prop.move();

            // 计算追逐进度
            prop.countProgress();

            // 拾取道具
            // TODO 需要提高一下速度，比较慢
            for (User user: getUserMap().values()) {
                if (user.getPropCount() >= 15) {
                    continue;
                }
                boolean isNear = isNear(prop.position.point, user.getPosition().point, FETCH_DISTANCE);
                if (isNear) {
                    user.setProp(prop.typeId, (byte)(user.getProp(prop.typeId) + 1));
                    prop.vanishTime = System.currentTimeMillis();
                    iterator.remove();
                    getEnvPropChangedList().add(prop);

                    log.info("fetched prop: {}:{}:{}", gameInfo.getId(), prop.id, getEnvPropChangedList());
                }
            }
        }
    }

    /**
     * 补充新的道具
     */
    protected void generateProp() {
        if (gamePropConfigMap.count == 0) return;
        while (getEnvPropList().size() < gamePropConfigMap.count) {
            Prop envProp = new Prop();
            GamePropConfigMap.GamePropConfig propConfig = gamePropConfigMap.getRandomProp();
            envProp.typeId = propConfig.propTypeId;
            envProp.id = nextPropSeqId ++;
            envProp.position.point = navimesh.getRandomPosition();
            envProp.createTime = System.currentTimeMillis();
            envProp.vanishTime = envProp.createTime + propConfig.duration * 1000;
            getEnvPropList().add(envProp);
            getEnvPropChangedList().add(envProp);
            log.info("created prop: {}:{}-{}:{}", gameInfo.getId(), envProp.id, envProp.position.point.x, envProp.position.point.z);
        }
    }

    /**
     * 获得游戏支持的道具类型
     */
    protected byte[] getPropIdList () {
        return new byte[]{};
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
        Integer count = movableNPCCountMap.get(config.typeId);
        if (count == null) {
            count = 0;
        }
        for (int i = 0; i < config.count - count; i++) {
            Position position = getInitPosition();
            int seqId = movableNPCSeqId ++;
            getMoveableNPCMap().put(seqId, new NPC(seqId, config.typeId, position, config.speed));
            if (movableNPCCountMap.get(config.typeId) == null) {
                movableNPCCountMap.put(config.typeId, 1);
            } else {
                movableNPCCountMap.put(config.typeId, movableNPCCountMap.get(config.typeId) + 1);
            }

        }
    }

    /**
     * 移动NPC
     */
    protected void moveNPC() {
        for (NPC npc: getMoveableNPCMap().values()) {
            if (npc.isPathEmpty()) {
                npc.setPath(navimesh.getPath(npc.getPosition().point, navimesh.getRandomPosition()));
            }
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
    protected Position getInitPosition() {
        Point point = navimesh.getRandomPosition();
        return new Position((byte)0, point, 0);
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
