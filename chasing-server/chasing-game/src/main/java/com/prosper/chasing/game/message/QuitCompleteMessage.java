package com.prosper.chasing.game.message;

/**
 * 用户退出完成需要发送的消息
 */
public class QuitCompleteMessage extends SystemMessage {

    private int userId;
    
    public QuitCompleteMessage(int gameId, int userId) {
        super(gameId);
        this.userId = userId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
}
