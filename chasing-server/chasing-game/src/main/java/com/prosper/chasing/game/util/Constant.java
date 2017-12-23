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
        public static byte OFFLINE = 3;  // 离线状态
        public static byte GAME_OVER = 4;  // 离线状态
        public static byte QUITING = 5;  // 正在退出
        public static byte QUIT = 6;     // 已退出
    }
    
}
