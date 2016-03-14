package com.prosper.game.http;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.web.bind.annotation.ControllerAdvice;

import com.prosper.game.http.runtime.EnableBatch;
import com.prosper.game.http.runtime.EnableCron;
import com.prosper.game.http.runtime.EnableHttp;

@EnableBatch
@ComponentScan(
		basePackages = {
				"com.youku.java.cms.common"},
		includeFilters = @ComponentScan.Filter(
				value= ControllerAdvice.class, type = FilterType.ANNOTATION)
		)
public class BatchBeans {

}













