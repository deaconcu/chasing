package com.prosper.chasing.data.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.prosper.chasing.data.bean.Game;
import com.prosper.chasing.data.bean.Metagame;
import com.prosper.chasing.data.bean.Prop;

public interface GameMapper extends MapperI<Game>{

    List<Game> selectListByStatePage(
            @Param("state") int state, 
            @Param("limit") int limit, 
            @Param("offset") int offset);

    List<Game> selectListByStateAndServer(
            @Param("state") int state, 
            @Param("server") String server, 
            @Param("limit") int limit);

    void updateGameByState(
            @Param("sourceState") int sourceState,
            @Param("targetState") int targetState,
            @Param("server") String server, 
            @Param("updateTime") String updateTime, 
            @Param("limit") int limit);
}
