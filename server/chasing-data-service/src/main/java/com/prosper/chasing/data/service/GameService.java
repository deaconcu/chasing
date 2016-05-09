package com.prosper.chasing.data.service;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import redis.clients.jedis.Jedis;

import com.prosper.chasing.data.bean.Friend;
import com.prosper.chasing.data.bean.Game;
import com.prosper.chasing.data.bean.GameUser;
import com.prosper.chasing.data.bean.Metagame;
import com.prosper.chasing.data.bean.MetagameType;
import com.prosper.chasing.data.bean.User;
import com.prosper.chasing.data.bean.UserData;
import com.prosper.chasing.data.exception.InvalidArgumentException;
import com.prosper.chasing.data.exception.OperationNotAllowedException;
import com.prosper.chasing.data.exception.ResourceNotExistException;
import com.prosper.chasing.data.mapper.FriendMapper;
import com.prosper.chasing.data.mapper.GameMapper;
import com.prosper.chasing.data.mapper.GameUserMapper;
import com.prosper.chasing.data.mapper.MetagameMapper;
import com.prosper.chasing.data.mapper.MetagameTypeMapper;
import com.prosper.chasing.data.mapper.UserDataMapper;
import com.prosper.chasing.data.mapper.UserMapper;
import com.prosper.chasing.data.util.CommonConfig;
import com.prosper.chasing.data.util.Constant.CacheName;
import com.prosper.chasing.data.util.Constant.FriendState;
import com.prosper.chasing.data.util.Constant.FriendType;
import com.prosper.chasing.data.util.Constant.GameState;
import com.prosper.chasing.data.util.Constant.MetagameState;
import com.prosper.chasing.common.interfaces.data.GameDataService;
import com.prosper.chasing.common.util.CommonUtil;

/**
 * @author Deacon
 */
@Service
public class GameService {

    @Autowired
    private MetagameMapper metagameMapper;
    @Autowired
    private MetagameTypeMapper metagameTypeMapper;
    @Autowired
    private GameUserMapper gameUserMapper;
    @Autowired
    private GameMapper gameMapper;
    @Autowired
    private FriendMapper friendMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private Jedis jedis;
    @Autowired
    private CommonConfig commonConfig;

    /**
     * 插入一条metagame
     */
    public void addMetagame(Metagame metagame) {
        String[] types = metagame.getTypes().split(",");
        String typesString = "";
        for (String typeString: types) {
            int type;
            try {
                type = Integer.parseInt(typeString);
            } catch(Exception e) {
                throw new InvalidArgumentException("type is not int");
            }
            if (metagameTypeMapper.selectOne(type) == null) {
                throw new InvalidArgumentException("type is not exist");
            }
            typesString += Integer.toString(type) + ",";
        }
        if (!"".equals(typesString)) {
            typesString = typesString.substring(0, typesString.length() - 1);
        }
        metagame.setState(MetagameState.NORMAL);
        metagame.setTypes(typesString);
        metagame.setCreateTime(CommonUtil.getTime(new Date()));
        metagame.setUpdateTime(CommonUtil.getTime(new Date()));
        metagameMapper.insert(metagame);
    }

    /**
     * 更新metagame
     */
    public void updateMetagame(Metagame metagame) {
        String[] types = metagame.getTypes().split(",");
        String typesString = "";
        for (String typeString: types) {
            int type;
            try {
                type = Integer.parseInt(typeString);
            } catch(Exception e) {
                throw new InvalidArgumentException("type is not int");
            }
            if (metagameTypeMapper.selectOne(type) == null) {
                throw new InvalidArgumentException("type is not exist");
            }
            typesString += Integer.toString(type) + ",";
        }
        if (!"".equals(typesString)) {
            typesString = typesString.substring(0, typesString.length() - 1);
        }
        metagame.setUpdateTime(CommonUtil.getTime(new Date()));
        metagameMapper.update(metagame);
    }

    /**
     * 分页获取metagame列表
     */
    public List<Metagame> getMetagames(int page, int pageLength) {
        List<Metagame> metagameList = metagameMapper.selectListByPage(pageLength, pageLength * (page - 1));
        return getTypeForMetagame(metagameList);
    }

    /**
     * 通过id获取metagame列表
     */
    public List<Metagame> getMetagames(List<Integer> idList) {
        List<Metagame> metagameList = metagameMapper.selectListByIds(idList);
        return getTypeForMetagame(metagameList);
    }

    /**
     * 往metagame里边塞gametype信息
     */
    private List<Metagame> getTypeForMetagame(List<Metagame> metagameList) {
        List<MetagameType> allTypeList = metagameTypeMapper.selectAll();
        Map<Integer, MetagameType> typeMap = new HashMap<>();
        for (MetagameType type: allTypeList) {
            typeMap.put(type.getId(), type);
        }
        for (Metagame metagame: metagameList) {
            List<MetagameType> typeList = new LinkedList<MetagameType>();
            String[] types = metagame.getTypes().split(",");
            for (String typeString: types) {
                int typeId = Integer.parseInt(typeString);
                if (typeMap.containsKey(typeId)) {
                    typeList.add(typeMap.get(typeId));
                }
            }
            metagame.setMetagameTypeList(typeList);
        }
        return metagameList;
    }
    
    /**
     * 根据状态获取游戏列表
     */
    public List<Game> getGame(int state, int page, int pageLength) {
        return gameMapper.selectListByStatePage(state, pageLength, pageLength * (page - 1));
    }

    /**
     * 创建一个新游戏
     */
    public void addGame(Game game, Integer userId) {
        GameUser gameUser = gameUserMapper.selectOneByUserId(userId);
        if (gameUser != null) {
            throw new OperationNotAllowedException("user is in game");
        }
        if (game.getDuration() > commonConfig.maxGameDuration || game.getDuration() < commonConfig.minGameDuration) {
            throw new InvalidArgumentException("duration is not in range");
        }
        game.setCreatorId(userId);
        game.setState(GameState.CREATE);
        game.setCreateTime(CommonUtil.getTime(new Date()));
        game.setUpdateTime(CommonUtil.getTime(new Date()));
        gameMapper.insert(game);
    }

    /**
     * 修改游戏，包括游戏状态和游戏时间
     */
    public void updateGame(Game game) {
        if (game.getDuration() > commonConfig.maxGameDuration || game.getDuration() < commonConfig.minGameDuration) {
            throw new InvalidArgumentException("duration is not in range");
        }
        Game gameInDb = gameMapper.selectOne(game.getId());
        if (gameInDb.getState() != GameState.CREATE) {
            throw new InvalidArgumentException("invalid state");
        }
        gameInDb.setDuration(game.getDuration());
        gameInDb.setState(game.getState());
        gameInDb.setUpdateTime(CommonUtil.getTime(new Date()));
        gameMapper.update(gameInDb);
    }

    /**
     * 新建一个游戏用户
     */
    public void addGameUser(GameUser gameUser, int userId) {
        Game gameInDb = gameMapper.selectOne(gameUser.getGameId());
        if (gameInDb == null) {
            throw new InvalidArgumentException("game is not exist");
        }
        GameUser gameUserInDb = gameUserMapper.selectOneByGameUser(gameUser.getGameId(), userId);
        if (gameUserInDb != null) {
            throw new InvalidArgumentException("user is exist");
        }
        gameUser.setUserId(userId);
        gameUser.setCreateTime(CommonUtil.getTime(new Date()));
        gameUserMapper.insert(gameUser);
    }

    /**
     * 删除一个游戏用户
     */
    public void deleteGameUser(int gameId, int userId) {
        Game gameInDb = gameMapper.selectOne(gameId);
        if (gameInDb == null || gameInDb.getState() != GameState.CREATE) {
            throw new InvalidArgumentException("game is not exist or game state is not valid");
        }
        GameUser gameUserInDb = gameUserMapper.selectOneByGameUser(gameId, userId);
        if (gameUserInDb == null) {
            throw new InvalidArgumentException("user is not exist");
        }
        gameUserMapper.delete(gameUserInDb.getId());
    }

    /**
     * 获取游戏中的用户
     */
    public List<User> getGameUser(int gameId) {
        Game gameInDb = gameMapper.selectOne(gameId);
        if (gameInDb == null) {
            throw new InvalidArgumentException("game is not exist");
        }
        List<Integer> userList = gameUserMapper.selectUserListByGameId(gameId);
        return userMapper.selectListByIds(userList);
    }

    /**
     * 添加一个游戏类型
     */
    public void addGameType(MetagameType metagameType) {
        metagameType.setCreateTime(CommonUtil.getTime(new Date()));
        metagameType.setUpdateTime(CommonUtil.getTime(new Date()));
        metagameTypeMapper.insert(metagameType);
    }

    /**
     * 修改一个游戏类型
     */
    public void putGameType(MetagameType metagameType) {
        metagameType.setUpdateTime(CommonUtil.getTime(new Date()));
        metagameTypeMapper.update(metagameType);
    }

    

}
