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
        public static byte LOADED = 1;   // 加载完成状态
        public static byte ACTIVE = 2;   // 活动状态
        public static byte GHOST = 3;   // 活动状态
        public static byte GAME_OVER = 4;  // 游戏结束状态
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

    public static class MapBlockType {
        public static byte WALL = 0;
        public static byte BLOCK_AREA = 1;
        public static byte MAIN_ROAD = 2;
        public static byte SHORTCUT = 3;
        public static byte BRANCH = 4;
    }

    public static class Direction {
        public static byte RIGHT = 0;
        public static byte DOWN = 1;
        public static byte LEFT = 2;
        public static byte UP = 3;
    }

    public static class TerrainType {
        public static byte ANIMAL = 1;
        public static byte BLANK = 2;
        public static byte BUILDING = 3;
        public static byte FOG = 4;
        public static byte FOREST = 5;
        public static byte GRASS = 6;
        public static byte LAVA = 7;
        public static byte RAIN = 8;
        public static byte ROAD = 9;
        public static byte ROCK = 10;
        public static byte SAND = 11;
        public static byte SNOW = 12;
        public static byte SWAMP = 13;
        public static byte VEGETABLE = 14;
        public static byte VIEW = 15;
        public static byte WATER = 16;
        public static byte WHEAT = 17;
        public static byte WILDWIND = 18;
        public static byte WIND = 19;
        public static byte UPLAND = 20;
    }

    public static class BuildingType {
        public static byte WAREHOUSE = 1;
        public static byte STORE = 2;
        public static byte TALL_TREE = 3;
        public static byte GRAVEYARD = 4;
        public static byte WELL = 5;
        public static byte JACKSTRAW = 6;
    }

}
