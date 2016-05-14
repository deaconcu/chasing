package com.prosper.chasing.game.util;

import com.prosper.chasing.common.util.Pair;

public class Constant {
    
    public static class GameExceptionConstant {
        public static final Pair<Integer, String> gameExistException = new Pair<>(101, "game is exist");
    }
    
    public static class GameLoadingState {
        public static final short START = 1;                    //开始加载
        public static final short GAME_INFO_FINISHED = 2;     //游戏信息加载完成
        public static final short USER_INFO_FINISHED = 3;     //用户信息加载完成
        public static final short PROP_INFO_FINISHED = 4;     //道具信息加载完成
        public static final short FINISHED = 5;                 //完成
    }
    
}
