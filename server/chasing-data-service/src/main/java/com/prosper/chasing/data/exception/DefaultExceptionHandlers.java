package com.prosper.chasing.data.exception;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.NoHandlerFoundException;

import com.prosper.chasing.common.util.CommonUtil;
import com.prosper.chasing.data.util.Lang;
import com.prosper.chasing.data.util.Constant.OpCode;

@Component
public class DefaultExceptionHandlers {
	
	private static final Logger log = LoggerFactory.getLogger(DefaultExceptionHandlers.class);
	
	@Autowired
	private Lang lang;
	
	@ExceptionHandler(InvalidArgumentException.class)
	public ExceptionResponse handleII(Exception e) {
		return new ExceptionResponse(OpCode.INVALID_PARAMS, lang.getLang(e.getMessage() == null ? e.getClass().getName() : e.getMessage()));
    }
	
	@ExceptionHandler(ResourceNotExistException.class)
	public ExceptionResponse handleRNFE(Exception e) {
		return new ExceptionResponse(OpCode.RESOURCE_NOT_FOUND, lang.getLang(e.getMessage() == null ? e.getClass().getName() : e.getMessage()));
    }
	
	@ExceptionHandler(OperationNotAllowedException.class)
	public ExceptionResponse handleONAE(Exception e) {
		return new ExceptionResponse(OpCode.NOT_PERMITED, lang.getLang(e.getMessage() == null ? e.getClass().getName() : e.getMessage()));
    }
	
	@ExceptionHandler(NeedAuthorizationException.class)
	public ExceptionResponse handleNAE(NeedAuthorizationException e) {
	    return new ExceptionResponse(OpCode.NEED_AUTHORIZATION, lang.getLang("NEED_LOGIN"));
    }
	
	@ExceptionHandler(Exception.class)
	public ExceptionResponse handleE(Exception e) {
		log.error("operation failed, " + "exception:\n" + CommonUtil.getStackTrace(e));
		return new ExceptionResponse(OpCode.INTERNAL_EXCEPTION, lang.getLang("INTERNAL_EXCEPTION") 
		        + ":" + lang.getLang(e.getMessage() == null ? e.getClass().getName() : e.getMessage()));
    }
	
}











