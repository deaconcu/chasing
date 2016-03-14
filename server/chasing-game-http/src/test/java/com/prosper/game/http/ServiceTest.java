package com.prosper.game.http;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.prosper.game.http.runtime.Application;
import com.prosper.game.http.validation.Validation;

@Service
public class ServiceTest {
	
	@Autowired
	Validation validation;

	public void print() {
		validation.getObject("{\"title\":\"bbbbb\",\"content\":\"aaaa\",\"operatorId\":111,\"operatorName\":\"xxx\",\"ntime\":\"2014-10-2310:15:43\",\"state\":1,\"criterion\":2,\"memo\":\"xxxxx\",\"type\":1,\"id\":72461}", Map.class, null);
	}
}
