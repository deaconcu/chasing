package com.prosper.chasing.game.bean;

public class User {
    
    private long id;
    private Position position;
    private Game game;
    
    public void addX(int x) {
        this.position.setX(this.position.getX() + x);
    }
    
    public void addY(int y) {
        this.position.setX(this.position.getX() + y);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public void onChange() {
        game.onChange(this);
    }
    
}
