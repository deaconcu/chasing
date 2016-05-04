package com.prosper.chasing.game.http.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.prosper.chasing.game.http.bean.Prop;

public interface PropMapper extends MapperI<Prop>{

    List<Prop> selectListByName(@Param("name") String name);

    List<Prop> selectListByPageState(
            @Param("state") int state, 
            @Param("limit") int limit, 
            @Param("offset") int offset);

}
