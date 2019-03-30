package com.prosper.chasing.game.core;

import com.prosper.chasing.common.util.CommonConstant;
import com.prosper.chasing.game.base.GameInfo;
import com.prosper.chasing.game.base.GameManage;
import com.prosper.chasing.game.base.User;
import com.prosper.chasing.game.map.GameMap;
import com.prosper.chasing.game.message.Message;

import java.util.List;

/**
 * Created by deacon on 2019/3/28.
 */
public interface Game {

    /**
     * 初始化
     * @param gameManage
     * @param gameMap
     * @param gameInfo
     * @param userList
     */
    void init(GameManage gameManage, GameMap gameMap, GameInfo gameInfo, List<? extends User> userList);

    /**
     * 接收消息
     * @param parsedMessage
     */
    void offerMessage(Message parsedMessage);

    /**
     * 执行
     */
    void execute();

    /**
     * 结束
     */
    void finish();
}
