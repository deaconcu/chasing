package com.prosper.chasing.data.exception;

public class InvalidArgumentException extends RuntimeException {
	
	public InvalidArgumentException() {
		super();
	}
	
	public InvalidArgumentException(String s) {
		super(s);
	}
	
	public InvalidArgumentException(Throwable t) {
		super(t);
	}
	
	public InvalidArgumentException(String msg, Throwable t) {
		super(msg, t);
	}

}
