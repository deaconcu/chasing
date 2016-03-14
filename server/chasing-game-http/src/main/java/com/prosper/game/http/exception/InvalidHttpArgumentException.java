package com.prosper.game.http.exception;

public class InvalidHttpArgumentException extends RuntimeException {
	
	public InvalidHttpArgumentException() {
		super();
	}
	
	public InvalidHttpArgumentException(String s) {
		super(s);
	}
	
	public InvalidHttpArgumentException(Throwable t) {
		super(t);
	}
	
	public InvalidHttpArgumentException(String msg, Throwable t) {
		super(msg, t);
	}

}
