package com.prosper.chasing.data.util;

/**
 * 系统配置，一经定义只能添加，不能修改！
 */
public class Constant {
    
    public static String SERVE_LIST_ZK_NAME = "/gameDataServer/serverList";
    
    public static class CacheName {
        public static final String session = "c_session_";
        public static final String game = "c_game_";
        public static final String gameUser = "c_game_user_";
        public static final String userGame = "c_user_game_";
        public static final String systemUserList = "c_system_user_list_";
        public static final String userState = "c_user_state_";  // 0:none, 1:join user list
    }
	
	public static class OpCode {
		public static final short SUCCESS = 200;
		public static final short INVALID_PARAMS = 400;
		public static final short NEED_AUTHORIZATION = 401;
		public static final short NOT_PERMITED = 403;
		public static final short RESOURCE_NOT_FOUND = 404;
		public static final short INTERNAL_EXCEPTION = 500;
	}
	
	public static class PropState {
	    public static final short NORMAL = 1;      //正常
	    public static final short DISABLED = 2;    //禁用
	}
	
	public static class UserPropAction {
        public static final short PLUS = 1;   //加
        public static final short MINUS = 2;  //减
    }
	
	public static class FriendState {
	    public static final short APPLYING = 1;   //申请中
	    public static final short APPROVED = 2;   //通过
	}
	
	public static class FriendType {
        public static final short APPLYING_TO_ME = 1;   //申请添加我为好友的
        public static final short APPLYING_FROM_ME = 2;   //我申请添加为好友的
        public static final short APPROVED = 3;   //通过的
    }
	
	public static class PriceUnit {
	    public static final short GOLD = 1;    //游戏金币
        public static final short POINT = 2;   //游戏点数
	}
	
	public static class MetagameState {
	    public static final short NORMAL = 1;      //正常
        public static final short DISABLED = 2;    //禁用
    }
	
	public static class GameState {
        public static final short CREATE = 1;         //创建
        public static final short POST_START = 2;     //提交开始
        public static final short LOADING = 3;         //加载中
        public static final short PROCESSING = 4;    //进行中
        public static final short FINISHED = 5;    //完成
    }
	
}























