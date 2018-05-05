package com.prosper.chasing.game.games;

import com.prosper.chasing.game.base.*;
import com.prosper.chasing.game.navmesh.Point;
import com.prosper.chasing.game.util.ByteBuilder;
import com.prosper.chasing.game.util.Constant;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

@MetaGameAnno("marathon")
public class Marathon extends Game {

    private static final int POSITION_X_LIMIT = 10;
    private static final int POSITION_Z_LIMIT = 10;

    protected static GamePropConfigMap gamePropConfigMap;

    static {
        gamePropConfigMap = new GamePropConfigMap(10)
                .add(PropConfig.SPEED_UP_LEVEL_1, (short)100, (short)60, false)
                .add(PropConfig.SPEED_UP_LEVEL_2, (short)100, (short)60, false)
                .add(PropConfig.SPEED_DOWN_LEVEL_1, (short)100, (short)60, false)
                .add(PropConfig.SPEED_DOWN_LEVEL_2, (short)100, (short)60, false)
                .add(PropConfig.BLOOD_PILL, (short)100, (short)60, false)
                .add(PropConfig.BLOOD_BAG, (short)100, (short)60, false)
                .add(PropConfig.FLASH_LEVEL_1, (short)100, (short)60, false)
                .add(PropConfig.FLASH_LEVEL_2, (short)100, (short)60, false)
                .add(PropConfig.MONEY, (short)100, (short)60, true)
                .add(PropConfig.GIFT_BOX, (short)100, (short)60, true);
    }

    // 出发地
    private Point birthArea = new Point(0, 0, 0);

    public static class MarathonUser extends User {

        // 完成时间
        private int finishTime;

        public MarathonUser() {}

        public MarathonUser(User user) {
            setId(user.getId());
            setPropMap(user.getPropMap());
            setGame(user.getGame());
            setState(user.getState());
        }
    }

    @Override
    public Class<? extends User> getUserClass() {
        return MarathonUser.class;
    }

    @Override
    public void logic() {
        super.logic();
    }

    @Override
    public ByteBuilder generateResultMessage(User user) {
        int rank = 1;
        for (User gameUser: getUserMap().values()) {
            if (gameUser.gameOverTime < user.gameOverTime) {
                rank ++;
            }
        }

        ByteBuilder bb = new ByteBuilder();
        bb.append(Constant.MessageType.RESULT);
        bb.append(rank);
        bb.append(0);

        return bb;
    }

    @Override
    public GamePropConfigMap getGamePropConfigMap() {
        return gamePropConfigMap;
    }

    @Override
    protected int getCustomPropPrice(short propTypeId) {
        if (propTypeId == PropConfig.MARK) return 10;
        else return -1;
    }

    @Override
    protected short[] getStorePropIds() {
        return new short[]{1,2,3,4,5,6,7,8,9,10};
    }

    @Override
    protected List<NPC> generateNPC() {
        List<NPC> npcList = new LinkedList<>();
        // TODO
        /*
        npcList.add(new Merchant(
                this, 1, (short)1, "范蠡", false, new short[]{1,2,3,4,5},
                new Position((byte)0, navimeshGroup.getRandomPositionPoint("king"), 0)));
                */
        return npcList;
    }

}
