package com.prosper.game.http;

import javax.annotation.PostConstruct;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class SystemCron {
	
	@PostConstruct
    public void syncTest() throws InterruptedException  {
		System.out.println("done");
		
		
    }
	
}
