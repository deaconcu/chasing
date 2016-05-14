package com.prosper.chasing.game.http.controll;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.prosper.chasing.game.http.anotation.NeedLogin;
import com.prosper.chasing.game.http.bean.User;
import com.prosper.chasing.game.http.bean.UserData;
import com.prosper.chasing.game.http.mapper.UserDataMapper;
import com.prosper.chasing.game.http.service.UserService;
import com.prosper.chasing.game.http.validation.Validation;

@RestController
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private UserDataMapper userDataMapper;
    @Autowired
    private Validation validation;

    @RequestMapping(value="/users",method=RequestMethod.POST)
    public Object addUser(HttpServletRequest request, @RequestBody String body){
        String ipAddress = request.getHeader("X-FORWARDED-FOR");
        if (ipAddress == null || "".equals(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }

        User user = validation.getObject(body, User.class, new String[]{"name"});

        userService.createUser(user, ipAddress);
        Map<String, Object> response = new HashMap<>();
        response.put("id", user.getId());
        response.put("password", user.getPassword());
        return response;
    }
    
    @RequestMapping(value="/users",method=RequestMethod.GET)
    public Object addUser(
            @RequestParam(value="page") int page,
            @RequestParam(value="pageLength", defaultValue="50") int pageLength,
            @RequestParam(value="ids") int ids,
            @RequestParam(value="email") String email,
            @RequestParam(value="phone") String phone
            ){
        // TODO admin api
        return null;
    }
    
    @NeedLogin
    @RequestMapping(value="/userData",method=RequestMethod.GET)
    public Object getUserData(@RequestParam(value="id") int id){
        UserData userData = userDataMapper.selectOne(id);
        return userData;
    }

}