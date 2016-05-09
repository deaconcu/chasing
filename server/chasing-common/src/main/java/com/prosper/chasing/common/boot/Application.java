package com.prosper.chasing.common.boot;

public interface Application {
    
    /**
     * 应用ip
     */
    public String getIP();
    
    /**
     * 应用端口
     */
    public int getPort();
    
    /**
     * 应用base package，用来扫描需要的类
     */
    public String getPackage();

    /**
     * 应用名称
     */
    public String getName();
    
    /**
     * 执行入口
     */
    public void run(String[] args);

}
