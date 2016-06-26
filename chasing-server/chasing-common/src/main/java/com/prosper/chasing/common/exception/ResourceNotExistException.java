package com.prosper.chasing.common.exception;

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
