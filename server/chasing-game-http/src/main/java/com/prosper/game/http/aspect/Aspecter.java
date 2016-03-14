package com.prosper.game.http.aspect;

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

import com.prosper.game.http.exception.ExceptionHandlerMap;
import com.prosper.game.http.util.CommonConfig;
import com.prosper.game.http.util.CommonUtil;
import com.prosper.game.http.util.JsonUtil;
import com.prosper.game.http.util.Constant.OpCode;

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

	@SuppressWarnings("unchecked")
	@Around("@within(org.springframework.web.bind.annotation.RestController)")
	public Object doControllerAround(ProceedingJoinPoint pjp) throws Throwable {
		Object response = null;

		try {
			Object[] args = pjp.getArgs();
			long startTime = System.currentTimeMillis();
			response = pjp.proceed(args);

			Map<String, Object> castResponse;
			if (response == null) {
				response = new HashMap<String, Object>();
			}

			if (response instanceof Map) {
				castResponse = (Map<String, Object>) response;
				castResponse.put("cost", (double)(System.currentTimeMillis() - startTime) / 1000);
				castResponse.put("code", OpCode.SUCCESS);
			}
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
