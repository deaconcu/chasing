package com.prosper.chasing.data.service;

import java.util.Date;
import java.util.List;

import com.prosper.chasing.common.util.CommonUtil;
import com.prosper.chasing.data.bean.Prop;
import com.prosper.chasing.data.bean.UserProp;
import com.prosper.chasing.data.exception.GameException;
import com.prosper.chasing.data.exception.InternalException;
import com.prosper.chasing.data.exception.InvalidArgumentException;
import com.prosper.chasing.data.exception.ResourceNotExistException;
import com.prosper.chasing.data.mapper.PropMapper;
import com.prosper.chasing.data.mapper.UserMapper;
import com.prosper.chasing.data.mapper.UserPropMapper;
import com.prosper.chasing.data.util.Constant.PropState;
import com.prosper.chasing.data.util.Constant.UserPropAction;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PropService {
    
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private PropMapper propMapper;
    @Autowired
    private UserPropMapper userPropMapper;

    public Object getPropByName(String name) {
        List<Prop> propList = propMapper.selectListByName(name);
        if(propList.size() > 1) {
            throw new InternalException("return more than one prop by name:" + name);
        }
        return propList;
    }

    public void createProp(Prop prop) {
        List<Prop> propList = propMapper.selectListByName(prop.getName());
        if (propList.size() > 0) {
            throw new InvalidArgumentException("prop name is exist");
        }
        prop.setCreateTime(CommonUtil.getTime(new Date()));
        prop.setUpdateTime(CommonUtil.getTime(new Date()));
        prop.setState(PropState.DISABLED);
        propMapper.insert(prop);
    }

    public void updateProp(Prop prop) {
        if (prop.getName() != null && "".equals(prop.getName())) {
            throw new InvalidArgumentException("prop name is not valid");
        }
        
        if (prop.getState() != PropState.NORMAL && prop.getState() != PropState.DISABLED) {
            throw new InvalidArgumentException("prop state is not valid");
        }
        Prop existProp = propMapper.selectOne(prop.getId());
        if (existProp == null) {
            throw new ResourceNotExistException("prop is not exist");
        }
        existProp.setName(prop.getName());
        existProp.setState(prop.getState());
        existProp.setUpdateTime(CommonUtil.getTime(new Date()));
        propMapper.update(existProp);
    }

    public void putUserProp(UserProp userProp) {
        if (userProp.getCount() <= 0) {
            throw new InvalidArgumentException("count is not valid");
        }
        int action = userProp.getAction();
        if (action != UserPropAction.PLUS && action != UserPropAction.MINUS) {
            throw new InvalidArgumentException("action is not valid");
        }
        if (userMapper.selectOne(userProp.getUserId()) == null) {
            throw new InvalidArgumentException("user is not exist");
        }
        
        UserProp existUserProp = userPropMapper.selectOneByUserProp(userProp.getUserId(), userProp.getPropCode());
        if (existUserProp == null) {
            if (userProp.getAction() == UserPropAction.PLUS) {
                userProp.setCreateTime(CommonUtil.getTime(new Date()));
                userProp.setUpdateTime(CommonUtil.getTime(new Date()));
                userPropMapper.insert(userProp);
            } else if (userProp.getAction() == UserPropAction.MINUS) {
                throw new InvalidArgumentException("prop is not enough");
            }
        } else {
            if (userProp.getAction() == UserPropAction.PLUS) {
                existUserProp.setCount(existUserProp.getCount() + userProp.getCount());
                existUserProp.setUpdateTime(CommonUtil.getTime(new Date()));
                userPropMapper.update(existUserProp);
            } else if (userProp.getAction() == UserPropAction.MINUS) {
                int computeCount = existUserProp.getCount() - userProp.getCount();
                if (computeCount < 0) {
                    throw new InvalidArgumentException("prop is not enough");
                }
                existUserProp.setCount(computeCount);
                existUserProp.setUpdateTime(CommonUtil.getTime(new Date()));
                userPropMapper.update(existUserProp);
            }
        }
    }
    
    /**
     * 获取用户道具
     */
    public List<UserProp> getUserProp(int userId) {
        return userPropMapper.selectListByUser(userId);
    }

}
