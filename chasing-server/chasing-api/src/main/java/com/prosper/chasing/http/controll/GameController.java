package com.prosper.chasing.http.controll;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.prosper.chasing.data.bean.CacheGame;
import com.prosper.chasing.data.bean.Game;
import com.prosper.chasing.data.bean.GameUser;
import com.prosper.chasing.data.bean.Metagame;
import com.prosper.chasing.data.bean.MetagameType;
import com.prosper.chasing.data.exception.InvalidArgumentException;
import com.prosper.chasing.data.mapper.GameMapper;
import com.prosper.chasing.data.mapper.MetagameMapper;
import com.prosper.chasing.data.mapper.MetagameTypeMapper;
import com.prosper.chasing.data.service.GameService;
import com.prosper.chasing.http.anotation.NeedLogin;
import com.prosper.chasing.http.validation.Validation;

@NeedLogin
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
            @RequestParam(value="page", defaultValue="1") int page,
            @RequestParam(value="pageLength", defaultValue="50") int pageLength){
        return gameService.getMetagames(page, pageLength);
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
            @RequestParam(value="userId", required=false) Integer userId,
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
        } else if (userId != null){
            return gameService.getGameByUser(userId);
        } else {
            return gameMapper.selectListByPage(pageLength, pageLength * (page - 1));
        }
    }

    @RequestMapping(value="/gameMaps",method=RequestMethod.GET)
    public Object getGameMap(@RequestParam(value="id") String id) {
        return gameService.getGameMapBytes(id);
    }
    
    /**
     * 根据用户id或者游戏id获取游戏信息
     */
    @RequestMapping(value="/cache/games",method=RequestMethod.GET)
    public Object getGameInCache(
    		HttpServletRequest request, 
    		@RequestParam(value="gameId", required=false) String gameId){
    	if (gameId != null && !"".equals(gameId)) {
    		return gameService.getGameInCacheByGameId(gameId);
        } else {
        	int userId = Integer.parseInt(request.getHeader("userId"));
        	return gameService.getGameInCacheByUserId(userId);
        }
    }
    
    /**
     * 创建多人游戏时使用
     */
    @RequestMapping(value="/cache/games",method=RequestMethod.POST)
    public Object addGameInCache(
            HttpServletRequest request, @RequestBody String body){
    	CacheGame cacheGame = validation.getObject(body, CacheGame.class, new String[]{"metagameId", "duration"});
        int userId = Integer.parseInt(request.getHeader("userId"));
        String gameId = gameService.createGameInCache(cacheGame, userId);
        Map<String, String> response = new HashMap<String, String>();
        response.put("gameId", gameId);
        return response;
    }
    
    @RequestMapping(value="/cache/gameUsers",method=RequestMethod.GET)
    public Object getGameUserInCache (@RequestParam(value="gameId", required=false) String gameId){
        return gameService.getGameUserInCache(gameId);
    }
    
    @RequestMapping(value="/cache/gameUsers",method=RequestMethod.POST)
    public Object addGameUserInCache(
            HttpServletRequest request, @RequestBody String body){
        CacheGame CacheGame = validation.getObject(body, CacheGame.class, new String[]{"gameId"});
        int userId = Integer.parseInt(request.getHeader("userId"));
        gameService.addGameUserInCache(CacheGame.getId(), userId);
        return null;
    }
    
    @RequestMapping(value="/cache/gameUsers",method=RequestMethod.DELETE)
    public Object deleteGameUserInCache(HttpServletRequest request, @RequestParam(value="gameId") String gameId){
        int userId = Integer.parseInt(request.getHeader("userId"));
        gameService.deleteGameUserInCache(gameId, userId);
        return null;
    }

    @RequestMapping(value="/systemGames",method=RequestMethod.POST)
    public Object createSystemGame(HttpServletRequest request){
        gameService.createGameBySystem();
        return null;
    }

    @Deprecated
    @RequestMapping(value="/games",method=RequestMethod.PUT)
    public Object updateGame(HttpServletRequest request, @RequestBody String body){
        Game game = validation.getObject(body, Game.class, new String[]{"id", "metagameId", "duration"});
        gameService.updateGame(game);
        return null;
    }
    
    @Deprecated
    @RequestMapping(value="/gameUsers",method=RequestMethod.GET)
    public Object getGameUsers (@RequestParam(value="gameId", required=false) Integer gameId){
        return gameService.getGameUser(gameId);
    }
    
    @Deprecated
    @RequestMapping(value="/gameUsers",method=RequestMethod.POST)
    public Object addGameUser(
            HttpServletRequest request, @RequestBody String body){
        GameUser gameUser = validation.getObject(body, GameUser.class, new String[]{"gameId"});
        int userId = Integer.parseInt(request.getHeader("userId"));
        gameService.addGameUser(gameUser, userId);
        return null;
    }
    
    @Deprecated
    @RequestMapping(value="/gameUsers",method=RequestMethod.DELETE)
    public Object deleteGameUser(HttpServletRequest request, @RequestParam(value="gameId") int gameId){
        int userId = Integer.parseInt(request.getHeader("userId"));
        gameService.deleteGameUser(gameId, userId);
        return null;
    }

}
