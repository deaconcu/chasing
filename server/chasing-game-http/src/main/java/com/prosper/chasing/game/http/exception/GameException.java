package com.prosper.chasing.game.http.exception;

public class GameException extends RuntimeException {
	
	public GameException() {
		super();
	}
	
	public GameException(String s) {
		super(s);
	}
	
	public GameException(Throwable t) {
		super(t);
	}
	
	public GameException(String msg, Throwable t) {
		super(msg, t);
	}

}
