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
    
}
