package com.prosper.chasing.data.service;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import redis.clients.jedis.Jedis;

import com.prosper.chasing.data.bean.CacheGame;
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
import com.prosper.chasing.data.util.Config;
import com.prosper.chasing.data.util.Constant;
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

    private Logger log = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private UserQueueService userQueueService;
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
    private Config config;

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
     * 获取某个用户加入的游戏
     */
    public Game getGameByUser(int userId) {
        GameUser gameUser = gameUserMapper.selectOneByUserId(userId);
        if (gameUser == null) {
            return null;
        }
        List<Integer> gameIds = new LinkedList<>();
        gameIds.add(gameUser.getGameId());
        return gameMapper.selectListByIds(gameIds).get(0);
    }

    /**
     * 按用户id获取缓存中的游戏信息
     */
    public CacheGame getGameInCacheByUserId(int userId) {
        String userGameKey = Constant.CacheName.userGame + Integer.toString(userId);
        String gameCacheId = jedis.get(userGameKey);
        if (gameCacheId == null) {
            throw new ResourceNotExistException("game is not exist");
        }
        String gameKey = Constant.CacheName.game + gameCacheId;
        String gameUserKey = Constant.CacheName.gameUser + gameCacheId;

        String info = jedis.get(gameKey);
        if (info == null) {
            jedis.del(userGameKey);
            jedis.del(gameUserKey);
            throw new ResourceNotExistException("game is not exist");
        }

        CacheGame cacheGame = new CacheGame(info);
        if (!jedis.exists(gameUserKey)) {
            jedis.del(userGameKey);
            jedis.del(gameKey);
            throw new InvalidArgumentException("game is not exist");
        }

        Set<String> userIds = jedis.smembers(gameUserKey);
        cacheGame.setUsers(userIds);
        cacheGame.setId(gameCacheId);
        return cacheGame;
    }

    /**
     * 按游戏id获取缓存中的游戏信息
     */
    public CacheGame getGameInCacheByGameId(String gameCacheId) {
        String gameKey = Constant.CacheName.game + gameCacheId;
        String gameUserKey = Constant.CacheName.gameUser + gameCacheId;

        String info = jedis.get(gameKey);
        if (info == null) {
            jedis.del(gameUserKey);
            throw new ResourceNotExistException("game is not exist");
        }

        CacheGame cacheGame = new CacheGame(info);
        if (!jedis.exists(gameUserKey)) {
            jedis.del(gameKey);
            throw new InvalidArgumentException("game is not exist");
        }

        Set<String> userIds = jedis.smembers(gameUserKey);
        cacheGame.setUsers(userIds);
        cacheGame.setId(gameCacheId);
        return cacheGame;
    }

    /**
     * 在缓存中主动创建一个新游戏
     */
    public String createGameInCache(CacheGame cacheGame, Integer userId) {
        GameUser gameUser = gameUserMapper.selectOneByUserId(userId);
        if (gameUser != null) {
            throw new OperationNotAllowedException("user is in game");
        }
        if (cacheGame.getDuration() > config.maxGameDuration || cacheGame.getDuration() < config.minGameDuration) {
            throw new InvalidArgumentException("duration is not in range");
        }

        String userGameKey = Constant.CacheName.userGame + userId.toString();
        String gameId = jedis.get(userGameKey);
        if (gameId != null) {
            throw new OperationNotAllowedException("user is join another game");
        }

        String gameCacheId = UUID.randomUUID().toString();
        String gameKey = Constant.CacheName.game + gameCacheId;
        jedis.set(gameKey, cacheGame.toString());

        String gameUserKey = Constant.CacheName.gameUser + gameCacheId;
        String value = userId.toString();
        jedis.sadd(gameUserKey, value);

        jedis.set(userGameKey, gameCacheId);

        return gameCacheId;
    }


    /**
     * 系统自动创建游戏
     * 单线程运行
     */
    public void createGameBySystem() {
        log.info("start create game");

        String userListKey = CacheName.systemUserList + "0";
        List<Integer> userList = new LinkedList<>();
        if (jedis.llen(userListKey) >= config.minUserCount) {
            for (int i = 0; i < config.minUserCount; i++) {
                int userId = Integer.parseInt(jedis.lindex(userListKey, i));
                if (!isUserInGame(userId)) {
                    userList.add(userId);
                }
            }
        }
        
        if (userList.size() == config.minUserCount) {
            // create game
            Game game = new Game();
            game.setMetagameId(1);
            game.setCreatorId(0);
            game.setState(GameState.POST_START);
            game.setServer("");
            game.setDuration(900);
            game.setCreateTime(CommonUtil.getTime(new Date()));
            game.setUpdateTime(CommonUtil.getTime(new Date()));
            gameMapper.insert(game);

            // add game user
            for (int userId: userList) {
                GameUser gameUserInDb = gameUserMapper.selectOneByGameUser(game.getId(), userId);
                if (gameUserInDb != null) {
                    throw new InvalidArgumentException("user is exist");
                }
                GameUser gameUser = new GameUser();
                gameUser.setGameId(game.getId());
                gameUser.setUserId(userId);
                gameUser.setCreateTime(CommonUtil.getTime(new Date()));
                gameUserMapper.insert(gameUser);

                // remove game user from cache list
                userQueueService.removeUser(0, userId);
            }
            log.info("create game success, game id:" + game.getId());
        }
    }

    /**
     * 检查用户是否已开始游戏
     * @// TODO: 2017/2/15
     */
    private boolean isUserInGame(long userId) {
        return false;
    }

    /**
     * 获取缓存游戏中的游戏玩家
     */
    public Set<String> getGameUserInCache(String gameCacheId) {
        String gameUserKey = Constant.CacheName.gameUser + gameCacheId;
        if (!jedis.exists(gameUserKey)) {
            throw new InvalidArgumentException("game is not exist");
        }

        Set<String> userIds = jedis.smembers(gameUserKey);
        return userIds;
    }

    /**
     * 在缓存游戏中增加一个游戏用户
     */
    public void addGameUserInCache(String gameCacheId, Integer userId) {
        String userGameKey = Constant.CacheName.userGame + userId.toString();
        String gameId = jedis.get(userGameKey);
        if (gameId != null) {
            throw new OperationNotAllowedException("user is join another game");
        }

        String gameKey = Constant.CacheName.game + gameCacheId;
        if (jedis.exists(gameKey)) {
            jedis.sadd(gameKey, userId.toString());
        }
    }

    /**
     * 在缓存游戏中去掉一个游戏用户
     */
    public void deleteGameUserInCache(String gameCacheId, Integer userId) {
        String gameKey = Constant.CacheName.game + gameCacheId;
        if (jedis.exists(gameKey)) {
            String info = jedis.get(gameKey);
            CacheGame cacheGame = new CacheGame(info);
            if (cacheGame.getCreatorId() == userId) {
                // remove user game info
                Set<String> userIds = jedis.smembers(gameKey);
                for (String gameUserId: userIds) {
                    String userGameKey = Constant.CacheName.userGame + gameUserId;
                    jedis.del(userGameKey);
                }

                // remove game user info
                String gameUserKey = Constant.CacheName.gameUser + gameCacheId;
                jedis.del(gameUserKey);

                // remove game info
                jedis.del(gameKey);
            } else {
                String userGameKey = Constant.CacheName.userGame + userId.toString();
                jedis.del(userGameKey);

                String gameUserKey = Constant.CacheName.gameUser + gameCacheId;
                jedis.srem(gameUserKey, userId.toString());
            }
        }
    }

    /**
     * 创建一个新游戏
     * @deprecated
     */
    public void addGameOld(Game game, Integer userId) {
        GameUser gameUser = gameUserMapper.selectOneByUserId(userId);
        if (gameUser != null) {
            throw new OperationNotAllowedException("user is in game");
        }
        if (game.getDuration() > config.maxGameDuration || game.getDuration() < config.minGameDuration) {
            throw new InvalidArgumentException("duration is not in range");
        }
        game.setCreatorId(userId);
        game.setState(GameState.CREATE);
        game.setServer("");
        game.setCreateTime(CommonUtil.getTime(new Date()));
        game.setUpdateTime(CommonUtil.getTime(new Date()));
        gameMapper.insert(game);
    }

    /**
     * TODO 不确定update list是否是原子操作，需要再优化
     * 领取一个游戏
     */
    public List<Game> claimGame(String ip, int port, int count) {
        if (count < 0 || count > 1000) {
            throw new InvalidArgumentException("count is invalid, count:" + count);
        }
        String server = ip + ":" + port;
        List<Game> gameList = gameMapper.selectListByStateAndServer(GameState.LOADING, server, 100);
        if (gameList.size() > 0) {
            return gameList;
        }

        gameList = gameMapper.selectListByStatePage(GameState.POST_START, 1, 0);
        if (gameList.size() > 0) {
            gameMapper.updateGameByState(
                    GameState.POST_START, GameState.LOADING, server, CommonUtil.getTime(new Date()), count);
        }

        gameList = gameMapper.selectListByStateAndServer(GameState.LOADING, server, 100);
        if (gameList.size() > 0) {
            return gameList;
        }
        return new LinkedList<>();
    }

    /**
     * 修改游戏，包括游戏状态和游戏时间
     */
    @Deprecated
    public void updateGame(Game game) {
        if (game.getDuration() > config.maxGameDuration || game.getDuration() < config.minGameDuration) {
            throw new InvalidArgumentException("duration is not in range");
        }
        Game gameInDb = gameMapper.selectOne(game.getId());

        // TODO check if state change is legal

        gameInDb.setDuration(game.getDuration());
        gameInDb.setState(game.getState());
        gameInDb.setUpdateTime(CommonUtil.getTime(new Date()));
        gameMapper.update(gameInDb);
    }

    /**
     *  为一个游戏增加一个游戏用户
     */
    @Deprecated
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
