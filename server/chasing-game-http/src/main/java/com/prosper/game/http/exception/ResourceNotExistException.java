package com.prosper.game.http.exception;

public class ResourceNotExistException extends RuntimeException {
	
	public ResourceNotExistException() {
		super();
	}
	
	public ResourceNotExistException(String s) {
		super(s);
	}
	
	public ResourceNotExistException(Throwable t) {
		super(t);
	}

}
