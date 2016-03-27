package com.prosper.chasing.game.http.mapper;

import java.util.List;

import com.prosper.chasing.game.http.bean.Prop;

public interface PropMapper extends MapperI<Prop>{

    List<Prop> selectListByName(String name);

}
