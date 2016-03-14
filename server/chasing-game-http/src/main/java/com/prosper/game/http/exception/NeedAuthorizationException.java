package com.prosper.game.http.exception;

public class NeedAuthorizationException extends RuntimeException {

	private String loginUrl;
	
	public NeedAuthorizationException() {
		super();
	}

	public NeedAuthorizationException(Throwable t) {
		super(t);
	}
	
	public NeedAuthorizationException(String loginUrl) {
		this.loginUrl = loginUrl;
	}

	public String getLoginUrl() {
		return loginUrl;
	}

	public void setLoginUrl(String loginUrl) {
		this.loginUrl = loginUrl;
	}
}
