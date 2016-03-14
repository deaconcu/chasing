package com.prosper.game.http.exception;

public class InternalException extends RuntimeException {
	
	public InternalException() {
		super();
	}
	
	public InternalException(String s) {
		super(s);
	}
	
	public InternalException(Throwable t) {
		super(t);
	}

}
