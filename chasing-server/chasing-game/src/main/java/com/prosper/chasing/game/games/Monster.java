package com.prosper.chasing.game.games;

import com.prosper.chasing.game.base.Game;
import com.prosper.chasing.game.base.MetaGameAnno;
import com.prosper.chasing.game.base.Position;
import com.prosper.chasing.game.base.User;
import com.prosper.chasing.game.message.PropMessage;
import com.prosper.chasing.game.navmesh.Point;
import com.prosper.chasing.game.service.PropService;
import com.prosper.chasing.game.util.ByteBuilder;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.prosper.chasing.game.games.Monster.MonsterUser.TYPE_MONSTER;

@MetaGameAnno("monster")
public class Monster extends Game {

    // 找变成妖怪的道具时间，超过这个时间就会随机找一个人成为妖怪
    private static final int frozenTime = 300;
    private static final int fragmentTotal = 10;

    private static final byte STEP_CHOOSE_MONSTER = 1;
    private static final byte STEP_FIND_WEAPON = 2;
    private static final byte STEP_KILL_MONSTER = 3;

    // 已找到的碎片数
    private int fragmentCount = 0;

    private MonsterUser chosenOne;

    public static class MonsterUser extends User {
        public static final byte TYPE_CIVILIAN = 0; // 平民
        public static final byte TYPE_MONSTER = 1;  // 妖怪

        // 坚持的时间
        public int endTime;

        public byte type = TYPE_CIVILIAN;

        public short fragmentCount = 0;

        public MonsterUser(User user) {
            setId(user.getId());
            setPropMap(user.getPropMap());
            setGame(user.getGame());
            setState(user.getState());
        }

        /**
         * return type(1)|fragmentCount(2)
         */
        public byte[] getCustomPropertyBytes() {
            ByteBuilder byteBuilder = new ByteBuilder();
            byteBuilder.append(type);
            byteBuilder.append(fragmentCount);
            return byteBuilder.getBytes();
        }

        public byte getType() {
            return type;
        }

        public void setType(byte type) {
            this.isCustomPropertyChanged = true;
            this.type = type;
        }

        public short getFragmentCount() {
            return fragmentCount;
        }

        public void setFragmentCount(short fragmentCount) {
            this.isCustomPropertyChanged = true;
            this.fragmentCount = fragmentCount;
        }
    }

    public static class MonsterPropExecutor extends PropService {

        public static final byte HUNTER_MONSTER_PILL = 120;

        static {
            typeMap.put(HUNTER_MONSTER_PILL, new byte[]{PropMessage.TYPE_NONE, PropMessage.TYPE_USER});
        }

        @Override
        public void doUse(byte propId, PropMessage message, User user, User toUser,
                          Map<Integer, ? extends User> userMap, List<Game.EnvProp> envPropList) {
            super.doUse(propId, message, user, toUser, userMap, envPropList);
            if (propId == HUNTER_MONSTER_PILL) {
                ((MonsterUser)user).setType(TYPE_MONSTER);
                user.getGame().setStep(STEP_FIND_WEAPON);
            }
        }
    }

    @Override
    public void loadUser(List<User> userList) {
        for (User user: userList) {
            MonsterUser monsterUser = new MonsterUser(user);
            Position position = new Position(
                    (byte)1, new Point(0, 0, 0), 0);
            monsterUser.setPosition(position);
            monsterUser.setInitPosition(position);
            getUserMap().put(monsterUser.getId(), monsterUser);
        }
    }

    @Override
    public void logic() {
        removeInvalidProp();
        fetchProp();

        // 超过设置时间，随机指定一个人为monster
        if ((System.currentTimeMillis() / 1000 - startTime) > frozenTime && chosenOne == null) {
            MonsterUser user = (MonsterUser)getUserMap().values().toArray()[getRandom().nextInt(getUserMap().size())];
            user.type = TYPE_MONSTER;
            chosenOne = user;
            setStep(STEP_FIND_WEAPON);
        }

        // 判断是否生命已结束
        if (chosenOne != null) {
            for (User user: getUserMap().values()) {
                MonsterUser monsterUser = (MonsterUser)user;
                if (monsterUser.type == TYPE_MONSTER) {
                    continue;
                }
                boolean isNear = isNear(
                        chosenOne.getPosition().point,
                        monsterUser.getPosition().point, FETCH_DISTANCE);
                if (isNear) {
                    monsterUser.setLife((short)0);
                    monsterUser.endTime = (int) System.currentTimeMillis() / 1000;
                }
            }
        }

        if (fragmentCount >= fragmentTotal) {
            chosenOne.setLife((short)0);
        }

        generateProp();
    }

    @Override
    public List<Result> getResultList() {
        List<Result> resultList = new LinkedList<>();
        for(User user: getUserMap().values()) {
            MonsterUser mUser = (MonsterUser) user;
            if (mUser.type == 1) {
                continue;
            }
            resultList.add(new Result(mUser, mUser.fragmentCount, 0));
        }
        Collections.sort(resultList);
        if (chosenOne.getLife() == TYPE_MONSTER) {
            resultList.add(0, new Result(chosenOne, chosenOne.fragmentCount, 0));
        } else {
            resultList.add(new Result(chosenOne, chosenOne.fragmentCount, 0));
        }
        return resultList;
    }
}
