package com.prosper.chasing.game.games;

import com.prosper.chasing.game.base.*;

import com.prosper.chasing.game.message.PropMessage;
import com.prosper.chasing.game.message.TaskMessage;
import com.prosper.chasing.game.service.PropService;

import java.util.*;

@MetaGameAnno("Hunter")
public class Hunter extends Game {

    private static final short NPC_PROP_MERCHANT_ID = 1001; // 卖道具的商人
    private static final short NPC_HUNTER_ID = 1002;  // 猎人，卖追踪器，陷阱
    private static final short NPC_CHEST_MERCHANT_ID = 1003; // 宝箱商人，提示宝箱位置

    private static final short NPC_SHEEP_ID = 2001; // 绵羊
    private static final short NPC_DOG_ID = 2002; // 狗
    private static final short NPC_BULL_ID = 2003; // 野牛
    private static final short NPC_WOLF_ID = 2004; // 狼
    private static final short NPC_TIGER_ID = 2005; // 老虎

    private static final short PROP_SHEEP_ID = 3001;
    private static final short PROP_DOG_ID = 3002;
    private static final short PROP_BULL_ID = 3003;
    private static final short PROP_WOLF_ID = 3004;
    private static final short PROP_TIGER_ID = 3005;

    private static final Map<Short, Integer> PROP_VALUE_MAP = new HashMap<>();
    static {
        PROP_VALUE_MAP.put(PROP_SHEEP_ID, 100);
        PROP_VALUE_MAP.put(PROP_DOG_ID, 200);
        PROP_VALUE_MAP.put(PROP_BULL_ID, 300);
        PROP_VALUE_MAP.put(PROP_WOLF_ID, 400);
        PROP_VALUE_MAP.put(PROP_TIGER_ID, 500);
    }

    private static final short TASK_HUNT = 1;

    private static final int POSITION_X_LIMIT = 10;
    private static final int POSITION_Z_LIMIT = 10;

    private static Map<Short, Short> npcRelatedPropMap = new HashMap<>();

    static {
        npcRelatedPropMap.put(NPC_SHEEP_ID, PROP_SHEEP_ID);
        npcRelatedPropMap.put(NPC_DOG_ID, PROP_DOG_ID);
        npcRelatedPropMap.put(NPC_BULL_ID, PROP_BULL_ID);
        npcRelatedPropMap.put(NPC_WOLF_ID, PROP_WOLF_ID);
        npcRelatedPropMap.put(NPC_TIGER_ID, PROP_TIGER_ID);
    }

    public static class HunterUser extends User {
        public HunterUser(User user) {
            setId(user.getId());
            setName(user.getName());
            setPropMap(user.getPropMap());
            setGame(user.getGame());
            setState(user.getState());
        }

        @Override
        public void catchUp(NPC npc) {
            getGame().getMoveableNPCMap().remove(npc.getId());
            Short propId = getGame().getRelatedPropByNPC(npc.getId());
            if (propId != null) {
                setProp(propId, (byte)(getProp(propId) + 1));
            }
        }
    }





    public Hunter() {
        super();
        addMovableNPCConfig(new NPC.NPCConfig(NPC_SHEEP_ID, 20, 2)); // sheep
        addMovableNPCConfig(new NPC.NPCConfig(NPC_DOG_ID, 15, 5)); // dog
        addMovableNPCConfig(new NPC.NPCConfig(NPC_BULL_ID, 10, 10)); // bull
        addMovableNPCConfig(new NPC.NPCConfig(NPC_WOLF_ID, 5, 15)); // wolf
        addMovableNPCConfig(new NPC.NPCConfig(NPC_TIGER_ID, 2, 20)); // tiger

        // 生成npc商人
        getStaticNPCMap().put(1, new NPC(1, NPC_PROP_MERCHANT_ID,
                new Position((byte)0, new PositionPoint(0, 0, 1), 0), 0));
        getStaticNPCMap().put(1, new NPC(1, NPC_HUNTER_ID,
                new Position((byte)0, new PositionPoint(0, 0, 2), 0), 0));
        getStaticNPCMap().put(1, new NPC(1, NPC_CHEST_MERCHANT_ID,
                new Position((byte)0, new PositionPoint(0, 0, 3), 0), 0));
    }

    @Override
    public Class<? extends User> getUserClass() {
        return HunterUser.class;
    }

    @Override
    public Short getRelatedPropByNPC(short npcId) {
        return npcRelatedPropMap.get(npcId);
    }

    @Override
    public void loadUser(List<User> userList) {
        for (User user: userList) {
            HunterUser gemsUser = new HunterUser(user);
            int positionX = getRandom().nextInt(POSITION_X_LIMIT * 2 + 1) - POSITION_X_LIMIT;
           int positionZ = getRandom().nextInt(POSITION_Z_LIMIT * 2 + 1) - POSITION_Z_LIMIT;
            Position position = new Position((byte)1, new PositionPoint(positionX, 0, positionZ), 0);
            gemsUser.setPosition(position);
            gemsUser.setInitPosition(position);
            getUserMap().put(gemsUser.getId(), gemsUser);
        }
    }

    @Override
    public void logic() {
        removeInvalidProp();
        fetchProp();
        generateProp();

        generateMovableNPC();
        moveNPC();
    }

    @Override
    public List<Result> getResultList() {
        List<Result> resultList = new LinkedList<>();
        for(User user: getUserMap().values()) {
            resultList.add(new Result(user, user.getProp(PropService.GEN), 0));
        }
        Collections.sort(resultList);
        return resultList;
    }

    @Override
    public void executeTaskMessage(TaskMessage message) {
        User user = getUser(message.getUserId());
        if (message.taskId == TASK_HUNT) {
            int amount = 0;
            for (Map.Entry<Short, Integer> entry: PROP_VALUE_MAP.entrySet()) {
                short propId = entry.getKey();
                int value = entry.getValue();

                amount += user.getProp(propId) * value;
                user.setProp(propId, (byte)0);
            }
            user.addMoney(amount);
        }
    }
}
