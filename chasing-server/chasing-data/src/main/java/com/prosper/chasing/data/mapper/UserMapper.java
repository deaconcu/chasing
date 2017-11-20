package com.prosper.chasing.data.mapper;

import com.prosper.chasing.data.bean.User;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UserMapper extends MapperI<User>{

    User selectOneByPhone(String phone);
}
