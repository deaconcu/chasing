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

import com.prosper.chasing.game.http.bean.User;
import com.prosper.chasing.game.http.exception.ResourceNotExistException;
import com.prosper.chasing.game.http.service.UserService;
import com.prosper.chasing.game.http.validation.Validation;

@RestController
public class FriendController {

    @Autowired
    private UserService userService;
    @Autowired
    private Validation validation;

    @RequestMapping(value="/friendRequests",method=RequestMethod.POST)
    public Object addFriendRequest(HttpServletRequest request, @RequestBody String body){
        User user = validation.getObject(body, User.class, new String[]{"password"});
        boolean exist = userService.checkUser(user);
        if (!exist) {
            throw new ResourceNotExistException("user is not exist");
        }

        Map<String, Object> response = new HashMap<>();
        response.put("id", user.getId());
        return response;
    }
    
}
