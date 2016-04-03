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

import com.prosper.chasing.game.http.bean.Prop;
import com.prosper.chasing.game.http.bean.UserProp;
import com.prosper.chasing.game.http.exception.InvalidArgumentException;
import com.prosper.chasing.game.http.mapper.PropMapper;
import com.prosper.chasing.game.http.mapper.UserPropMapper;
import com.prosper.chasing.game.http.service.PropService;
import com.prosper.chasing.game.http.validation.Validation;

@RestController
public class UserPropController {

    @Autowired
    private PropService propService;
    @Autowired
    private UserPropMapper userPropMapper;
    @Autowired
    private Validation validation;

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
            return userPropMapper.selectListByUser("name");
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
