package com.prosper.chasing.data.thrift;

import java.util.List;

import org.apache.thrift.TException;
import org.springframework.stereotype.Component;

import com.prosper.chasing.common.boot.RPCService;
import com.prosper.chasing.common.interfaces.data.GameDataService;
import com.prosper.chasing.common.interfaces.data.PropDataService;
import com.prosper.chasing.common.interfaces.data.UserPropTr;
import com.prosper.chasing.data.bean.UserProp;
import com.prosper.chasing.data.service.PropService;
import com.prosper.chasing.data.util.ViewTransformer;

@Component
@RPCService(processorClass = PropDataService.Processor.class)
public class PropDataServiceImpl implements PropDataService.Iface {
    
    private PropService propService;

    @Override
    public List<UserPropTr> getUserProp(int userId) throws TException {
        List<UserProp> userPropList = propService.getUserProp(userId);
        return ViewTransformer.transferList(userPropList);
    }

    @Override
    public void updateUserProp(int userId, List<UserPropTr> usedUserPropList)
            throws TException {
        // TODO Auto-generated method stub
    }


}
