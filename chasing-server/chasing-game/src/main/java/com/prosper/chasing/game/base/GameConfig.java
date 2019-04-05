package com.prosper.chasing.game.base;

import com.prosper.chasing.game.util.Enums.*;

import java.util.Map;

/**
 * Created by deacon on 2019/3/25.
 */
public class GameConfig {

    // 排名第一字段名称
    private String firstRankName;

    // 排名第二字段名称
    private String secondRankName;

    // 排名第一字段类型
    private RankValueType firstRankValueType;

    // 排名第二字段类型
    private RankValueType secondRankValueType;

    // 自定义道具价格
    private Map<PropType, Integer> propPriceMap;

    // 商店出售的道具
    private PropType[] storePropTypes;

    public GameConfig(String firstRankName, RankValueType firstRankValueType,
                      String secondRankName, RankValueType secondRankValueType,
                      Map<PropType, Integer> propPriceMap, PropType[] storePropTypes) {
        this.firstRankName = firstRankName;
        this.secondRankName = secondRankName;
        this.firstRankValueType =  firstRankValueType;
        this.secondRankValueType = secondRankValueType;
        this.propPriceMap = propPriceMap;
        this.storePropTypes = storePropTypes;
    }

    public String getFirstRankName() {
        return firstRankName;
    }

    public String getSecondRankName() {
        return secondRankName;
    }

    public RankValueType getFirstRankValueType() {
        return firstRankValueType;
    }

    public RankValueType getSecondRankValueType() {
        return secondRankValueType;
    }

    public Map<PropType, Integer> getPropPriceMap() {
        return propPriceMap;
    }

    public PropType[] getStorePropTypes() {
        return storePropTypes;
    }
}
