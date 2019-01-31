package com.prosper.chasing.game.base;

/**
 * Created by deacon on 2018/12/30.
 */
public class UsePropAction {

    private GameObject target;

    private long startTime;

    private Point startPosition;

    public  UsePropAction(short propTypeId, GameObject target, long startTime, Point startPosition) {
        this.target = target;
        this.startPosition = startPosition;
        this.startTime = startTime;
    }
}
