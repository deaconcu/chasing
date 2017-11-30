package com.prosper.chasing.game.message;

/**
 * 系统发送的消息，必须存在gameId，用来通知一些异步执行的消息或者系统消息，比如退出游戏完成
 */
public class SystemMessage extends Message {
    
    public SystemMessage(int gameId) {
        setGameId(gameId);
    }
}
