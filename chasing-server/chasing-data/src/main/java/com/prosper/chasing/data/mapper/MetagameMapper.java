package com.prosper.chasing.data.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.prosper.chasing.data.bean.Metagame;
import com.prosper.chasing.data.bean.Prop;

public interface MetagameMapper extends MapperI<Metagame>{

    List<Metagame> selectAll();

}
