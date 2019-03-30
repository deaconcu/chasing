package com.prosper.chasing.game.base;

import com.prosper.chasing.game.util.ByteBuilder;
import com.prosper.chasing.game.util.Enums.*;

import java.util.*;

/**
 * 玩家排名信息
 * Created by deacon on 2019/3/25.
 */
public class Rank {

    private Map<Integer, RankItem> rankMap;
    private RankValueType firstRankType;
    private RankValueType secondRankType;
    private Set<Integer> syncUserIdSet;

    public class RankItem {
        // 用户id
        private int userId;

        // 排行榜int类型的一级字段
        private int firstIntValue;

        // 排行榜的int类型的二级字段
        private int secondIntValue;

        // 排行榜的金币奖励
        private int reward;

        public RankItem(int userId) {
            this.userId = userId;
        }

        public void appendBytes(ByteBuilder byteBuilder) {
            byteBuilder.append(userId);
            byteBuilder.append(firstIntValue);
            byteBuilder.append(secondIntValue);
            byteBuilder.append(reward);
        }
    }

    public Rank(List<? extends User> userList, RankValueType firstRankType, RankValueType secondRankType) {
        rankMap = new HashMap<>();
        syncUserIdSet = new HashSet<>();
        this.firstRankType = firstRankType;
        this.secondRankType = secondRankType;

        for (User user: userList) {
            rankMap.put(user.getId(), new RankItem(user.getId()));
            syncUserIdSet.add(user.getId());
        }
    }

    public void setFirstValue(int userId, int value) {
        if (rankMap.get(userId).firstIntValue != value) {
            rankMap.get(userId).firstIntValue = value;
            syncUserIdSet.add(userId);
        }
    }

    public void setSecondValue(int userId, int value) {
        if (rankMap.get(userId).secondIntValue != value) {
            rankMap.get(userId).secondIntValue = value;
            syncUserIdSet.add(userId);
        }
    }

    public void setReward(int userId, int value) {
        if (rankMap.get(userId).reward != value) {
            rankMap.get(userId).reward = value;
            syncUserIdSet.add(userId);
        }
    }

    public boolean isChanged() {
        return syncUserIdSet.size() > 0 ? true : false;
    }

    public void appendBytes(ByteBuilder byteBuilder) {
        byteBuilder.append((byte)syncUserIdSet.size());
        for (int userId: syncUserIdSet) {
            rankMap.get(userId).appendBytes(byteBuilder);
        }
        syncUserIdSet.clear();
    }
}
