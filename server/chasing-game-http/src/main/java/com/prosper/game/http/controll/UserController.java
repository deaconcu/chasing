package com.prosper.game.http.controll;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.prosper.game.http.bean.User;
import com.prosper.game.http.service.UserService;
import com.prosper.game.http.validation.Validation;

@RestController
public class UserController {
    
    @Autowired
    private UserService userService;
    @Autowired
    private Validation validation;
    
    @RequestMapping(value="/user",method=RequestMethod.POST)
    public Object addUser(HttpServletRequest request, @RequestBody String body){
        String ipAddress = request.getHeader("X-FORWARDED-FOR");
        if (ipAddress == null || "".equals(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }
        
        User user = validation.getObject(body, User.class, new String[]{"name"});
        
        long userId = userService.createUser(user, ipAddress);
        return null;
    }

}
