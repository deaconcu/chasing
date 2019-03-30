package com.prosper.chasing.game.base;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import com.prosper.chasing.game.map.*;
import com.prosper.chasing.game.message.*;
import com.prosper.chasing.game.util.ByteBuilder;
import com.prosper.chasing.game.util.Constant;
import com.prosper.chasing.game.util.Enums;
import com.prosper.chasing.game.util.Enums.*;
import com.prosper.chasing.game.base.InteractiveObjects.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.prosper.chasing.game.message.Message;
import com.prosper.chasing.game.util.Constant.UserState;
import com.prosper.chasing.common.util.CommonConstant.GameState;
import com.prosper.chasing.common.util.JsonUtil;

/**
 * 游戏主要逻辑对象
 *
 * 游戏场景中包含四种类型物体，分别用不同的map存放
 * 玩家，道具，追踪者，固定物体
 */
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
    protected GameInfo gameInfo;

    // 参与的用户信息
    private Map<Integer, User> userMap;

    // 玩家消息队列
    private Queue<Message> messageQueue = new LinkedList<>();

    private LinkedList<User> movedUserList = new LinkedList<>();

    // 游戏内的全部道具
    protected LinkedList<Short> propList;

    // 游戏场景内的道具信息
    protected Map<Integer, EnvProp> propMap = new HashMap<>();

    private Map<Integer, Stationary> stationaryMap = new HashMap<>();

    private Map<Integer, Interactive> interactiveMap = new HashMap<>();

    private List<Action> actionList = new LinkedList<>();

    // 地图中的npc集合
    private Map<Integer, NPC> npcMap = new HashMap<>();

    protected Rank rank;

    // npc的顺序id号
    private int npcSequenceId = 1;
    private int dynamicObjectSeqId = 1;
    private int interObjectSeqId = 1;

    // prop顺序号id
    protected int nextPropSeqId = 1;
    // 静止NPC顺序号id
    private int staticNPCSeqId = 1;
    // 移动NPC顺序号id
    private int movableNPCSeqId = 1;

    // 处理游戏逻辑的管理器
    private GameManage gameManage;
    private JsonUtil jsonUtil = new JsonUtil();

    // 一些外部依赖
    //protected NavMeshGroup navimeshGroup;

    //protected GameMap gameMap;
    protected MapSkeleton gameMap;

    private Random random = new Random();

    protected boolean isStepChanged = false;

    // 每一次同步的时候有位置变化和buff变化的用户map
    Set<Integer> positionChangedSet = new HashSet<>();
    Set<Integer> buffChangedSet = new HashSet<>();

    Set<GameObject> objectChangedSet = new HashSet<>();

    public long startTime = System.currentTimeMillis();
    private long lastUpdateTime = System.currentTimeMillis();
    private long currentUpdateTime = System.currentTimeMillis();

    private Map<PropType, Prop> propConfig = DefaultPropConfig.getPropConfig();
    private Map<BuffType, Abilitys.Ability[]> buffConfig = DefaultBuffConfig.getBuffConfig();

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
        //propPriceMap.put(PropConfig.IMMUNITY_LEVEL_1, 20);
        propPriceMap.put(PropConfig.IMMUNITY, 20);
        propPriceMap.put(PropConfig.REBOUND, 20);
        propPriceMap.put(PropConfig.NEAR_ENEMY_REMIND, 20);
        propPriceMap.put(PropConfig.PROP_BOMB, 20);
        propPriceMap.put(PropConfig.MONEY, 20);
        propPriceMap.put(PropConfig.GIFT_BOX, 20);
    }

    public abstract GameConfig getGameConfig();

    /********************************
     * 游戏逻辑
     ********************************/

    /**
     * 处理游戏开始之后的一些准备数据，比如设置用户位置, NPC位置等等
     */
    public void prepare() {
        initUser(userMap);
        initProp();
        initGameObject();
    }

    public void createIntroductionMessages() {
        ByteBuilder bb = createIntroductionMessage();
        for (User user: userMap.values())  {
            user.offerMessage(bb);
        }
    }

    protected abstract ByteBuilder createIntroductionMessage();

    /**
     * 初始化游戏场景中动态的游戏对象
     */
    //protected abstract void initDynamicObject();

    /**
     * 初始化可交互对象
     * TODO TEST VERSION
     */
    protected void initGameObject() {
        initSingleSSObbject();
        initStore();
    }

    protected void initSingleSSObbject() {
        for (SpecialSection specialSection: gameMap.specialSectionMap.values()) {
            if (!specialSection.isSingle()) continue;
            RoadPoint roadPoint = specialSection.getRoadPoints()[0];
            if (specialSection.getTerrainType() == Enums.TerrainType.RIVER)
                addGameObject(new River(roadPoint.getPoint().toPoint3(), 90 * 1000 - roadPoint.getDegree()));
            else if (specialSection.getTerrainType() == Enums.TerrainType.STONE)
                addGameObject(new Stone(roadPoint.getPoint().toPoint3(), 90 * 1000 - roadPoint.getDegree()));
            else if (specialSection.getTerrainType() == Enums.TerrainType.FIRE_FENCE)
                addGameObject(new FireFence(roadPoint.getPoint().toPoint3(), 90 * 1000 - roadPoint.getDegree()));
            else if (specialSection.getTerrainType() == Enums.TerrainType.GATE)
                addGameObject(new Gate(roadPoint.getPoint().toPoint3(), 90 * 1000 - roadPoint.getDegree()));
            else continue;
        }
    }

    protected void initStore() {
        List<Segment> segmentList = gameMap.getRandomEndpointSegment(0.5f);
        for (Segment segment : segmentList) {
            RoadPoint roadPoint = segment.getRandomLampPoint(Enums.RoadPointType.WAYSIDE);
            addGameObject(new Stationary(
                    Enums.StationaryType.STORE, roadPoint.getPoint().toPoint3(), - roadPoint.getStationaryDegree()));
        }
    }

    /**
     * 添加场景中的游戏对象
     * @param gameObject
     */
    public void addGameObject(GameObject gameObject) {
        if (gameObject instanceof Stationary) {
            Stationary stationary = (Stationary) gameObject;
            stationaryMap.put(stationary.getId(), stationary);
            log.info("create stationary: " + stationary);
        } else if (gameObject instanceof NPC) {
            NPC npc = (NPC) gameObject;
            npcMap.put(npc.getId(), npc);
        } else if (gameObject instanceof Interactive) {
            Interactive interactive = (Interactive) gameObject;
            interactiveMap.put(interactive.getId(), interactive);
            log.info("create interactive: " + interactive);
        }
        objectChangedSet.add(gameObject);
    }

    /**
     * 移除场景中的游戏对象
     * @param gameObject
     */
    public boolean removeGameObject(GameObject gameObject) {
        if (gameObject == null) return false;

        if (gameObject instanceof Stationary) {
            Stationary stationary = (Stationary) gameObject;
            stationary.setSyncAction(SyncAction.DEAD);
            stationaryMap.remove(stationary.getId());
            objectChangedSet.add(stationary);
        } else if (gameObject instanceof NPC) {
            NPC npc = (NPC) gameObject;
            npc.setSyncAction(SyncAction.DEAD);
            npcMap.remove(npc.getId());
            objectChangedSet.add(npc);
        } else if (gameObject instanceof EnvProp) {
            EnvProp prop = (EnvProp) gameObject;
            prop.setSyncAction(SyncAction.DEAD);
            propMap.remove(prop);
            objectChangedSet.add(prop);
        }
        return true;
    }

    /**
     * 游戏主逻辑
     * 主要包括：处理用户消息，执行预定义游戏逻辑，检查连接等
     */
    public void executeLogic() {
        // set time
        lastUpdateTime = currentUpdateTime;
        currentUpdateTime = System.currentTimeMillis();

        // 检查, 比如玩家是否掉线, 游戏是否结束
        check();
        // 执行玩家消息
        executeMessage();
        // 执行游戏逻辑
        logic();
    }

    /**
     * 游戏场景中的一些逻辑，比如生成道具
     */
    public void logic() {
        // 计算追逐
        doChasing();

        // 执行场景中的道具逻辑，比如生成新的道具，回收到期的道具
        doPropLogic();

        // 执行场景中的用户的逻辑
        doUserLogic();

        // 执行场景中固定物体的逻辑
        doStationaryLogic();

        // 执行场景中可交互物体的逻辑
        doInteractiveLogic();

        // 自定义逻辑
        customLogic();
    }

    /**
     * 执行场景中的用户逻辑
     */
    private void doUserLogic() {
        for (User user: userMap.values()) {
            user.countEnergy();
            //user.countSpeedRate();

            // 用户身上存在buff时需要执行buff逻辑
            for (Buff buff: user.getBuffList()) {
                if (!buff.isValid()) continue;
                for (Abilitys.Ability ability: buffConfig.get(buff.typeId)) {
                    ability.apply(this, user, user);
                }
            }

            // 持有BUFF_ON类型道具时需要添加buff, 比如权杖
            for (short propType: user.getPropMap().keySet()) {
                if (user.getPropMap().get(propType) <= 0) continue;

                Prop prop = propConfig.get(propType);
                for (Abilitys.Ability ability: prop.getAbilities(PropUsageType.HOLD)) {
                    ability.apply(this, user, user);
                }
            }

            // 用户处在特殊路段的时候执行特殊路段逻辑
            SpecialSection specialSection = gameMap.getSpecialSection(user.getPoint3().x, user.getPoint3().z);
            if (specialSection == null) user.clearTerrainBuff();
            else doSpecialSection(specialSection, user);

            doCustomUserLogic(user);
        }
    }

    private void doSpecialSection(SpecialSection specialSection, User user) {
        if (specialSection.getTerrainType() == Enums.TerrainType.FOG) {
            if (!user.isCrossZone()) return;
            user.addBuff(BuffConfig.SPEED_DOWN_LEVEL_1_TERRAIN, specialSection.getId());
        } else if (specialSection.getTerrainType() == Enums.TerrainType.RAIN) {
            if (!user.isCrossZone()) return;
            user.addBuff(BuffConfig.SPEED_DOWN_LEVEL_2_TERRAIN, specialSection.getId());
        } else if (specialSection.getTerrainType() == Enums.TerrainType.SNOW) {
            if (!user.isCrossZone()) return;
            user.addBuff(BuffConfig.SPEED_DOWN_LEVEL_3_TERRAIN, specialSection.getId());
        } else if (specialSection.getTerrainType() == Enums.TerrainType.DREAM_L1) {
            if (!user.isCrossZone()) return;
            if (user.addBuff(BuffConfig.SLEEPY_LEVEL_1, specialSection.getId())) {
                user.setSystemTargetObject(user.getPoint3());
            }
        } else if (specialSection.getTerrainType() == Enums.TerrainType.DREAM_L2) {
            if (!user.isCrossZone()) return;
            if (user.addBuff(BuffConfig.SLEEPY_LEVEL_2, specialSection.getId())) {
                user.setSystemTargetObject(user.getPoint3());
            }
        } else if (specialSection.getTerrainType() == Enums.TerrainType.ANIMAL_OSTRICH) {
                /*
                if (!user.isCrossZone()) continue;
                if (!user.hasBuff(BuffConfig.ANIMAL)) {
                    int blockId = gameMap.getNearestEndPoint(user.getPoint3(), blockGroup);

                    List<Integer> blockList = gameMap.getUnoccupiedBlocksInDistance(blockId, 8);
                    int chosenBlockId = blockList.get(ThreadLocalRandom.current().nextInt(blockList.size()));
                    Point3 point3 = gameMap.getPoint(chosenBlockId);

                    Animal animal = new Animal(
                            getNextNpcId(),
                            Enums.AnimalType.TIGER,
                            new Point3(point3.x * 1000, 0, point3.z * 1000),
                            0,
                            blockGroup.getId(),
                            user);

                    animal.setTargetUser(user);
                    animal.setSpeed(500);

                    addGameObject(animal);
                    user.addBuff(BuffConfig.ANIMAL, (short)-1, specialSection.getId());
                }
                */
        } else if (specialSection.getTerrainType() == Enums.TerrainType.WIND) {
                /*
                if (!user.isCrossZone()) continue;
                BlockGroup blockGroup = gameMap.getBlockGroup(block.blockGroupId);
                user.addBuff(BuffConfig.WIND, (short)30, block.blockGroupId,
                    gameMap.getPoint(blockGroup.getStartBlockId()));
                */
        } else if (specialSection.getTerrainType() == Enums.TerrainType.WILD_FIRE) {
            if  (ThreadLocalRandom.current().nextInt(10000) < 3) {
                user.setGhost(true);
                Stationary fire = new Stationary(
                        Enums.StationaryType.FIRE,
                        new Point3(user.getPoint3().x, 0, user.getPoint3().z),
                        0,
                        (short)3);
                addGameObject(fire);
            }
        } else {
            user.clearTerrainBuff();
        }

        if (user.hasBuff(BuffConfig.SLEEPY_LEVEL_1) && !user.hasBuff(BuffConfig.DREAMING)
                && user.getSpeed() < 2 && user.allowIntoDream()) {
            user.addBuff(BuffConfig.DREAMING, (short) 15, true);
            user.setLastDreamTime(System.currentTimeMillis());
        } else if (user.hasBuff(BuffConfig.SLEEPY_LEVEL_2) && !user.hasBuff(BuffConfig.DREAMING)
                && user.getSpeed() < 4 && user.allowIntoDream()) {
            user.addBuff(BuffConfig.DREAMING, (short)15, true);
            user.setLastDreamTime(System.currentTimeMillis());
        }
    }

    protected abstract void doCustomUserLogic(User user);

    /**
     * 执行场景中固定对象的逻辑
     */
    private void doStationaryLogic() {
        List<Stationary> needRemoveList = null;
        for (Stationary stationary: stationaryMap.values()) {
            if (stationary.getEndTime() < System.currentTimeMillis()) {
                if (needRemoveList == null) needRemoveList = new LinkedList<>();
                needRemoveList.add(stationary);
            }
        }

        if (needRemoveList != null) {
            for (Stationary stationary: needRemoveList) {
                removeGameObject(stationary);
            }
        }
    }

    /**
     *
     */
    private void doInteractiveLogic() {
        for (Interactive interactive: interactiveMap.values()) {
            interactive.logic(this);
        }
    }

    /**
     * 游戏自定义逻辑
     */
    protected abstract void customLogic();

    /**
     * 初始化用户
     * @param userMap
     */
    private void initUser(Map<Integer, User> userMap) {
        for (User user: userMap.values())  {
            user.init();
        }
        customInitUser(userMap);
    }

    /**
     * 一些游戏数据的检查
     */
    public void check() {
        // 检查游戏是否结束
        if (getRemainTime() <= 0) {
            state = GameState.FINISHED;
            for (User user: userMap.values()) {
                user.setState(UserState.FINISHED);
            }
        }

        if (getRemainTime() <= -10000) {
            state = GameState.DESTROYING;
        }

        // 检查用户是否掉线，是否已结束游戏（死亡或者获胜）
        for (User user: userMap.values()) {
            user.check();
            /*
            if (user.getState() == UserState.FINISHED) {
                user.setState(UserState.RESULT_INFORMED);
            }*/
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
            log.info("executeLogic user message: {}", Arrays.toString(userMessage.getContent().array()));
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
            } else if (message instanceof ObjectPositionMessage) {
                executeObjectPositionMessage((ObjectPositionMessage) message);
            } else if (message instanceof PropMessage) {
                executePropMessage((PropMessage) message);
            } else if (message instanceof TransferPropMessage) {
                executeTransferPropMessage((TransferPropMessage) message);
            } else if (message instanceof PurchaseMessage) {
                executePurchaseMessage((PurchaseMessage) message);
            } else if (message instanceof TargetMessage){
                executeTargetMessage((TargetMessage) message);
            } else if (message instanceof FocusMessage){
                executeFocusMessage((FocusMessage) message);
            } else if (message instanceof InteractionMessage){
                executeInteractionMessage((InteractionMessage) message);
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
     * 1.游戏场景中改变的数据需要同步到用户
     * 2.用户自身的修改需要同步给用户
     */
    public void generateUserMessage() {
        for (User user: userMap.values()) {
            if (user.isMoved() || user.buffChangedSet.size() != 0) objectChangedSet.add(user);
        }

        ByteBuilder gameChangeBytes = changesToBytes();
        for (User user: userMap.values()) {
            if (gameChangeBytes != null) {
                user.offerMessage(gameChangeBytes);
            }

            ByteBuilder userChangeBytes = user.bytesOfChangesToMe();
            if (userChangeBytes != null) {
                user.offerMessage(userChangeBytes);
            }
            user.clearAfterSync();
        }

        objectChangedSet.clear();
        positionChangedSet.clear();
        buffChangedSet.clear();
        actionList.clear();
    }

    /**
     * sign: gameChanges|focusUserInfo|target|rank|RESERVED|RESERVED|action|state|
     *     position|life|speed|buff|prop|customProperty|RESERVED|RESERVED|
     */
    private ByteBuilder changesToBytes() {
        ByteBuilder byteBuilder =  new ByteBuilder();
        int seqId = 0;
        byteBuilder.append(seqId);
        byteBuilder.append(Constant.MessageType.USER);

        short sign = 0;
        byteBuilder.append(sign);
        byteBuilder.append(System.currentTimeMillis());

        if (objectChangedSet.size() > 0 || actionList.size() > 0) {
            if (byteBuilder == null) {
                byteBuilder =  new ByteBuilder();
            }
            sign = (short) (sign | 4096);
            byteBuilder.append((short)objectChangedSet.size());
            for (GameObject gameObject: objectChangedSet) {
                gameObject.appendBytes(byteBuilder);
            }

            byteBuilder.append((short)actionList.size());
            for (Action action: actionList) {
                Action.PropAction propAction = (Action.PropAction) action;
                byteBuilder.append(propAction.propTypeId);
                byteBuilder.append(propAction.userId);
                byteBuilder.append(propAction.targetType.getValue());
                byteBuilder.append(propAction.targetId);
            }
        }

        if (rank.isChanged()) {
            if (byteBuilder == null) {
                byteBuilder =  new ByteBuilder();
            }
            sign = (short) (sign | 2048);
            rank.appendBytes(byteBuilder);
        }

        // 如果没有需要同步的内容，返回null
        if (sign == 0) {
            return null;
        } else {
            byteBuilder.set(sign, 5);
            return byteBuilder;
        }
    }

    /**
     * 游戏开始后的准备工作
     * @param gameManage
     * @param gameMap
     * @param gameInfo
     * @param userList
     */
    public void init(GameManage gameManage, MapSkeleton gameMap, GameInfo gameInfo, List<? extends User> userList) {
        setGameManage(gameManage);
        setGameMap(gameMap);
        setGameInfo(gameInfo);
        loadUser(userList);

        generatePrepareMessage();
        setState(GameState.PREPARE);

        customInit();
    }

    /**
     * 自定义游戏的一些初始化工作
     */
    protected abstract void customInit();

    /**
     * 初始化道具
     */
    private void initProp() {
        propList = getGameConfig().getPropConfig().generatePropList();
    }

    /**
     * 同步游戏开始之后的一些数据，比如用户名, 游戏时间等，格式如下
     *
     * seqId(4)|messageType(1)|remainTime(4)|UserCount(1)|UserList[]|propCount(2)|list<PropPrice>|Map
     *
     * User: userId(4)|nameLength(1)|name
     * PropPrice: objectId(2)|price(4)
     * //Map: boundX(4)|boundY(4)|mapByteCount|List<byte>
     * Building: objectId(4)|type(1)|positionX(4)|positionY(4)|orientation(1)
     */
    public void generatePrepareMessage() {
        ByteBuilder bb = new ByteBuilder();
        bb.append(0);
        bb.append(Constant.MessageType.PREPARE);
        byte[] metagameCodeBytes = gameInfo.getMetagameCode().getBytes();
        bb.append(metagameCodeBytes.length);
        bb.append(metagameCodeBytes);
        bb.append(gameInfo.getDuration());
        bb.append(getGameConfig().getFirstRankValueType().getValue());
        bb.append(getGameConfig().getSecondRankValueType().getValue());
        bb.append((byte)userMap.size());
        for (User user: userMap.values()) {
            bb.append(user.getId());
            bb.append(user.getRoleType());
            bb.append(user.getName().getBytes().length);
            bb.append(user.getName().getBytes());
        }

        short[] storePropIds = getGameConfig().getStorePropIds();
        bb.append((short)storePropIds.length);
        for (short storePropId: storePropIds) {
            int price = getPropPrice(storePropId);
            bb.append(storePropId);
            bb.append(price);
        }

        /*
        bb.append(gameMap.boundX);
        bb.append(gameMap.boundY);

        byte[] mapBytes = gameMap.getMapBytes();
        bb.append(mapBytes.length);
        bb.append(mapBytes);
        */

        /*
        bb.append(stationaryMap.distance());
        for (Stationary interactiveObject: stationaryMap.values()) {
            bb.append(interactiveObject.getId());
            bb.append(interactiveObject.getType());
            bb.append(interactiveObject.getPoint3().x);
            bb.append(interactiveObject.getPoint3().y);
            bb.append(interactiveObject.getPoint3().z);
            bb.append(interactiveObject.getRotateY());
        }
        */

        if (bb.getSize() > 0) {
            for (User user: userMap.values()) {
                log.info("init message offered");
                user.offerMessage(bb);
            }
        }
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
        user.setRotateY(message.rotationY);
        user.setPoint3(new Point3(message.positionX, message.positionY, message.positionZ));
        user.setMoveState(message.moveState);
        user.addSteps(message.steps);
        user.setSpeed(message.speed);

        movedUserList.add(user);

        if (user.hasBuff(BuffConfig.INVISIBLE_LEVEL_1)) {
            user.removeBuff(BuffConfig.INVISIBLE_LEVEL_1);
        }
        doPositionChanged(user);
    }

    /**
     * 处理其他对象的位置消息
     */
    public void executeObjectPositionMessage(ObjectPositionMessage message) {
        /**
         NPC npc = npcMap.get(message.objectId);
         if (npc == null) return;

         // TODO check if could move
         npc.setRotateY(message.rotationY);
         npc.setPoint3(new Point3(message.positionX, message.positionY, message.positionZ));
         //user.setMoveState(message.moveState);

         npcChangedSet.add(npc);
         */
    }

    /**
     * 当用户的位置有变化时，执行一些自定义逻辑
     */
    protected abstract void doPositionChanged(User user);

    /**
     * 处理使用道具消息
     */
    public void executePropMessage(PropMessage message) {
        User user = getUser(message.getUserId(), true);
        if (user.getState() == UserState.FINISHED) return;

        User toUser;
        if (message.getToUserId() == -1)  {
            toUser = user;
        } else {
            toUser = getUser(message.getToUserId(), false);
        }

        // TODO need notice
        if (toUser == null) return;

        // check if user prop is enough
        short propTypeId = message.getPropTypeId();
        if (user.checkProp(propTypeId, (byte)1)) {
            //user.useProp(propTypeId, toUser);

            Prop prop = propConfig.get(propTypeId);
            if (!prop.usable()) return;

            boolean testGood = true;
            for (Abilitys.Ability ability: prop.getAbilities(PropUsageType.USE)) {
                if (!ability.test(this, user, toUser)) testGood = false;
            }

            if (testGood) {
                for (Abilitys.Ability ability: prop.getAbilities(PropUsageType.USE)) {
                    ability.test(this, user, toUser);
                    ability.apply(this, user, toUser);
                }
                actionList.add(new Action.PropAction(
                        propTypeId, user.getId(), message.getType(), message.getToUserId()));
            }

            /*
            if(PropConfig.getProp(propTypeId).use(message, user, toUser, this)) {
                actionList.add(new Action.PropAction(propTypeId, user.getId(), message.getType(), message.getToUserId()));
            }
            */
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
        if (user.getState() == UserState.FINISHED) return;

        int price = getPropPrice(message.itemId);
        if (price == -1) {
            return;
        }
        user.purchaseProp(message.itemId, price);
    }

    /**
     * 处理交互消息
     */
    public void executeInteractionMessage(InteractionMessage message) {
        User user = getUser(message.getUserId(), true);
        if (user.getState() == UserState.FINISHED) return;
        // for override

        if (message.objectType == GameObjectType.INTERACTIVE) {
            Interactive interactive = interactiveMap.get(message.objectId);
            if (interactive == null) return;

            // TODO 需要判断玩家是不是在这个对象附近
            boolean success = interactive.interact(this, user, message.objectState);
            if (success) objectChangedSet.add(interactive);
        } else if (message.objectType ==  GameObjectType.PROP) {
            EnvProp prop = propMap.get(message.objectId);
            if (prop != null && removeGameObject(prop)) {
                user.increaseProp(prop.typeId, (short)1);
            }
        }
    }

    /**
     * 处理设置目标的消息
     */
    public void executeTargetMessage(TargetMessage message) {
        User user = getUser(message.getUserId(), true);
        user.setTarget(message.getType(), message.getId(), message.getPoint3());
    }

    /**
     * 处理聚焦的消息
     * 0表示聚焦对象为空
     */
    private void executeFocusMessage(FocusMessage message) {
        User user = getUser(message.getUserId(), true);
        User focusUser = userMap.get(message.getId());
        if (focusUser != null) {
            user.setFocus(focusUser);
        } else {
            user.setFocus(null);
        }
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

                if (replyMessage.getContent().array().length < 300)
                    log.info("reply bytes: {}", Arrays.toString(replyMessage.getContent().array()));
                else
                    log.info("reply bytes: {}",
                            Arrays.toString(Arrays.copyOfRange(replyMessage.getContent().array(), 0, 300)) + "...");
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

        rank = new Rank(userList, getGameConfig().getFirstRankValueType(), getGameConfig().getSecondRankValueType());
    }

    /**
     * 获取用户
     * @isThrow 在用户不存在的时候是否抛出异常
     */
    private User getUser(int userId, boolean isThrow) {
        User user = userMap.get(userId);
        if (user == null && isThrow) {
            throw new RuntimeException("user is not exist, user objectId:" + userId);
        }
        return user;
    }

    /**
     * 通过计算距离来更新用户的被追逐信息
     */
    protected void doChasing() {
        // TODO 50为最大group数，需要写到游戏配置中
        long[] groupChasingTimes = new long[50];
        byte[] groupChasingCounts = new byte[50];

        // 更新用户的被追逐buff, buff时间应该等于：
        //     1: 如果追逐方中有人持有权杖，buff时间为持有权杖的人的最早的startTime
        //     2: 如果追逐方有两人为同一group，且与被追逐人不同，buff时间为相同group中，
        //        chasingInfo中的最迟startTime中的最早的
        for (User user: userMap.values()) {

            long buffStartTime = Long.MAX_VALUE;

            for(User.ChasingInfo chasingInfo: user.getChasingInfoMap().values()) {
                // 如果追逐方与被追逐方的距离接近CHASING_DISTANCE, 且chasingInfo中startTime为0, 将startTime置为当前时间
                // 如果追逐方与被追逐方的距离超过CHASING_DISTANCE, 且chasingInfo中startTime不为0，将startTime置0
                // 只计算不同group的玩家

                if (user.getGroupId() == chasingInfo.chasingUser.getGroupId()) continue;
                if (user.getPoint3().distance(chasingInfo.chasingUser.getPoint3()) < 10) {
                    if (chasingInfo.startTime == 0) chasingInfo.startTime = System.currentTimeMillis();
                } else {
                    if (chasingInfo.startTime != 0) chasingInfo.startTime = 0;
                }

                if (chasingInfo.chasingUser.hasBuff(BuffConfig.HOLD_SCEPTER) && chasingInfo.startTime != 0 &&
                        buffStartTime > chasingInfo.startTime) {
                    buffStartTime = chasingInfo.startTime;
                }

                groupChasingCounts[chasingInfo.chasingUser.getGroupId()] ++;
                long startTime = groupChasingTimes[chasingInfo.chasingUser.getGroupId()];
                if (startTime < chasingInfo.startTime) {
                    groupChasingTimes[chasingInfo.chasingUser.getGroupId()] = chasingInfo.startTime;
                }
            }

            for (int i = 0; i < groupChasingCounts.length; i ++) {
                byte groupChasingCount = groupChasingCounts[i];
                long groupChasingTime = groupChasingTimes[i];

                if (groupChasingCount >= 2) {
                    if (buffStartTime > groupChasingTime) {
                        buffStartTime = groupChasingTime;
                    }
                }
            }

            /** TODO
             // 新增或者修改被追逐的buff
             long currentStartTime = 0;
             if (user.hasBuff(BuffConfig.EXPEL)) {
             currentStartTime =  (Long)user.getBufferInfo(BuffConfig.EXPEL).get(0);
             }

             if (buffStartTime != currentStartTime) {
             user.removeBuff(BuffConfig.EXPEL);
             user.addBuff(BuffConfig.EXPEL, 0, buffStartTime);
             }
             */

            for (int i = 0; i < groupChasingCounts.length; i ++) {
                groupChasingCounts[i] = 0;
                groupChasingTimes[i] = 0;
            }
        }
    }

    /**
     * 获取某一个位置周围的用户列表
     * TODO 需要性能优化
     */
    private List<User> getAroundUserList(User fromUser) {
        List<User> aroundUserList = null;
        Point3 position = fromUser.getPoint3();
        for (User user: userMap.values()) {
            if (user.getId() == fromUser.getId()) continue;
            if ((user.getPoint3().x - position.x) < 10 && (user.getPoint3().y - position.y < 10)) {
                if (user.getPoint3().distance(position) < 10) {
                    if (aroundUserList == null) {
                        aroundUserList = new LinkedList<>();
                    }
                    aroundUserList.add(user);
                }
            }
        }
        return aroundUserList;
    }

    /********************************
     * prop 相关
     ********************************/

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
     * 移动可移动道具
     * 1. 道具离玩家太近会自动跑开(暂时不做)
     * 2. 如果被玩家标记且距离太近，道具会离开，且会尝试摆脱
     * 3. 可以被多名玩家追逐
     */
    /*
    protected void moveProp() {
        ListIterator<EnvProp> iterator = getPropInSceneList().listIterator();
        while(iterator.hasNext()) {
            EnvProp prop = iterator.next();
            if(!prop.movable) continue;

            // TODO  navimesh is no longer used
            /*
            // 设置路径
            if (prop.isPathEmpty()) {
                prop.setPath(navimeshGroup.getPath(
                        gameInfo.getMetagameCode(), prop.position.point3,
                        navimeshGroup.getRandomPositionPoint(gameInfo.getMetagameCode())));
            }

            // 移动道具
            prop.move();
            */
    /*
            if (prop.isPositionChanged()) {
                getEnvPropChangedList().add(prop);
                prop.setPositionChanged(false);
            }
        }
    }
    */

    /**
     * 处理道具
     */
    protected void doPropLogic() {
        // 生成新的道具
        int propInStock = getGameConfig().getPropConfig().getPropRemained((int)getGameTime() / 1000);
        int count = propList.size() - propInStock;
        if (count <= 0) return;

        while (count > 0) {
            EnvProp envProp = new EnvProp(this);

            envProp.typeId = propList.removeFirst();
            envProp.setId(nextPropSeqId ++);

            RoadPoint roadPoint = gameMap.getRandomPoint(RoadPointType.CENTER);
            envProp.setPoint3(new Point3(roadPoint.getPoint().x, 100, roadPoint.getPoint().y));
            envProp.createTime = System.currentTimeMillis();
            envProp.vanishTime = envProp.createTime +
                    getGameConfig().getPropConfig().getPropConfig(envProp.typeId).duration * 1000;
            propMap.put(envProp.getId(), envProp);
            objectChangedSet.add(envProp);
            log.info("created prop: {}:{}-{}:{}:{}", gameInfo.getId(), envProp.getId(),
                    envProp.getPoint3().x, envProp.getPoint3().y, envProp.getPoint3().z);

            count --;
        }

        // 为到期的道具生成新的位置
        /*
        ListIterator<EnvProp> iterator = propMap.listIterator();
        while(iterator.hasNext()) {
        */
        for (EnvProp prop: propMap.values()) {
            // 如果道具到期，移除道具
            if (prop.vanishTime <= System.currentTimeMillis()) {
                RoadPoint roadPoint = gameMap.getRandomPoint(RoadPointType.CENTER);
                // TODO 忘记了什么意思
                prop.setPoint3(new Point3(roadPoint.getPoint().x, 1100, roadPoint.getPoint().y));
                prop.createTime = System.currentTimeMillis();
                prop.vanishTime = prop.createTime +
                        getGameConfig().getPropConfig().getPropConfig(prop.typeId).duration * 1000;

                objectChangedSet.add(prop);
                //log.info("recreated prop: {}:{}:{}", gameInfo.getId(), prop.objectId, getEnvPropChangedList());
                continue;
            }
        }
    }

    /**
     * 根据id获取环境道具
     */
    public EnvProp getProp(int id) {
        return propMap.get(id);
        /*
        for (EnvProp prop: propInSceneList) {
            if (prop.getId() == objectId) {
                return prop;
            }
        }
        return null;
        */
    }

    /**
     * 获取道具价格
     */
    public int getPropPrice(short propTypeId) {
        if (getGameConfig().getPropPriceMap().containsKey(propTypeId))
            return getGameConfig().getPropPriceMap().get(propTypeId);
        return propPriceMap.get(propTypeId);
    }

    /********************************
     * NPC 相关
     ********************************/

    /**
     * 根据id获取NPC
     */
    public Stationary getStationary(int id) {
        return stationaryMap.get(id);
    }

    public Interactive getInteractive(int id) {
        return interactiveMap.get(id);
    }

    /************************************
     * 以下是可以override的方法
     ************************************/

    /**
     * 设置玩家初始位置
     */
    protected abstract void customInitUser(Map<Integer, User> userMap);

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
     * 获取游戏剩余时间, 单位毫秒
     */
    private long getRemainTime() {
        long remainTime = gameInfo.getDuration() * 1000 - (System.currentTimeMillis() - startTime);
        return remainTime;
    }

    /**
     * 获取游戏运行时间，单位毫秒
     */
    protected long getGameTime() {
        return System.currentTimeMillis() - startTime;
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

    public MapSkeleton getGameMap() {
        return gameMap;
    }

    public void setGameMap(MapSkeleton gameMap) {
        this.gameMap = gameMap;
    }

    public Set<GameObject> getObjectChangedSet() {
        return objectChangedSet;
    }
}
