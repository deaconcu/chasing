package com.prosper.chasing.game.base.impl;

import org.springframework.stereotype.Component;

import com.prosper.chasing.game.base.Game;
import com.prosper.chasing.game.base.MetaGameAnno;
import com.prosper.chasing.game.base.User;
import com.prosper.chasing.game.message.Message;

@Component
@MetaGameAnno("chasing")
public class ChasingGame extends Game {

    @Override
    public void executeMessage(Message message) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onChange(User user) {
        // TODO Auto-generated method stub
        
    }

}
