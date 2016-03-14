package com.prosper.game.http;

public enum TestEnum {
	
	INVALID_PARAM("参数异常"),
	b("bbbb");
	
	private String value;
	
	public String value(){
		return value;
	}
	
	private TestEnum(String value) {
		this.value = value;
	}
	
}


