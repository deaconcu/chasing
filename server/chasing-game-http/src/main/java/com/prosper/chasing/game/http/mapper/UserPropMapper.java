package com.prosper.chasing.game.http.mapper;

import java.util.List;

import com.prosper.chasing.game.http.bean.Prop;
import com.prosper.chasing.game.http.bean.UserProp;

public interface UserPropMapper extends MapperI<UserProp>{

    List<UserProp> selectListByUser(String string);

    UserProp selectOneByUserProp(int userId, int propId);

}
