package com.prosper.chasing.game.base;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by deacon on 2018/1/1.
 */
public class Buff {

    byte id;
    int startSecond; // 起始时间
    short last;      // 持续时间
    List<Object> valueList;

    public Buff(byte id, short last) {
        this.id = id;
        this.startSecond = (int)(System.currentTimeMillis() / 1000);
        this.last = last;
        this.valueList = new LinkedList<>();
    }

    public Buff(BuffConfig config) {
        this(config.id, config.last);
    }

    public Buff(BuffConfig config, Object... values) {
        this(config.id, config.last);
        if (values != null) {
            for (Object o: values) {
                valueList.add(o);
            }
        }
    }

    public int getRemainSecond() {
        return (int) (System.currentTimeMillis() / 1000 - (startSecond + last));
    }
}
