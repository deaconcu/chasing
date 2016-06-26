package com.prosper.chasing.common.boot;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface RuntimeSpringBeans {

    /**
     * 启动模式
     */
    String mode();
    
    /**
     * 是否含有web的部分
     */
    boolean withWeb() default false;
    
}
