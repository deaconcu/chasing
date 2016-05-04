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
import com.prosper.chasing.game.http.bean.Game;
import com.prosper.chasing.game.http.bean.GameUser;
import com.prosper.chasing.game.http.bean.Metagame;
import com.prosper.chasing.game.http.bean.MetagameType;
import com.prosper.chasing.game.http.exception.InvalidArgumentException;
import com.prosper.chasing.game.http.mapper.GameMapper;
import com.prosper.chasing.game.http.mapper.MetagameMapper;
import com.prosper.chasing.game.http.mapper.MetagameTypeMapper;
import com.prosper.chasing.game.http.service.GameService;
import com.prosper.chasing.game.http.validation.Validation;

@RestController
public class GameController {

    @Autowired
    private GameService gameService;
    @Autowired
    private GameMapper gameMapper;
    @Autowired
    private MetagameMapper metagameMapper;
    @Autowired
    private MetagameTypeMapper metagameTypeMapper;
    @Autowired
    private Validation validation;

    @RequestMapping(value="/metagames",method=RequestMethod.GET)
    public Object addMetagame(
            @RequestParam(value="ids", required=false) String ids,
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
            return gameService.getMetagames(idList);
        } else {
            return gameService.getMetagames(page, pageLength);
        }
    }

    @RequestMapping(value="/metagames",method=RequestMethod.POST)
    public Object addMetagame(HttpServletRequest request, @RequestBody String body){
        Metagame metagame = validation.getObject(body, Metagame.class, new String[]{"code", "name", "duration"});
        gameService.addMetagame(metagame);
        return null;
    }

    @RequestMapping(value="/metagames",method=RequestMethod.PUT)
    public Object updateMetagame(HttpServletRequest request, @RequestBody String body){
        Metagame metagame = validation.getObject(body, Metagame.class, new String[]{"id", "name", "duration", "state"});
        gameService.updateMetagame(metagame);
        return null;
    }

    @RequestMapping(value="/metagames",method=RequestMethod.DELETE)
    public Object deleteMetagame(@RequestParam(value="id") int id){
        metagameMapper.delete(id);
        return null;
    }
    
    @RequestMapping(value="/metagameTypes",method=RequestMethod.POST)
    public Object addMetagameTypes(HttpServletRequest request, @RequestBody String body){
        MetagameType metagameType = validation.getObject(body, MetagameType.class, new String[]{"name"});
        gameService.addGameType(metagameType);
        return null;
    }

    @RequestMapping(value="/metagameTypes",method=RequestMethod.PUT)
    public Object updateMetagameType(HttpServletRequest request, @RequestBody String body){
        MetagameType metagameType = validation.getObject(body, MetagameType.class, new String[]{"id", "name"});
        gameService.putGameType(metagameType);
        return null;
    }
    
    @RequestMapping(value="/games",method=RequestMethod.GET)
    public Object getGames (
            @RequestParam(value="ids", required=false) String ids,
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
            return gameMapper.selectListByIds(idList);
        } else {
            return gameMapper.selectListByPage(pageLength, pageLength * (page - 1));
        }
    }
    
    @NeedLogin
    @RequestMapping(value="/games",method=RequestMethod.POST)
    public Object addGame(
            HttpServletRequest request, @RequestBody String body){
        Game game = validation.getObject(body, Game.class, new String[]{"metagameId", "duration"});
        int userId = Integer.parseInt(request.getHeader("userId"));
        gameService.addGame(game, userId);
        return null;
    }

    @RequestMapping(value="/games",method=RequestMethod.PUT)
    public Object updateGame(HttpServletRequest request, @RequestBody String body){
        Game game = validation.getObject(body, Game.class, new String[]{"id", "metagameId", "duration"});
        gameService.updateGame(game);
        return null;
    }
    
    @RequestMapping(value="/gameUsers",method=RequestMethod.GET)
    public Object getGameUsers (@RequestParam(value="gameId", required=false) String gameId){
        return gameService.getGameUser(gameId);
    }
    
    @RequestMapping(value="/gameUsers",method=RequestMethod.POST)
    public Object addGameUser(
            HttpServletRequest request, @RequestBody String body){
        GameUser gameUser = validation.getObject(body, GameUser.class, new String[]{"gameId"});
        int userId = Integer.parseInt(request.getHeader("userId"));
        gameService.addGameUser(gameUser, userId);
        return null;
    }
    
    @RequestMapping(value="/gameUsers",method=RequestMethod.DELETE)
    public Object deleteGameUser(HttpServletRequest request, @RequestParam(value="gameId") int gameId){
        int userId = Integer.parseInt(request.getHeader("userId"));
        gameService.deleteGameUser(gameId, userId);
        return null;
    }

}
