package com.prosper.chasing.game.base;

import java.util.*;

import com.prosper.chasing.game.message.*;
import com.prosper.chasing.game.navmesh.NaviMeshGroup;
import com.prosper.chasing.game.navmesh.Point;
import com.prosper.chasing.game.util.ByteBuilder;
import com.prosper.chasing.game.util.Constant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.prosper.chasing.game.message.Message;
import com.prosper.chasing.game.util.Constant.UserState;
import com.prosper.chasing.common.util.CommonConstant.GameState;
import com.prosper.chasing.common.util.JsonUtil;

import static com.prosper.chasing.game.util.Constant.TargetType.TYPE_PROP;

public abstract class Game {
    
    private Logger log = LoggerFactory.getLogger(getClass());

    public static final int FETCH_DISTANCE = 200;
    public static final int FROZEN_TIME = 10000;

    // 游戏中的道具配置
    public static Map<Short, Integer> propPriceMap = new HashMap<>();

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

    // 地形map
    protected Map<Integer, TerrainBlock> terrainBlockMap = new HashMap<>();

    // 游戏场景内的道具信息
    private List<EnvProp> envPropList = new LinkedList<>();

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


    // 处理游戏逻辑的管理器
    private GameManage gameManage;
    private JsonUtil jsonUtil = new JsonUtil();

    // 一些外部依赖
    protected NaviMeshGroup navimeshGroup;

    private Random random = new Random();

    protected boolean isStepChanged = false;
    // 每一次同步的时候有位置变化和buff变化的用户map
    List<EnvProp> envPropChangedList = new LinkedList<>();
    List<NPC> npcChangedList = new LinkedList<>();
    Set<Integer> positionChangedSet = new HashSet<>();
    Set<Integer> buffChangedSet = new HashSet<>();

    public long startTime = System.currentTimeMillis();
    private long lastUpdateTime = System.currentTimeMillis();
    private long currentUpdateTime = System.currentTimeMillis();

    public Game() {
        this.state = GameState.CREATE;
        userMap = new HashMap<>();

        propPriceMap.put(PropConfig.MARK, 20);
        propPriceMap.put(PropConfig.INVISIBLE_LEVEL_1, 20);
        propPriceMap.put(PropConfig.INVISIBLE_LEVEL_2, 20);
        propPriceMap.put(PropConfig.ANTI_INVISIBLE, 20);
        propPriceMap.put(PropConfig.RETURN_TO_INIT_POSITION, 20);
        propPriceMap.put(PropConfig.RANDOM_POSITION, 20);
        propPriceMap.put(PropConfig.FLASH_LEVEL_1, 20);
        propPriceMap.put(PropConfig.FLASH_LEVEL_2, 20);
        propPriceMap.put(PropConfig.FOLLOW, 20);
        propPriceMap.put(PropConfig.SPEED_UP_LEVEL_1, 20);
        propPriceMap.put(PropConfig.SPEED_UP_LEVEL_2, 20);
        propPriceMap.put(PropConfig.SPEED_DOWN_LEVEL_1, 20);
        propPriceMap.put(PropConfig.SPEED_DOWN_LEVEL_2, 20);
        propPriceMap.put(PropConfig.HOLD_POSITION, 20);
        propPriceMap.put(PropConfig.BLOOD_PILL, 20);
        propPriceMap.put(PropConfig.BLOOD_BAG, 20);
        propPriceMap.put(PropConfig.REBIRTH, 20);
        propPriceMap.put(PropConfig.DARK_VISION, 20);
        propPriceMap.put(PropConfig.IMMUNITY_LEVEL_1, 20);
        propPriceMap.put(PropConfig.IMMUNITY_LEVEL_2, 20);
        propPriceMap.put(PropConfig.REBOUND, 20);
        propPriceMap.put(PropConfig.NEAR_ENEMY_REMIND, 20);
        propPriceMap.put(PropConfig.PROP_BOMB, 20);
        propPriceMap.put(PropConfig.MONEY, 20);
        propPriceMap.put(PropConfig.GIFT_BOX, 20);
    }

    /**
     * 生成游戏场景中的对象
     */
    public void generateGameObjects() {
        generateTerrainBlock();
        generateStaticStuff();
        generateDecorations();
    }

    public void generateTerrainBlock() {}

    public void generateStaticStuff() {}

    public void generateDecorations() {}

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

    /********************************
     * 游戏逻辑
     ********************************/

    /**
     * 游戏主逻辑
     * 主要包括：处理用户消息，执行预定义游戏逻辑，检查连接等
     */
    public void execute() {
        // set time
        lastUpdateTime = currentUpdateTime;
        currentUpdateTime = System.currentTimeMillis();

        // 检查, 比如玩家是否掉线, 游戏是否结束
        check();
        // 执行游戏逻辑
        logic();
        // 执行玩家消息
        executeMessage();
    }

    /**
     * 游戏场景中的一些逻辑，比如生成道具
     */
    public void logic() {
        // 移动所有可移动道具
        moveProp();

        // 计算追逐和捕获
        countChasingProgress();

        // 移除无效道具
        removeInvildProp();

        // 生成道具
        generateProp();

        // npc
        moveNPC();
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

        // 检查用户是否掉线，是否已结束游戏（死亡或者获胜）
        for (User user: userMap.values()) {
            user.check();
            if (user.getState() == UserState.GAME_OVER) {
                ByteBuilder resultMessage = generateResultMessage(user);
                user.offerMessage(resultMessage);
                user.setState(UserState.RESULT_INFORMED);
            }
        }
    }

    /**
     * 新增游戏处理消息
     */
    public void offerMessage(Message message) {
        messageQueue.offer(message);
    }


    /********************************
     * 消息相关
     ********************************/

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

        if (state == GameState.FINISHED) {
            // 如果游戏已结束，只接受退出的消息
            if (message instanceof QuitMessage) {
                executeQuitMessage((QuitMessage) message);
            } else if (message instanceof QuitCompleteMessage) {
                executeQuitCompleteMessage((QuitCompleteMessage) message);
            }
        } else {
            // 其他状态下正常处理其它的消息
            if (message instanceof ConnectMessage) {
                executeConnectMessage((ConnectMessage) message);
            } else if (message instanceof QuitMessage) {
                executeQuitMessage((QuitMessage) message);
            } else if (message instanceof PositionMessage) {
                executePositionMessage((PositionMessage) message);
            } else if (message instanceof PropMessage) {
                executePropMessage((PropMessage) message);
            } else if (message instanceof TransferPropMessage) {
                executeTransferPropMessage((TransferPropMessage) message);
            } else if (message instanceof PurchaseMessage) {
                executePurchaseMessage((PurchaseMessage) message);
            } else if (message instanceof TargetMessage){
                executeTargetMessage((TargetMessage) message);
            } else if (message instanceof TaskMessage){
                executeTaskMessage((TaskMessage) message);
            } else if (message instanceof QuitCompleteMessage){
                executeQuitCompleteMessage((QuitCompleteMessage) message);
            } else if (message instanceof EchoMessage){
                executeEchoMessage((EchoMessage) message);
            } else {
                if (!executeCustomMessage(message)) {
                    log.warn("undefined message type:" + message.getClass().getName());
                }
            }
        }
    }

    /**
     * 处理子游戏自定义消息
     */
    protected boolean executeCustomMessage(Message message) {
        return true;
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
        npcChangedList.clear();
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
            setUserInitPosition(user);
        }
        initNPC();
    }

    /**
     * 同步游戏开始之后的一些数据，比如用户名, 游戏时间等，格式如下
     *
     * seqId(4)|messageType(1)|remainTime(4)|UserCount(1)|UserList[]|propCount(2)|list<PropPrice>|
     * PropPrice: id(2)|price(4)
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

        short[] storePropIds = getStorePropIds();
        bb.append((short)storePropIds.length);
        for (short storePropId: storePropIds) {
            int price = getPropPrice(storePropId);
            bb.append(storePropId);
            bb.append(price);
        }

        if (bb.getSize() > 0) {
            for (User user: userMap.values()) {
                user.offerMessage(bb);
            }
        }
    }


    /**
     * 为单个用户生成排名及奖励信息
     * seqId(4)|messageType(1)|rank(2)|reward(4)
     */
    public ByteBuilder generateResultMessage(User user) {
        ByteBuilder byteBuilder =  new ByteBuilder();
        int seqId = 0;
        byteBuilder.append(seqId);
        byteBuilder.append(Constant.MessageType.USER);
        byteBuilder.append(generateCustomResultMessage(user));
        return byteBuilder;
    }

    /**
     * 为单个用户生成排名及奖励信息, 每个游戏不一样，需要override，默认返回空
     * seqId(4)|messageType(1)|rank(2)|reward(4)
     */
    public byte[] generateCustomResultMessage(User user) {
        return null;
    }


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
        user.addSteps(message.steps);
    }
    
    /**
     * 处理使用道具消息
     */
    public void executePropMessage(PropMessage message) {
        User user = getUser(message.getUserId(), true);
        if (user.getState() == UserState.GAME_OVER) return;

        User toUser = getUser(message.getToUserId(), false);
        if (toUser == null) {
            toUser = user;
        }
        // check if user prop is enough
        short propId = message.getPropId();
        if (user.checkProp(propId, (byte)1)) {
            PropConfig.getProp(propId).use(message, user, this);
        }
    }

    /**
     * 处理转移道具消息
     */
    public void executeTransferPropMessage(TransferPropMessage message) {
        if (message.getType() == TransferPropMessage.GIVE) {
            doTransferProp(getUser(message.getUserId()), getUser(message.getTargetUserId()),
                    message.getPropId(), message.getCount());
        } else {
            doTransferProp(getUser(message.getTargetUserId()), getUser(message.getUserId()),
                    message.getPropId(), message.getCount());
        }
    }


    /**
     * 处理购买消息
     */
    public void executePurchaseMessage(PurchaseMessage message) {
        User user = getUser(message.getUserId(), true);
        if (user.getState() == UserState.GAME_OVER) return;

        int price = getPropPrice(message.itemId);
        if (price == -1) {
            return;
        }
        user.purchaseProp(message.itemId, price);
    }

    /**
     * 处理任务消息
     */
    public void executeTaskMessage(TaskMessage message) {
        User user = getUser(message.getUserId(), true);
        if (user.getState() == UserState.GAME_OVER) return;
        // for override
    }

    /**
     * 处理使用技能消息
     */
    public void executeTargetMessage(TargetMessage message) {
        User user = getUser(message.getUserId(), true);
        user.setTarget(message.getType(), message.getId(), message.getPoint());
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





    /********************************
     * user 相关
     ********************************/

    /**
     * 同步用户消息
     */
    public void syncUser() {
        for (User user: userMap.values()) {
            if (user.getState() == UserState.OFFLINE || user.getState() == UserState.LOADED) {
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
     * 获取用户
     */
    public User getUser(int userId) {
        return userMap.get(userId);
    }
    
    /**
     * 加载用户，游戏创建时调用
     */
    public void loadUser(List<? extends User> userList) {
        for (User user: userList) {
            userMap.put(user.getId(), user);
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

    /**
     * 计算所有用户的追逐进度
     */
    protected void countChasingProgress() {
        for (User user: userMap.values()) {
            user.countMovableTargetProgress();
        }
    }

    /********************************
     * prop 相关
     ********************************/

    public abstract GamePropConfigMap getGamePropConfigMap();

    /**
     * 执行转移道具
     */
    private void doTransferProp(User fromUser, User toUser, byte propId, short count) {
        // 检查源用户道具是否足够, 目标用户包是否已满
        if (!fromUser.checkProp(propId, count)) return;
        if (PropConfig.getProp(propId).isInPackage) {
            if (toUser.isPackageFull(count)) return;
        }
        if (!checkIfPropCanTransfer(fromUser, toUser, propId)) return;

        fromUser.reduceProp(propId, count);
        toUser.increaseProp(propId, count);
    }

    /**
     * 移除场景内无效的道具，比如时间到期
     */
    protected void removeInvildProp() {
        ListIterator<EnvProp> iterator = getEnvPropList().listIterator();
        while(iterator.hasNext()) {
            EnvProp prop = iterator.next();
            // 如果道具到期，移除道具
            if (prop.vanishTime <= System.currentTimeMillis()) {
                getEnvPropChangedList().add(prop);
                iterator.remove();
                log.info("removed prop: {}:{}:{}", gameInfo.getId(), prop.id, getEnvPropChangedList());
                continue;
            }
        }
    }

    /**
     * 移动可移动道具
     * 1. 道具离玩家太近会自动跑开(暂时不做)
     * 2. 如果被玩家标记且距离太近，道具会离开，且会尝试摆脱
     * 3. 可以被多名玩家追逐
     */
    protected void moveProp() {
        ListIterator<EnvProp> iterator = getEnvPropList().listIterator();
        while(iterator.hasNext()) {
            EnvProp prop = iterator.next();
            if(!prop.movable) continue;
            // 设置路径
            if (prop.isPathEmpty()) {
                prop.setPath(navimeshGroup.getPath(
                        gameInfo.getMetagameCode(), prop.position.point,
                        navimeshGroup.getRandomPositionPoint(gameInfo.getMetagameCode())));
            }

            // 移动道具
            prop.move();
            if (prop.isPositionChanged()) {
                getEnvPropChangedList().add(prop);
                prop.setPositionChanged(false);
            }
        }
    }

    /**
     * 补充新的道具
     */
    protected void generateProp() {
        if (getGamePropConfigMap().count == 0) return;
        while (getEnvPropList().size() < getGamePropConfigMap().count) {
            EnvProp envProp = new EnvProp(this);
            GamePropConfigMap.GamePropConfig propConfig = getGamePropConfigMap().getRandomProp();
            if (propConfig == null) continue;

            envProp.typeId = propConfig.propTypeId;
            envProp.id = nextPropSeqId ++;
            envProp.position = new Position();
            envProp.position.point = navimeshGroup.getRandomPositionPoint(gameInfo.getMetagameCode());
            envProp.createTime = System.currentTimeMillis();
            envProp.vanishTime = envProp.createTime + propConfig.duration * 1000;
            envProp.movable = propConfig.movable;
            getEnvPropList().add(envProp);
            getEnvPropChangedList().add(envProp);
            log.info("created prop: {}:{}-{}:{}:{}", gameInfo.getId(), envProp.id,
                    envProp.position.point.x, envProp.position.point.y, envProp.position.point.z);
        }
    }

    /**
     * 根据id获取环境道具
     */
    public EnvProp getProp(int id) {
        for (EnvProp prop: envPropList) {
            if (prop.id == id) {
                return prop;
            }
        }
        return null;
    }

    /**
     * 获取道具价格
     */
    public int getPropPrice(short propTypeId) {
        // TODO 有可能默认的price也不存在，就会抛错
        int customPropPrice = getCustomPropPrice(propTypeId);
        if (customPropPrice == -1) {
            return propPriceMap.get(propTypeId);
        }
        return customPropPrice;
    }

    /**
     * 自定义道具价格，需要override
     */
    protected abstract int getCustomPropPrice(short propTypeId);

    /**
     * 自定义游戏可以购买的道具id
     */
    protected abstract short[] getStorePropIds();

    /********************************
     * NPC 相关
     ********************************/

    private void initNPC() {
        List<NPC> npcList = generateNPC();
        if (npcList == null) return;
        for(NPC npc: generateNPC()) {
            getMoveableNPCMap().put(npc.id, npc);
            npcChangedList.add(npc);
        }
    }

    /**
     * 生成可以移动的NPC
     */
    protected abstract List<NPC> generateNPC();

    /**
     * 移动NPC
     */
    protected void moveNPC() {
        for (NPC npc: getMoveableNPCMap().values()) {
            if (npc.movable && npc.isPathEmpty()) {
                npc.setPath(navimeshGroup.getPath(
                        gameInfo.getMetagameCode(), npc.position.point,
                        navimeshGroup.getRandomPositionPoint(gameInfo.getMetagameCode())));
            }
            npc.move();
            if (npc.isPositionChanged()) {
                npcChangedList.add(npc);
            }
        }
    }

    /**
     * 根据id获取NPC
     */
    public NPC getNPC(int id) {
        return getMoveableNPCMap().get(id);
    }

    /************************************
     * 以下是可以override的方法
     ************************************/

    /**
     * 设置玩家初始位置，默认为随机位置，静止状态，面向y=0方向
     */
    public void setUserInitPosition(User user) {
        Position userPosition = new Position();
        userPosition.rotateY = 0;
        userPosition.point = navimeshGroup.getRandomPositionPoint(gameInfo.getMetagameCode());
        //userPosition.point.y = 0;
        userPosition.moveState = Constant.MoveState.IDLE;
        user.setPosition(userPosition);
    }

    /**
     * 检查道具是否可以从某用户转移到别的用户, 游戏可以自定义该方法, 默认为不可以
     */
    protected boolean checkIfPropCanTransfer(User user, User targetUser, byte propId) {
        return false;
    }

    /**
     * 获取当前游戏使用的用户类型，子游戏可以自定义用户类型，可以包含一些不同的游戏属性
     */
    public Class<? extends User> getUserClass() {
        return User.class;
    }

    /********************************
     * 一些游戏属性相关的getter, setter
     ********************************/

    /**
     * 获取游戏剩余时间
     * @return
     */
    private long getRemainTime() {
        long remainTime = gameInfo.getDuration() * 1000 - (System.currentTimeMillis() - startTime);
        return remainTime;
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

    /**
     * 获得npc相关的道具，比如捕猎时捕获一个动物，获得一个相应的动物道具
     */
    public Short getRelatedPropByNPC(short npcId) {
        return null;
    }

    public void setNavimeshGroup(NaviMeshGroup navimeshGroup) {
        this.navimeshGroup = navimeshGroup;
    }

}
