package com.prosper.chasing.game.http.controll;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.prosper.chasing.game.http.anotation.NeedLogin;
import com.prosper.chasing.game.http.bean.Friend;
import com.prosper.chasing.game.http.bean.Prop;
import com.prosper.chasing.game.http.bean.User;
import com.prosper.chasing.game.http.exception.ResourceNotExistException;
import com.prosper.chasing.game.http.service.UserService;
import com.prosper.chasing.game.http.validation.Validation;

@NeedLogin
@RestController
public class FriendController {

    @Autowired
    private UserService userService;
    @Autowired
    private Validation validation;

    @RequestMapping(value="/friends",method=RequestMethod.GET)
    public Object getFriends(
            HttpServletRequest request,
            @RequestParam(value="type") int type,
            @RequestParam(value="page", defaultValue="1") int page,
            @RequestParam(value="pageLength", defaultValue="50") int pageLength){
        int userId = Integer.parseInt(request.getHeader("userId"));
        List<Friend> friendList = userService.getFriends(userId, type, page, pageLength);
        return friendList;
    }
    
    @RequestMapping(value="/friendRequests",method=RequestMethod.POST)
    public Object applyFriend(HttpServletRequest request, @RequestBody String body){
        Friend friend = validation.getObject(body, Friend.class, new String[]{"friendUserId"});
        userService.applyFriend(Integer.parseInt(request.getHeader("userId")), friend);
        return null;
    }
    
    @RequestMapping(value="/friendApprovals",method=RequestMethod.POST)
    public Object approveFriend(HttpServletRequest request, @RequestBody String body){
        Friend friend = validation.getObject(body, Friend.class, new String[]{"friendUserId"});
        userService.approveFriend(Integer.parseInt(request.getHeader("userId")), friend);
        return null;
    }
    
    @RequestMapping(value="/friends",method=RequestMethod.DELETE)
    public Object approveFriend(
            HttpServletRequest request,
            @RequestParam(value="friendUserId") int friendUserId){
        userService.deleteFriend(Integer.parseInt(request.getHeader("userId")), friendUserId);
        return null;
    }
    
}
