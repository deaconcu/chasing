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

    public Buff(int id, BuffConfig config) {
        this(id, config.id, config.last);
    }

    public Buff(int id, BuffConfig config, Object... values) {
        this(id, config, 0,  values);
    }

    public Buff(int id, BuffConfig config, int groupId, Object... values) {
        this(id, config.id, config.last);
        this.groupId = groupId;
        if (values != null) {
            for (Object o: values) {
                valueList.add(o);
            }
        }
    }

    public int getRemainSecond() {
        if (last < 0) return Integer.MAX_VALUE;
        return (int)Math.ceil((double) (System.currentTimeMillis() - startSecond - last * 1000) / 1000);
    }
}
