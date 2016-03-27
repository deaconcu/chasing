package com.prosper.chasing.game.http.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

@Component
public class CommonConfig {

	@Value("${application.debug}")
	public boolean debug;
	
	@Value("${application.debugInternal}")
	public short debugInternal;

}
