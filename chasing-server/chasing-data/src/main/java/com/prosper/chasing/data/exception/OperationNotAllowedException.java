package com.prosper.chasing.data.exception;

public class OperationNotAllowedException extends RuntimeException {

	public OperationNotAllowedException() {
		super();
	}

	public OperationNotAllowedException(String s) {
		super(s);
	}

	public OperationNotAllowedException(Throwable t) {
		super(t);
	}
}
