package com.prosper.chasing.common.boot;

public abstract class Application {
    
    /**
     * 执行前执行
     */
    public abstract void beforeExecute(String[] args);
    
    /**
     * 执行
     */
    public abstract void execute(String[] args);
    
    /**
     * 执行后执行
     */
    public abstract void afterExecute(String[] args);

    /**
     * 执行入口
     */
    public void run(String[] args) {
        beforeExecute(args);
        execute(args);
        afterExecute(args);
    }
    
}
