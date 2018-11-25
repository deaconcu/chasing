package com.prosper.chasing.game.base;

import java.util.Map;

/**
 * 动物有主动追逐玩家的特性
 * Created by deacon on 2018/9/7.
 */
public class Animal extends NPC {

    private int blockGroupId;

    // 物体当前目标
    private User targetUser;

    public Animal(int id, Point point, int rotateY, int blockGroupId) {
        super(id, point, rotateY);
        this.blockGroupId = blockGroupId;
    }

    @Override
    public void logic(Map<Integer, User> playerList) {
        if (targetUser.getPoint().minDistanceOfAxis(getPoint()) <= 1000 &&
                targetUser.getPoint().distance(getPoint()) <= 100) {
            targetUser.setLife((short)0);

        }
    }

    public User getTargetUser() {
        return targetUser;
    }

    public void setTargetUser(User targetUser) {
        this.targetUser = targetUser;
    }
}
