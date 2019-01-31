package com.prosper.chasing.game.base;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import com.prosper.chasing.game.games.Marathon;
import com.prosper.chasing.game.map.*;
import com.prosper.chasing.game.message.*;
import com.prosper.chasing.game.navmesh.NavMeshGroup;
import com.prosper.chasing.game.util.ByteBuilder;
import com.prosper.chasing.game.util.Constant;
import com.prosper.chasing.game.util.Enums;
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

    // 地形map
    protected Map<Integer, TerrainBlock> terrainBlockMap = new HashMap<>();

    private LinkedList<User> movedUserList = new LinkedList<>();

    //private Map<Integer, DynamicGameObject> dGameObjectMap = new HashMap<>();

    // 游戏内的全部道具
    protected LinkedList<Short> propList;

    // 游戏场景内的道具信息
    protected Map<Integer, EnvProp> propMap = new HashMap<>();

    private Map<Integer, Stationary> stationaryMap = new HashMap<>();

    private List<Action> actionList = new LinkedList<>();

    // 地图中的npc集合
    private Map<Integer, NPC> npcMap = new HashMap<>();

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

    protected GameMap gameMap;

    private Random random = new Random();

    protected boolean isStepChanged = false;
    // 每一次同步的时候有位置变化和buff变化的用户map
    List<EnvProp> envPropChangedList = new LinkedList<>();
    Set<Integer> positionChangedSet = new HashSet<>();
    Set<Integer> buffChangedSet = new HashSet<>();
    Set<Integer> terrainChangedSet = new HashSet<>();
    Set<NPC> npcChangedSet = new HashSet<>();
    Set<DynamicGameObject> dObjectChangedSet = new HashSet<>();
    Set<Stationary> stationaryChangedSet = new HashSet<>();

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
        //propPriceMap.put(PropConfig.IMMUNITY_LEVEL_1, 20);
        propPriceMap.put(PropConfig.IMMUNITY, 20);
        propPriceMap.put(PropConfig.REBOUND, 20);
        propPriceMap.put(PropConfig.NEAR_ENEMY_REMIND, 20);
        propPriceMap.put(PropConfig.PROP_BOMB, 20);
        propPriceMap.put(PropConfig.MONEY, 20);
        propPriceMap.put(PropConfig.GIFT_BOX, 20);
    }

    public Animal getAnimalOfBlockGroup() {
        // TODO
        return null;
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

    /********************************
     * 游戏逻辑
     ********************************/

    /**
     * 处理游戏开始之后的一些准备数据，比如设置用户位置, NPC位置等等
     */
    public void prepare() {
        initUser(userMap);
        initNPC();
        initProp();
        //initLamp();
        //initDynamicObject();
        initStationary();
    }

    public void createIntroductionMessages() {
        ByteBuilder bb = createIntroductionMessage();
        for (User user: userMap.values())  {
            user.offerMessage(bb);
        }
    }

    protected abstract ByteBuilder createIntroductionMessage();


    /*
    private void initLamp() {
        for (Block block: gameMap.occupiedBlockMap.values()) {
            int id = getNextDObjectId();
            if (block.distanceAwayFromRoadCrossPoint == 0) {
                dGameObjectMap.put(
                        id,
                        new DynamicGameObject(
                                Enums.StationaryType.LAMP,
                                id,
                                new Point(block.position.x * 1000, 0, block.position.y * 1000),
                                ThreadLocalRandom.current().nextInt(360),
                                Short.MAX_VALUE));
            }
        }
    }
    */

    /**
     * 初始化游戏场景中动态的游戏对象
     */
    //protected abstract void initDynamicObject();

    /**
     * 初始化可交互对象
     * TODO TEST VERSION
     */
    protected void initStationary() {

    }

    /**
     * 添加场景中的游戏对象
     * @param gameObject
     */
    protected void addGameObject(GameObject gameObject) {
        if (gameObject instanceof Stationary) {
            Stationary stationary = (Stationary) gameObject;
            stationaryMap.put(stationary.getId(), stationary);
            stationaryChangedSet.add(stationary);
        } else if (gameObject instanceof NPC) {
            NPC npc = (NPC) gameObject;
            npcMap.put(npc.getId(), npc);
            npcChangedSet.add(npc);
        }
    }

    /**
     * 移除场景中的游戏对象
     * @param gameObject
     */
    public boolean removeGameObject(GameObject gameObject) {
        if (gameObject == null) return false;

        if (gameObject instanceof Stationary) {
            Stationary stationary = (Stationary) gameObject;
            stationary.setSyncAction(Enums.SyncAction.DEAD);
            stationaryMap.remove(stationary.getId());
            stationaryChangedSet.add(stationary);
        } else if (gameObject instanceof NPC) {
            NPC npc = (NPC) gameObject;
            npc.setSyncAction(Enums.SyncAction.DEAD);
            npcMap.remove(npc.getId());
            npcChangedSet.add(npc);
        } else if (gameObject instanceof EnvProp) {
            EnvProp prop = (EnvProp) gameObject;
            prop.setSyncAction(Enums.SyncAction.DEAD);
            propMap.remove(prop);
            envPropChangedList.add(prop);
        }
        return true;
    }

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
        // 执行玩家消息
        executeMessage();
        // 执行游戏逻辑
        logic();
    }

    /**
     * 游戏场景中的一些逻辑，比如生成道具
     */
    public void logic() {
        // 执行npc逻辑
        doNPCLogic();

        // 移动所有可移动道具
        //moveProp();

        // 计算追逐
        doChasing();

        // 执行道具逻辑，比如生成新的道具，回收到期的道具
        doPropLogic();

        // 执行buff的逻辑
        doBuffLogic();

        // 执行固定物体的逻辑
        doStationaryLogic();

        // npc
        //moveNPC();

        // 自定义逻辑
        customLogic();
    }

    /**
     * 执行buff的一些逻辑
     */
    private void doBuffLogic() {
        for (User user: userMap.values()) {

            user.countEnergy();
            user.countSpeedRate();

            Block block = gameMap.getBlock(user.getPoint().x / 1000, user.getPoint().z / 1000);
            if (block == null || !gameMap.isOccupied(block.blockId)) continue;

            if (gameMap.getTerrainType(block) == Enums.TerrainType.FOG) {
                if (!user.isCrossZone()) continue;
                user.addBuff(BuffConfig.SPEED_DOWN_LEVEL_1_TERRAIN, (short)-1, block.blockGroupId);
            } else if (gameMap.getTerrainType(block) == Enums.TerrainType.RAIN) {
                if (!user.isCrossZone()) continue;
                user.addBuff(BuffConfig.SPEED_DOWN_LEVEL_2_TERRAIN, (short)-1, block.blockGroupId);
            } else if (gameMap.getTerrainType(block) == Enums.TerrainType.SNOW) {
                if (!user.isCrossZone()) continue;
                user.addBuff(BuffConfig.SPEED_DOWN_LEVEL_3_TERRAIN, (short)-1, block.blockGroupId);
            } else if (gameMap.getTerrainType(block) == Enums.TerrainType.DREAM_L1) {
                if (!user.isCrossZone()) continue;
                if (user.addBuff(BuffConfig.SLEEPY_LEVEL_1, (short)-1, block.blockGroupId)) {
                    user.setDreamTargetObject(user.getPoint());
                }
            } else if (gameMap.getTerrainType(block) == Enums.TerrainType.DREAM_L2) {
                if (!user.isCrossZone()) continue;
                if (user.addBuff(BuffConfig.SLEEPY_LEVEL_2, (short)-1, block.blockGroupId)) {
                    user.setDreamTargetObject(user.getPoint());
                }
            } else if (gameMap.getTerrainType(block) == Enums.TerrainType.ANIMAL_OSTRICH) {
                if (!user.isCrossZone()) continue;
                if (!user.hasBuff(BuffConfig.ANIMAL)) {
                    BlockGroup blockGroup = gameMap.getBlockGroup(block.blockGroupId);
                    int blockId = gameMap.getNearestEndPoint(user.getPoint(), blockGroup);

                    List<Integer> blockList = gameMap.getUnoccupiedBlocksInDistance(blockId, 8);
                    int chosenBlockId = blockList.get(ThreadLocalRandom.current().nextInt(blockList.size()));
                    Point point = gameMap.getPoint(chosenBlockId);

                    Animal animal = new Animal(
                            getNextNpcId(),
                            Enums.AnimalType.TIGER,
                            new Point(point.x * 1000, 0, point.z * 1000),
                            0,
                            blockGroup.getId(),
                            user);

                    animal.setTargetUser(user);
                    addGameObject(animal);
                    user.addBuff(BuffConfig.ANIMAL, (short)-1, block.blockGroupId);
                }
            } else if (gameMap.getTerrainType(block) == Enums.TerrainType.WIND) {
                if (!user.isCrossZone()) continue;
                BlockGroup blockGroup = gameMap.getBlockGroup(block.blockGroupId);
                user.addBuff(BuffConfig.WIND, (short)30, block.blockGroupId, gameMap.getPoint(blockGroup.getStartBlockId()));
            } else if (gameMap.getTerrainType(block) == Enums.TerrainType.WILD_FIRE) {
                if  (ThreadLocalRandom.current().nextInt(10000) < 3) {
                    user.setGhost(true);
                    Stationary fire = new Stationary(
                            Enums.StationaryType.FIRE,
                            new Point(user.getPoint().x, 0, user.getPoint().z),
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
    }

    /**
     * 执行地形的逻辑
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
     * 执行npc的业务逻辑
     */
    private void doNPCLogic() {
        List<NPC> needRemoveNPCList = null;
        for (NPC npc: npcMap.values()) {
            if (npc.isAlive()) {
                npc.logic(getNearByPlayer());
            } else {
                if (needRemoveNPCList == null) needRemoveNPCList = new LinkedList<>();
                needRemoveNPCList.add(npc);
            }
        }

        if (needRemoveNPCList != null) {
            for (NPC npc: needRemoveNPCList) {
                removeGameObject(npc);
            }
        }
    }

    /**
     * 游戏自定义逻辑
     */
    protected void customLogic() {

    }

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
     * 获得某一个物体周边玩家
     */
    private Map<Integer, User> getNearByPlayer() {
        // TODO
        return null;
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
            /*
            if (user.getState() == UserState.GAME_OVER) {
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
            if (user.isMoved()) {
                positionChangedSet.add(user.getId());
            }
            if (user.buffChangedSet.size() != 0) {
                buffChangedSet.add(user.getId());
            }
        }

        ByteBuilder gameChangeBytes = changesToBytes();

        for (User user: userMap.values()) {
            if (gameChangeBytes != null) {
                user.offerMessage(gameChangeBytes);
            }

            ByteBuilder userChangeBytes = user.ChangesToBytes();
            if (userChangeBytes != null) {
                user.offerMessage(userChangeBytes);
            }
            user.clearAfterSync();
        }

        envPropChangedList.clear();
        npcChangedSet.clear();
        positionChangedSet.clear();
        buffChangedSet.clear();
        npcChangedSet.clear();
        dObjectChangedSet.clear();
        stationaryChangedSet.clear();
        actionList.clear();
    }

    /**
     * sign: gameChanges|focusUserInfo|target|RESERVED|RESERVED|RESERVED|action|state|
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

        if (terrainChangedSet.size() > 0 || stationaryChangedSet.size() > 0 ||
                envPropChangedList.size() > 0 || npcChangedSet.size() > 0 || positionChangedSet.size() > 0 ||
                buffChangedSet.size() > 0  || actionList.size() > 0) {
            if (byteBuilder == null) {
                byteBuilder =  new ByteBuilder();
            }
            sign = (short) (sign | 4096);
            byteBuilder.append((short)terrainChangedSet.size());
            for (int specialRoadId: terrainChangedSet) {
                BlockGroup blockGroup = gameMap.blockGroupMap.get(specialRoadId);
                byteBuilder.append(blockGroup.getId());
                byteBuilder.append(blockGroup.getTerrainType().getValue());
            }

            /*
            byteBuilder.append((short)dObjectChangedSet.size());
            for(DynamicGameObject dynamicGameObject: dObjectChangedSet) {
                byteBuilder.append(dynamicGameObject.getSyncAction().getValue());
                byteBuilder.append(dynamicGameObject.getId());
                byteBuilder.append(dynamicGameObject.getTypeId().getValue());
                byteBuilder.append(dynamicGameObject.getPoint().x);
                byteBuilder.append(dynamicGameObject.getPoint().y);
                byteBuilder.append(dynamicGameObject.getPoint().z);
                byteBuilder.append(dynamicGameObject.getRotateY());
            }
            */

            byteBuilder.append((short) stationaryChangedSet.size());
            for(Stationary stationary : stationaryChangedSet) {
                byteBuilder.append(stationary.getId());
                byteBuilder.append(stationary.getType().getValue());
                byteBuilder.append(stationary.getPoint().x);
                byteBuilder.append(stationary.getPoint().y);
                byteBuilder.append(stationary.getPoint().z);
                byteBuilder.append(stationary.getRotateY());
                byteBuilder.append(stationary.getRemainSec());
            }

            byteBuilder.append((short)envPropChangedList.size());
            for (EnvProp envProp: envPropChangedList) {
                byteBuilder.append(envProp.getSyncAction().getValue());
                byteBuilder.append(envProp.typeId);
                byteBuilder.append(envProp.getId());
                byteBuilder.append(envProp.getPoint().x);
                byteBuilder.append(envProp.getPoint().y);
                byteBuilder.append(envProp.getPoint().z);
                byteBuilder.append(envProp.getRemainSecond());

                if (envProp.getSyncAction() == Enums.SyncAction.BORN)
                    envProp.setSyncAction(Enums.SyncAction.ALIVE);
            }

            byteBuilder.append((short) npcChangedSet.size());
            for (NPC npc: npcChangedSet) {
                if (npc != null) {
                    if (npc instanceof Merchant) {
                        byteBuilder.append(Constant.NPCType.MERCHANT);

                        byteBuilder.append(npc.getSyncAction().getValue());
                        byteBuilder.append(npc.getId());
                        byteBuilder.append(npc.getPoint().x);
                        byteBuilder.append(npc.getPoint().y);
                        byteBuilder.append(npc.getPoint().z);
                        byteBuilder.append(npc.getRotateY());

                        if (npc.getSyncAction() == Enums.SyncAction.BORN) {
                            Merchant merchant = (Merchant) npc;
                            byte[] nameBytes = merchant.getName().getBytes();
                            byteBuilder.append(nameBytes.length);
                            byteBuilder.append(nameBytes);
                            byteBuilder.append(merchant.getPropIds().length);
                            for (short propId: merchant.getPropIds()) {
                                byteBuilder.append(propId);
                            }
                            npc.setSyncAction(Enums.SyncAction.ALIVE);
                        }
                    } else if (npc instanceof Animal){
                        Animal animal = (Animal) npc;
                        byteBuilder.append(Constant.NPCType.ANIMAL);
                        byteBuilder.append(npc.getSyncAction().getValue());
                        byteBuilder.append(animal.getAnimalType().getValue());
                        byteBuilder.append(animal.getId());
                        byteBuilder.append(animal.getTargetUser().getId());
                        byteBuilder.append(animal.getPoint().x);
                        byteBuilder.append(animal.getPoint().y);
                        byteBuilder.append(animal.getPoint().z);
                        byteBuilder.append(animal.getRotateY());
                        if (npc.getSyncAction() == Enums.SyncAction.BORN) {
                            npc.setSyncAction(Enums.SyncAction.ALIVE);
                        }
                    }
                    npc.setMoved(false);
                }
            }

            byteBuilder.append((byte)positionChangedSet.size());
            for (int userId: positionChangedSet) {
                User user = userMap.get(userId);
                byteBuilder.append(userId);
                byteBuilder.append(user.moveState);
                byteBuilder.append(user.getPoint().x);
                byteBuilder.append(user.getPoint().y);
                byteBuilder.append(user.getPoint().z);
                byteBuilder.append(user.getRotateY());

                // TODO 临时放一下，需要移出去
                byteBuilder.append(user.getGroupId());
            }

            byteBuilder.append((byte)buffChangedSet.size());
            for (int userId: buffChangedSet) {
                byteBuilder.append(userId);
                byte count = 0;
                for (Buff buff: getUser(userId).getBuffList()) {
                    if (buff.getRemainSecond() > 0) {
                        count ++;
                    }
                }
                byteBuilder.append(count);
                for (Buff buff: getUser(userId).getBuffList()) {
                    if (buff.getRemainSecond() > 0) {
                        byteBuilder.append(buff.typeId);
                    }
                }
            }

            byteBuilder.append((short)actionList.size());
            for (Action action: actionList) {
                Action.PropAction propAction = (Action.PropAction) action;
                byteBuilder.append(propAction.propTypeId);
                byteBuilder.append(propAction.userId);
                byteBuilder.append(propAction.targetType);
                byteBuilder.append(propAction.targetId);
            }
        }

        /*
        if (dObjectChangedSet.size() > 0) {
            if (byteBuilder == null) {
                byteBuilder =  new ByteBuilder();
            }
            sign = (short) (sign | 16384);

        }

        if (stationaryChangedSet.size() > 0) {
            if (byteBuilder == null) {
                byteBuilder =  new ByteBuilder();
            }
            sign = (short) (sign | 4096);

        }
        */

        /*
        if (isStepChanged()) {
            if (byteBuilder == null) {
                byteBuilder =  new ByteBuilder();
            }
            sign = (short) (sign | 4096);
            byteBuilder.append(getStep());
        }
        if (envPropChangedList.size() != 0) {
            if (byteBuilder == null) {
                byteBuilder =  new ByteBuilder();
            }
            sign = (short) (sign | 2048);

        }

        if (npcChangedSet.size() > 0) {
            if (byteBuilder == null) {
                byteBuilder =  new ByteBuilder();
            }
            sign = (short) (sign | 1024);


        }

        if (positionChangedSet.size() != 0) {
            if (byteBuilder == null) {
                byteBuilder =  new ByteBuilder();
            }
            sign = (short) (sign | 2);

        }

        if (buffChangedSet.size() != 0) {
            if (byteBuilder == null) {
                byteBuilder =  new ByteBuilder();
            }
            sign = (short) (sign | 1);

        }

        */
        if (sign == 0) {
            // 如果没有需要同步的内容，返回null
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
    public void init(GameManage gameManage, GameMap gameMap, GameInfo gameInfo, List<? extends User> userList) {
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
        propList = getGamePropConfigMap().generatePropList();
    }

    /**
     * 同步游戏开始之后的一些数据，比如用户名, 游戏时间等，格式如下
     *
     * seqId(4)|messageType(1)|remainTime(4)|UserCount(1)|UserList[]|propCount(2)|list<PropPrice>|Map
     *
     * User: userId(4)|nameLength(1)|name
     * PropPrice: id(2)|price(4)
     * //Map: boundX(4)|boundY(4)|mapByteCount|List<byte>
     * Building: id(4)|type(1)|positionX(4)|positionY(4)|orientation(1)
     */
    public void generatePrepareMessage() {
        ByteBuilder bb = new ByteBuilder();
        bb.append(0);
        bb.append(Constant.MessageType.PREPARE);
        byte[] metagameCodeBytes = gameInfo.getMetagameCode().getBytes();
        bb.append(metagameCodeBytes.length);
        bb.append(metagameCodeBytes);
        bb.append(gameInfo.getDuration());
        bb.append((byte)userMap.size());
        for (User user: userMap.values()) {
            bb.append(user.getId());
            bb.append(user.getRoleType());
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

        /*
        bb.append(gameMap.boundX);
        bb.append(gameMap.boundY);

        byte[] mapBytes = gameMap.getMapBytes();
        bb.append(mapBytes.length);
        bb.append(mapBytes);
        */

        /*
        bb.append(stationaryMap.size());
        for (Stationary interactiveObject: stationaryMap.values()) {
            bb.append(interactiveObject.getId());
            bb.append(interactiveObject.getType());
            bb.append(interactiveObject.getPoint().x);
            bb.append(interactiveObject.getPoint().y);
            bb.append(interactiveObject.getPoint().z);
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
        user.setRotateY(message.rotationY);
        user.setPoint(new Point(message.positionX, message.positionY, message.positionZ));
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
        NPC npc = npcMap.get(message.objectId);
        if (npc == null) return;

        // TODO check if could move
        npc.setRotateY(message.rotationY);
        npc.setPoint(new Point(message.positionX, message.positionY, message.positionZ));
        //user.setMoveState(message.moveState);

        npcChangedSet.add(npc);
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
        if (user.getState() == UserState.GAME_OVER) return;

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
            if(PropConfig.getProp(propTypeId).use(message, user, toUser, this)) {
                actionList.add(new Action.PropAction(propTypeId, user.getId(), message.getType(), message.getToUserId()));
            }
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
     * 处理交互消息
     */
    public void executeInteractionMessage(InteractionMessage message) {
        User user = getUser(message.getUserId(), true);
        if (user.getState() == UserState.GAME_OVER) return;
        // for override

        if (message.interactiveObjectType == Enums.GameObjectType.INTERACT) {
            Stationary stationary = stationaryMap.get(message.interactiveObjectId);
            if (stationary == null) return;

            // TODO 需要判断玩家是不是在这个对象附近
            stationary.interact(this, user);
        } else if (message.interactiveObjectType ==  Enums.GameObjectType.PROP) {
            EnvProp prop = propMap.get(message.interactiveObjectId);
            if (prop != null && removeGameObject(prop)) {
                user.increaseProp(prop.typeId, (short)1);
            }
        }

        /*
        BlockGroup blockGroup = gameMap.blockGroupMap.get(message.interactiveObjectId);
        if (blockGroup == null) return;

        BlockGroup blockGroupAround = null;
        List<Block> blocksAround = gameMap.getBlocksInDistance(user.getPoint(), 1);
        for (Block block:  blocksAround) {
            if (block.blockGroupId == message.interactiveObjectId) {
                blockGroupAround = gameMap.blockGroupMap.get(block.blockGroupId);
            }
        }

        if (blockGroupAround == null) return;
        TerrainTransferConfigs.TerrainTransferConfig config =
                TerrainTransferConfigs.getConfig(blockGroupAround.getTerrainType());

        if (config.resourceType == Enums.ResourceType.MONEY) {
            if (user.getMoney() < config.count) return;
            user.modifyMoney( - config.count);
        } else if (config.resourceType == Enums.ResourceType.PROP) {
            short propId = (short)config.id;
            if (user.getProp(propId) < config.count) return;
            user.reduceProp(propId, (short)config.count);
        }
        blockGroupAround.setTerrainType(config.target);
        */
    }

    /**
     * 处理设置目标的消息
     */
    public void executeTargetMessage(TargetMessage message) {
        User user = getUser(message.getUserId(), true);
        user.setTarget(message.getType(), message.getId(), message.getPoint());
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
                if (user.getPoint().distance(chasingInfo.chasingUser.getPoint()) < 10) {
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
        Point position = fromUser.getPoint();
        for (User user: userMap.values()) {
            if (user.getId() == fromUser.getId()) continue;
            if ((user.getPoint().x - position.x) < 10 && (user.getPoint().y - position.y < 10)) {
                if (user.getPoint().distance(position) < 10) {
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
     * 定义游戏中道具的生成规则, 比如道具的刷新频率，生存周期
     * @return
     */
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
                        gameInfo.getMetagameCode(), prop.position.point,
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
        int propInStock = getGamePropConfigMap().getPropRemained((int)getGameTime() / 1000);
        int count = propList.size() - propInStock;
        if (count <= 0) return;

        while (count > 0) {
            EnvProp envProp = new EnvProp(this);

            envProp.typeId = propList.removeFirst();
            envProp.setId(nextPropSeqId ++);

            int randomBlockId = gameMap.getRandomMainRoadBlockId();
            int x = gameMap.getX(randomBlockId);
            int y = gameMap.getY(randomBlockId);
            envProp.setPoint(new Point(x * 1000, 100, y * 1000));
            envProp.createTime = System.currentTimeMillis();
            envProp.vanishTime = envProp.createTime +
                    getGamePropConfigMap().getPropConfig(envProp.typeId).duration * 1000;
            propMap.put(envProp.getId(), envProp);
            getEnvPropChangedList().add(envProp);
            log.info("created prop: {}:{}-{}:{}:{}", gameInfo.getId(), envProp.getId(),
                    envProp.getPoint().x, envProp.getPoint().y, envProp.getPoint().z);

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
                int randomBlockId = gameMap.getRandomMainRoadBlockId();
                int x = gameMap.getX(randomBlockId);
                int y = gameMap.getY(randomBlockId);
                prop.setPoint(new Point(x * 1000, 1100, y * 1000));
                prop.createTime = System.currentTimeMillis();
                prop.vanishTime = prop.createTime +
                        getGamePropConfigMap().getPropConfig(prop.typeId).duration * 1000;

                getEnvPropChangedList().add(prop);
                //log.info("recreated prop: {}:{}:{}", gameInfo.getId(), prop.id, getEnvPropChangedList());
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
            if (prop.getId() == id) {
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
        // TODO 有可能默认的price也不存在，就会抛错
        int customPropPrice = getCustomPropPrice(propTypeId);
        if (customPropPrice == -1) {
            return propPriceMap.get(propTypeId);
        }
        return customPropPrice;
    }

    /**
     * 自定义道具价格
     */
    protected abstract int getCustomPropPrice(short propTypeId);

    /**
     * 自定义游戏可以购买的道具id
     */
    protected abstract short[] getStorePropIds();

    /********************************
     * NPC 相关
     ********************************/

    protected int getNextNpcId() {
        return npcSequenceId ++;
    }

    protected int getNextDObjectId() {
        return dynamicObjectSeqId ++;
    }

    protected int getNextInterObjectId() {
        return interObjectSeqId ++;
    }

    private void initNPC() {
        List<NPC> npcList = generateNPC();
        if (npcList == null) return;
        for(NPC npc : generateNPC()) {
            addGameObject(npc);
        }
    }

    /**
     * 生成NPC
     */
    protected abstract List<NPC> generateNPC();

    /**
     * 移动NPC
     */
    /*
    protected void moveNPC() {
        for (NPCOld npcOld : getMoveableNPCMap().values()) {
            // TODO
            /*
            if (npcOld.movable && npcOld.isPathEmpty()) {
                npcOld.setPath(navimeshGroup.getPath(
                        gameInfo.getMetagameCode(), npcOld.position.point,
                        navimeshGroup.getRandomPositionPoint(gameInfo.getMetagameCode())));
            }
            npcOld.move();
            */
    /*
            if (npcOld.isPositionChanged()) {
                npcChangedList.add(npcOld);
            }
        }
    }
    */

    /**
     * 根据id获取NPC
     */
    public Stationary getStationary(int id) {
        return stationaryMap.get(id);
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

    public Random getRandom() {
        return random;
    }

    public List<EnvProp> getEnvPropChangedList() {
        return envPropChangedList;
    }

    /*
    public List<EnvProp> getPropInSceneList() {
        return propInSceneList;
    }
    */

    public byte getStep() {
        return step;
    }

    public void setStep(byte step) {
        isStepChanged = true;
        this.step = step;
    }

    public GameMap getGameMap() {
        return gameMap;
    }

    public void setGameMap(GameMap gameMap) {
        this.gameMap = gameMap;
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

    public void setNavimeshGroup(NavMeshGroup navimeshGroup) {
        //this.navimeshGroup = navimeshGroup;
    }

    protected List<Point> getPath(Point startPoint, Point endPoint) {
        if (startPoint == null || endPoint == null) return null;
        if (startPoint.equals(endPoint)) return null;

        int startBlockId =  gameMap.getBlockId(startPoint.x / 1000, startPoint.z / 1000);
        int endBlockId = gameMap.getBlockId(endPoint.x / 1000, endPoint.z / 1000);

        boolean isInStartBlock = true;
        if (!gameMap.isArtery(startBlockId)) {
            startBlockId = gameMap.getNearestArteryBlockId(startBlockId, 8);
            isInStartBlock = false;
        }
        boolean isInEndBlock = true;
        if (!gameMap.isArtery(endBlockId)) {
            endBlockId = gameMap.getNearestArteryBlockId(endBlockId, 8);
            isInEndBlock = false;
        }

        if (startBlockId == -1 || endBlockId == -1) return null;
        LinkedList<Point> path = getPath(startBlockId, endBlockId);

        if (isInStartBlock) path.removeFirst();
        if (!isInEndBlock) path.addLast(endPoint);
        return path;
    }

    protected LinkedList<Point> getPath(int startBlockId, int endBlockId) {
        if (startBlockId == endBlockId) return null;
        if (!gameMap.isArtery(startBlockId) || !gameMap.isArtery(endBlockId)) return null;

        Map<Integer, int[]> closeMap = new HashMap<>();
        Map<Integer, int[]> openMap = new HashMap<>();

        int h = getPathH(startBlockId, endBlockId);
        openMap.put(startBlockId, new int[]{-1, 0, h, h});

        boolean reachEnd = false;
        while(!reachEnd && openMap.size() > 0) {
            int smallestF = Integer.MAX_VALUE;
            int smallestFBlockId = -1;
            for (int blockId : openMap.keySet()) {
                if (openMap.get(blockId)[3] < smallestF) {
                    smallestFBlockId = blockId;
                    smallestF = openMap.get(blockId)[3];
                }
            }

            closeMap.put(smallestFBlockId, openMap.get(smallestFBlockId));
            openMap.remove(smallestFBlockId);

            int x = gameMap.getX(smallestFBlockId);
            int y = gameMap.getY(smallestFBlockId);

            for (int i = -1; i <= 1; i ++) {
                for (int j = -1; j <= 1; j ++) {
                    if (i == 0 && j == 0) continue;
                    int aroundBlockId = gameMap.getBlockId(x + i, y + j);

                    if (gameMap.isArtery(aroundBlockId)) {
                        int aroundBlockH = getPathH(aroundBlockId, endBlockId);
                        int aroundBlockG;
                        if (i == 0 || j == 0) aroundBlockG = closeMap.get(smallestFBlockId)[1] + 10;
                        else aroundBlockG = closeMap.get(smallestFBlockId)[1] + 10;

                        if (closeMap.containsKey(aroundBlockId)) {
                                if (closeMap.get(aroundBlockId)[1] > aroundBlockG) {
                                    closeMap.put(aroundBlockId, new int[]{
                                            smallestFBlockId, aroundBlockG, aroundBlockH, aroundBlockG + aroundBlockH});
                                }
                        } else {
                            if (aroundBlockId == endBlockId) {
                                reachEnd = true;
                                closeMap.put(aroundBlockId, new int[]{
                                        smallestFBlockId, aroundBlockG, aroundBlockH, aroundBlockG + aroundBlockH});

                            } else {
                                openMap.put(aroundBlockId, new int[]{
                                        smallestFBlockId, aroundBlockG, aroundBlockH, aroundBlockG + aroundBlockH});
                            }
                        }
                    }
                }
            }
        }

        if (!reachEnd) return null;

        /*
        List<Integer> pathList = new LinkedList<>();
        int currentBlockId = endBlockId;
        while (closeMap.containsKey(currentBlockId)) {
            pathList.add(currentBlockId);
            currentBlockId = closeMap.get(currentBlockId)[0];
        }
        Collections.reverse(pathList);
        return pathList;
        */

        LinkedList<Point> pathList = new LinkedList<>();
        int currentBlockId = endBlockId;
        while (closeMap.containsKey(currentBlockId)) {
            pathList.add(new Point(gameMap.getX(currentBlockId) * 1000 + 500,
                    0, gameMap.getY(currentBlockId) * 1000 + 500));
            currentBlockId = closeMap.get(currentBlockId)[0];
        }
        return pathList;
    }

    private int getPathH(int blockIdA, int blockIdB) {
        return (Math.abs(gameMap.getX(blockIdA) - gameMap.getX(blockIdB)) +
                Math.abs(gameMap.getY(blockIdA) - gameMap.getY(blockIdB))) * 10;
    }

    public static void main(String ... args) {
        Marathon game = new Marathon();
        MarathonGameMapCreator marathonGameMapCreator = new MarathonGameMapCreator();
        GameMap gameMap = marathonGameMapCreator.generate(20, 20, 49);
        game.setGameMap(gameMap);

        //int start = gameMap.getRandomMainRoadBlockId();
        //int end =  gameMap.getRandomMainRoadBlockId();

        int start = gameMap.mainRoad.blockList.get(20).blockId;
        int end = gameMap.mainRoad.blockList.get(50).blockId;

        long currentTimestamp = java.lang.System.currentTimeMillis();
        List<Point> path = game.getPath(start, end);
        long cost = System.currentTimeMillis() - currentTimestamp;
        System.out.println("cost:" + cost);

        System.out.println("start: [" + gameMap.getX(start) + ", " + gameMap.getY(start) + "]");
        System.out.println("end: [" + gameMap.getX(end) + ", " + gameMap.getY(end) + "]");
        System.out.println("length: " + path.size());

        for (Point point : path) {
            System.out.println("[" + point.x + ", " + point.z + "]");
        }
    }

}
