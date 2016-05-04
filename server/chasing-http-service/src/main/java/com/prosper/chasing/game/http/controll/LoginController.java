package com.prosper.chasing.game.http.controll;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.prosper.chasing.game.http.anotation.NeedLogin;
import com.prosper.chasing.game.http.bean.User;
import com.prosper.chasing.game.http.service.UserService;
import com.prosper.chasing.game.http.validation.Validation;

@RestController
public class LoginController {

    @Autowired
    private UserService userService;
    @Autowired
    private Validation validation;

    @RequestMapping(value="/logins",method=RequestMethod.POST)
    public Object login(HttpServletRequest request, @RequestBody String body){
        String ipAddress = request.getHeader("X-FORWARDED-FOR");
        if (ipAddress == null || "".equals(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }

        User user = validation.getObject(body, User.class, new String[]{"password"});
        String sessionId = userService.login(user);

        Map<String, Object> response = new HashMap<>();
        response.put("sessionId", sessionId);
        response.put("id", user.getId());
        return response;
    }
    
    @NeedLogin
    @RequestMapping(value="/logouts",method=RequestMethod.POST)
    public Object logout(HttpServletRequest request){
        String userId = request.getHeader("userId");
        userService.logout(userId);
        return null;
    }
    
}
