package com.prosper.game.http;

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

@Component
public class ConfigTest {

	@Value("${application.debug}")
	boolean debug;
}
