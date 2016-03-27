package com.prosper.chasing.game.http.aspect;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.qos.logback.classic.Logger;

import com.prosper.chasing.game.http.exception.ExceptionHandlerMap;
import com.prosper.chasing.game.http.util.CommonConfig;
import com.prosper.chasing.game.http.util.CommonUtil;
import com.prosper.chasing.game.http.util.JsonUtil;
import com.prosper.chasing.game.http.util.Constant.OpCode;

@Aspect
@Component
public class Aspecter {

	private Logger log = (Logger)LoggerFactory.getLogger(Aspecter.class);

	@Autowired
	private JsonUtil jsonUtil;
	@Autowired
	private CommonConfig commonConfig;
	@Autowired
	private ExceptionHandlerMap exceptionHandlerMap;

	@Around("@within(org.springframework.web.bind.annotation.RestController)")
	public Object doControllerAround(ProceedingJoinPoint pjp) throws Throwable {
	    Map<String, Object> response = null;
		try {
			Object[] args = pjp.getArgs();
			long startTime = System.currentTimeMillis();
			Object dataResponse = pjp.proceed(args);

			response = new HashMap<String, Object>();
			if (dataResponse != null) {
			    response.put("data", response);
			}

			response.put("cost", (double)(System.currentTimeMillis() - startTime) / 1000);
			response.put("code", OpCode.SUCCESS);
		} catch (Exception e) {
			Method method = exceptionHandlerMap.getMethod(e.getClass());
			if (method != null) {
				Object errorResponse = method.invoke(exceptionHandlerMap.getExceptionHandler(method), e);
				return errorResponse;
			}
		}
		return response;
	}

	@Around("@within(org.springframework.scheduling.annotation.Scheduled) || "
			+ "@annotation(org.springframework.scheduling.annotation.Scheduled)")
	public void doTaskAround(ProceedingJoinPoint pjp) throws Throwable {
		try {
			pjp.proceed();
		} catch (Exception e) {
			log.error("batch operation failed, must check for exception, "
					+ "exception:\n" + CommonUtil.getStackTrace(e));
		}
	}

}
