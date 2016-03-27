package com.prosper.chasing.game.http.service;

import java.util.Date;
import java.util.List;

import com.prosper.chasing.game.http.bean.Prop;
import com.prosper.chasing.game.http.bean.UserProp;
import com.prosper.chasing.game.http.exception.GameException;
import com.prosper.chasing.game.http.exception.InternalException;
import com.prosper.chasing.game.http.exception.InvalidArgumentException;
import com.prosper.chasing.game.http.mapper.PropMapper;
import com.prosper.chasing.game.http.mapper.UserPropMapper;
import com.prosper.chasing.game.http.util.CommonUtil;
import com.prosper.chasing.game.http.util.Constant.PropState;
import com.prosper.chasing.game.http.util.Constant.UserPropAction;

import org.springframework.beans.factory.annotation.Autowired;

public class PropService {
    
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
        Prop existProp = propMapper.selectOne(prop.getId());
        if (prop.getName() != null && "".equals(prop.getName())) {
            throw new InvalidArgumentException("prop name is not valid");
        }
        
        if (prop.getState() != PropState.NORMAL && prop.getState() != PropState.DISABLED) {
            throw new InvalidArgumentException("prop s is not valid");
        }
        existProp.setName(prop.getName());
        existProp.setState(prop.getState());
        existProp.setUpdateTime(CommonUtil.getTime(new Date()));
        propMapper.update(prop);
    }

    public void putUserProp(UserProp userProp) {
        int action = userProp.getAction();
        if (action != UserPropAction.PLUS && action != UserPropAction.MINUS) {
            throw new InvalidArgumentException("action is not valid");
        }
        UserProp existUserProp = userPropMapper.selectOneByUserProp(userProp.getUserId(), userProp.getPropId());
        if (existUserProp == null) {
            if (userProp.getAction() == UserPropAction.PLUS) {
                userProp.setUpdateTime(CommonUtil.getTime(new Date()));
                userPropMapper.insert(userProp);
            } else if (userProp.getAction() == UserPropAction.MINUS) {
                throw new GameException("prop is not enough");
            }
        } else {
            if (userProp.getAction() == UserPropAction.PLUS) {
                existUserProp.setCount(existUserProp.getCount() + userProp.getCount());
                existUserProp.setUpdateTime(CommonUtil.getTime(new Date()));
                userPropMapper.update(existUserProp);
            } else if (userProp.getAction() == UserPropAction.MINUS) {
                int computeCount = existUserProp.getCount() + userProp.getCount();
                if (computeCount < 0) {
                    throw new GameException("prop is not enough");
                }
                existUserProp.setCount(computeCount);
                existUserProp.setUpdateTime(CommonUtil.getTime(new Date()));
                userPropMapper.update(existUserProp);
            }
        }
    }

}
