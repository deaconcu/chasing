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
import com.prosper.chasing.game.http.exception.InvalidArgumentException;
import com.prosper.chasing.game.http.mapper.PropMapper;
import com.prosper.chasing.game.http.service.PropService;
import com.prosper.chasing.game.http.validation.Validation;

@RestController
public class PropController {

    @Autowired
    private PropService propService;
    @Autowired
    private PropMapper propMapper;
    @Autowired
    private Validation validation;

    @RequestMapping(value="/props",method=RequestMethod.GET)
    public Object getProps(
            @RequestParam(value="ids") String ids,
            @RequestParam(value="name") String name,
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
        } else if (name != null && "".equals(name)) {
            return propService.getPropByName("name");
        } else {
            return propMapper.selectListByPage(pageLength, (page - 1) * pageLength);
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
    
}
