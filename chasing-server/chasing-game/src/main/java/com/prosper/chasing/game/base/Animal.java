package com.prosper.chasing.game.base;

import com.prosper.chasing.game.util.Enums;

import java.util.Map;

/**
 * 动物有主动追逐玩家的特性
 * Created by deacon on 2018/9/7.
 */
public class Animal extends NPC {

    private int blockGroupId;

    private Enums.AnimalType animalType;

    /**
     * 目标用户id
     */
    private User targetUser;

    public Animal(int id, Enums.AnimalType  animalType, Point point, int rotateY, int blockGroupId, User targetUser) {
        super(id, point, rotateY);
        this.animalType = animalType;
        this.blockGroupId = blockGroupId;
        this.targetUser = targetUser;
    }

    /**
     */
    @Override
    public void logic(Game game) {
        if (!getTargetUser().hasBuff(BuffConfig.ANIMAL)) {
            setAlive(false);
        }

        if (getTargetUser().getPoint().minDistanceOfAxis(getPoint()) <= 1000 &&
                getTargetUser().getPoint().distance(getPoint()) <= 100) {
            //getTargetUser().setLife((short)0);
        }

        if (getTargetUser().isCrossZone()) {
            setPath(game.getPath(getPoint(), targetUser.getPoint()));
        }

        move();
        if (isMoved()) {
            game.npcChangedSet.add(this);
        }
    }

    public User getTargetUser() {
        return targetUser;
    }

    public void setTargetUser(User targetUser) {
        this.targetUser = targetUser;
    }

    public Enums.AnimalType getAnimalType() {
        return animalType;
    }

}
