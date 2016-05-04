package com.prosper.chasing.game.http.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class Config {

	@Value("${db.mysql.ip}")
	public String dbIp;
	
	@Value("${db.mysql.port}")
	public String dbPort;
	
	@Value("${db.mysql.dbName}")
	public String dbName;
	
	@Value("${db.mysql.userName}")
	public String dbUserName;
	
	@Value("${db.mysql.password}")
	public String dbPassword;
		
}
