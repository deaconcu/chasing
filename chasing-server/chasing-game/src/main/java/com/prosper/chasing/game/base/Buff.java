package com.prosper.chasing.game.base;

import com.prosper.chasing.game.util.Enums.*;

import java.util.LinkedList;
import java.util.List;

/**
 * 用户buff
 * buff有三种类型：
 * 1 普通buff，一般是使用道具产生的buff
 * 2 地形buff，当前所在的道路产生的buff
 * 3 道具光环，持有道具产生的buff
 * Created by deacon on 2018/1/1.
 */
public class Buff {

    /**
     * buff 标识id
     */
    int id;

    /**
     * buff 类型id
     */
    BuffType type;

    /**
     * buff的开始时间
     */
    long startSecond; // 起始时间

    /**
     * buff的结束时间
     */
    short last;      // 持续时间

    /**
     * 一些附加的信息
     */
    Object[] valueList;

    public Buff(int id, BuffType type, short last, Object... values) {
        this.id = id;
        this.type = type;
        this.startSecond = System.currentTimeMillis();
        this.last = last;
        valueList = values;
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

    public boolean isValid() {
        return getRemainSecond() > 0;
    }

    public void refresh(short last) {
        this.startSecond = System.currentTimeMillis();
        this.last = last;
    }

    /**
     * 特殊地形buff
     */
    public static class SpcSectionBuff extends Buff {

        int groupId;

        public SpcSectionBuff(int id, BuffType type, int groupId) {
            super(id, type, (short)-1);
            this.groupId = groupId;
        }
    }

    /**
     * 道具buff
     */
    public static class PropBuff extends Buff {

        PropType propType;

        public PropBuff(int id, BuffType type, PropType propType) {
            super(id, type, (short)-1);
            this.propType = propType;
        }
    }
}
