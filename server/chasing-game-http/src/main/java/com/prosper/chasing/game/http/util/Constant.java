package com.prosper.chasing.game.http.util;

/**
 * 系统配置，一经定义只能添加，不能修改！
 */
public class Constant {
	
	public static class OpCode {
		public static final short SUCCESS = 200;
		public static final short INVALID_PARAMS = 400;
		public static final short NEED_AUTHORIZATION = 401;
		public static final short NOT_PERMITED = 403;
		public static final short RESOURCE_NOT_FOUND = 404;
		public static final short INTERNAL_EXCEPTION = 500;
	}
	
	public static class PropState {
	    public static final short NORMAL = 1;
	    public static final short DISABLED = 1;
	}
	
	public static class UserPropAction {
        public static final short PLUS = 1;
        public static final short MINUS = 1;
    }
	
}























