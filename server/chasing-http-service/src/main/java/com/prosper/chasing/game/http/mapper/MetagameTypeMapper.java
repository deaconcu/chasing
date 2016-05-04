package com.prosper.chasing.game.http.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.prosper.chasing.game.http.bean.Metagame;
import com.prosper.chasing.game.http.bean.MetagameType;
import com.prosper.chasing.game.http.bean.Prop;

public interface MetagameTypeMapper extends MapperI<MetagameType>{

    List<MetagameType> selectAll();

    
}
