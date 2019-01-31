package com.prosper.chasing.game.base;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by deacon on 2018/1/1.
 */
public class Buff {

    int id;
    byte typeId;
    long startSecond; // 起始时间
    short last;      // 持续时间
    int groupId;
    List<Object> valueList;

    public Buff(int id, byte typeId, short last) {
        this.id = id;
        this.typeId = typeId;
        this.startSecond = System.currentTimeMillis();
        this.last = last;
        this.valueList = new LinkedList<>();
        this.groupId = 0;
    }

    public Buff(int id, byte typeId, short last, int groupId, Object... values) {
        this(id, typeId, last);
        this.groupId = groupId;
        if (values != null) {
            for (Object o: values) {
                valueList.add(o);
            }
        }
    }

    public Buff(int id, byte typeId, short last, Object... values) {
        this(id, typeId, last, 0, values);
    }

    /**
     * 使buff失效
     */
    public void expire() {
        this.last = 0;
    }

    public int getRemainSecond() {
        if (last < 0) return Integer.MAX_VALUE;
        int remainSecond = (int)Math.ceil((double) (startSecond + last * 1000 - System.currentTimeMillis()) / 1000);
        return remainSecond;
    }

    public void refresh(short last) {
        this.startSecond = System.currentTimeMillis();
        this.last = last;
    }
}
