package com.prosper.chasing.game.http.controll;

import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.prosper.chasing.game.http.anotation.NeedLogin;
import com.prosper.chasing.game.http.bean.Prop;
import com.prosper.chasing.game.http.bean.UserProp;
import com.prosper.chasing.game.http.exception.InvalidArgumentException;
import com.prosper.chasing.game.http.mapper.PropMapper;
import com.prosper.chasing.game.http.mapper.UserPropMapper;
import com.prosper.chasing.game.http.service.PropService;
import com.prosper.chasing.game.http.validation.Validation;

@NeedLogin
@RestController
public class PropController {

    @Autowired
    private PropService propService;
    @Autowired
    private PropMapper propMapper;
    @Autowired
    private Validation validation;
    @Autowired
    private UserPropMapper userPropMapper;

    @RequestMapping(value="/props",method=RequestMethod.GET)
    public Object getProps(
            @RequestParam(value="ids", required=false) String ids,
            @RequestParam(value="name", required=false) String name,
            @RequestParam(value="state", required=false) Integer state,
            @RequestParam(value="page", defaultValue="1") int page,
            @RequestParam(value="pageLength", defaultValue="50") int pageLength){
        if (ids != null && !"".equals(ids)) {
            List<Integer> idList = new LinkedList<>();
            String[] idStrings = ids.split(",");
            for (String idString: idStrings) {
                try {
                    idList.add(Integer.parseInt(idString));
                } catch (NumberFormatException e) {
                    throw new InvalidArgumentException("id is not integer");
                }
            }
            return propMapper.selectListByIds(idList);
        } else if (name != null && !"".equals(name)) {
            return propService.getPropByName(name);
        } else {
            return propMapper.selectListByPageState(state, pageLength, (page - 1) * pageLength);
        }
    }
    
    @RequestMapping(value="/props",method=RequestMethod.POST)
    public Object addProp(HttpServletRequest request, @RequestBody String body){
        Prop prop = validation.getObject(body, Prop.class, new String[]{"name"});
        propService.createProp(prop);
        return null;
    }
    
    @RequestMapping(value="/props",method=RequestMethod.PATCH)
    public Object updateProp(HttpServletRequest request, @RequestBody String body){
        Prop prop = validation.getObject(body, Prop.class, new String[]{"id", "name", "state"});
        propService.updateProp(prop);
        return null;
    }
    
    @RequestMapping(value="/userProps",method=RequestMethod.GET)
    public Object getUserProps(
            @RequestParam(value="ids", required=false) String ids,
            @RequestParam(value="userId", required=false) Integer userId,
            @RequestParam(value="page", defaultValue="1", required=false) int page,
            @RequestParam(value="pageLength", defaultValue="50", required=false) int pageLength){
        if (ids != null && !"".equals(ids)) {
            List<Integer> idList = new LinkedList<>();
            String[] idStrings = ids.split(",");
            for (String idString: idStrings) {
                try {
                    idList.add(Integer.parseInt(idString));
                } catch (NumberFormatException e) {
                    throw new InvalidArgumentException("id is not integer");
                }
            }
            return userPropMapper.selectListByIds(idList);
        } else if (userId != null) {
            return userPropMapper.selectListByUser(userId);
        } else {
            return userPropMapper.selectListByPage(pageLength, (page - 1) * pageLength);
        }
    }
    
    @RequestMapping(value="/userProps",method=RequestMethod.PUT)
    public Object putUserProp(HttpServletRequest request, @RequestBody String body){
        UserProp userProp = validation.getObject(body, UserProp.class, new String[]{"name"});
        propService.putUserProp(userProp);
        return null;
    }
    
}
