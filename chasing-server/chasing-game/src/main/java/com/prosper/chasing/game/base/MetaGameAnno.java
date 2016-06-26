package com.prosper.chasing.game.base;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface MetaGameAnno {

    /**
     * 标注当前游戏metagame code
     */
    String value();
    
}
