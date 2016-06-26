package com.prosper.chasing.data.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.prosper.chasing.data.bean.GameUser;
import com.prosper.chasing.data.bean.Metagame;
import com.prosper.chasing.data.bean.Prop;

public interface GameUserMapper extends MapperI<GameUser>{

    GameUser selectOneByUserId(@Param("userId") Integer userId);

    GameUser selectOneByGameUser(@Param("gameId") Integer gameId, @Param("userId") Integer userId);

    List<Integer> selectUserListByGameId(@Param("gameId") Integer gameId);

    
}
