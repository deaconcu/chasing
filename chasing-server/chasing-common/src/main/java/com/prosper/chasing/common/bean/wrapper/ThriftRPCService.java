package com.prosper.chasing.common.bean.wrapper;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apache.thrift.TProcessor;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ThriftRPCService {
    
    Class<? extends TProcessor> processorClass();

}
