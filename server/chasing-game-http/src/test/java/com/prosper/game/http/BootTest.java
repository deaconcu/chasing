package com.prosper.game.http;

import java.util.Map;

import com.prosper.game.http.runtime.Application;
import com.prosper.game.http.runtime.DefaultHttpBeans;
import com.prosper.game.http.validation.Validation;

public class BootTest {
	
	public static void main(String... args) {
		System.setProperty("mod", "batch");
		Application.main(new String[]{"serviceTest", "print"});
	}
}
