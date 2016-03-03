package com.prosper.chasing.game.bean;

public class Position {
    
    private int x;
    private int y;
    
    private User user;
    
    private void onChange() {
        user.onChange();
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        if (getX() != x) {
            this.x = x;
            onChange();
        }
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        if (getY() != y) {
            this.y = y;
            onChange();
        }
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
    
    
    
}
