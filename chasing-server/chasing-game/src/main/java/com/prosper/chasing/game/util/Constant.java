package com.prosper.chasing.game.util;

import com.prosper.chasing.common.util.Pair;

public class Constant {
    
    public static class GameExceptionConstant {
        public static final Pair<Integer, String> gameExistException = new Pair<>(101, "game is exist");
    }

    public static class MessageType {
        public static byte USER = 1;
        public static byte PREPARE = 2;
        public static byte RESULT = 3;
        public static byte NO_GAME = 4;
    }

    public static class UserState {
        public static byte LOADED = 1;   // 服务端用户信息加载完成, 玩家在服务端的初始状态
        public static byte ACTIVE = 2;   // 客户端发送连接消息后与服务端建立连接，用户进入活跃状态
        public static byte GHOST = 3;    // 活动状态
        public static byte GAME_OVER = 4;  // 用户游戏已结束
        public static byte RESULT_INFORMED = 5; // 排名及奖励信息已通知
        public static byte OFFLINE = 6;  // 离线状态
        public static byte QUITING = 7;  // 正在退出
        public static byte QUIT = 8;     // 已退出
    }

    public static class MoveState {
        public static byte IDLE = 1;
        public static byte WALK = 2;
        public static byte RUN = 3;
    }

    public static class MessageRetryType {
        public static byte NONE = 1;
        public static byte ALL = 2;
        public static byte SINGLE = 3;
    }

    public static class TargetType {
        public static byte TYPE_SELF = 0; // 自身
        public static byte TYPE_USER = 1; // 玩家
        public static byte TYPE_PROP = 2; // 道具
        public static byte TYPE_NPC = 3; // npc
        public static byte TYPE_POSITION = 4; // 位置
        public static byte TYPE_NONE = 5; // 没有对象
    }

    public static class ChasingConfig {
        public static int DISTANCE_CHASING = 20000;  // 追逐距离
        public static int DISTANCE_CATCHING = 2000;   // 捕获距离
        public static int DISTANCE_CATCHING_STATIC = 1000;   // 静止对象捕获距离

        public static int SECOND_CHASING = 10;  // 追逐保持时间
        public static int SECOND_CATCHING = 10;  // 捕获保持时间
    }

    public static class NPCType {
        public static byte OTHER = 1;
        public static byte MERCHANT = 2;
    }

    public static class FirstSync {
        public static byte TRUE = 1;
        public static byte FALSE = 2;
    }

    public static class Direction {
        public static byte RIGHT = 0;
        public static byte DOWN = 1;
        public static byte LEFT = 2;
        public static byte UP = 3;
    }

}
