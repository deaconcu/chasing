package com.prosper.chasing.data.service;

import java.util.*;

import com.prosper.chasing.common.interfaces.data.UserTr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import redis.clients.jedis.Jedis;

import com.prosper.chasing.data.bean.CacheGame;
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
import com.prosper.chasing.data.util.Constant.GameState;
import com.prosper.chasing.data.util.Constant.MetagameState;
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
    private UserService userService;
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
    private UserDataMapper userDataMapper;
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
        UserData userData = userDataMapper.selectOne(userId);
        if (userData.getGameId() == -1) {
            return null;
        }
        List<Integer> gameIds = new LinkedList<>();
        gameIds.add(userData.getGameId());
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
     * 系统自动创建游戏，开始的时候，量不会很大，效率低可以接受，之后需要再优化
     * TODO 现在加入队列和创建游戏是两个不同的线程，有可能会有问题，需要修改
     * 单线程运行
     */
    public int createGameBySystem() {
        int count = 0;
        // 查询当前有效的游戏code
        Set<String> metagameCodeSet = new HashSet<>();
        for (Metagame metagame: metagameMapper.selectAll()) {
            metagameCodeSet.add(metagame.getCode());
        }

        log.info("start create game");

        // 获取当前在等待的所有用户，并找到人数大于20的队列
        Map<String, String> userMap = jedis.hgetAll(CacheName.userQueue);
        Map<String, Integer> userCountMap = new HashMap<>();
        for (Map.Entry<String, String> userInfo: userMap.entrySet()) {
            if (userCountMap.containsKey(userInfo.getValue())) {
                userCountMap.put(userInfo.getValue(), userCountMap.get(userInfo.getValue()) + 1);
            } else {
                userCountMap.put(userInfo.getValue(), 1);
            }
        }

        for(Iterator<Map.Entry<String, Integer>> it = userCountMap.entrySet().iterator(); it.hasNext(); ) {
            if (it.next().getValue() < config.minUserCount) {
                it.remove();
            }
        }

        // 获取有效队列的所有用户
        Map<String, List<Integer>> gameMap = new HashMap<>();
        for (Map.Entry<String, String> userInfo: userMap.entrySet()) {
            if (userCountMap.containsKey(userInfo.getValue())) {
                gameMap.putIfAbsent(userInfo.getValue(), new LinkedList<>());
                gameMap.get(userInfo.getValue()).add(Integer.parseInt(userInfo.getKey()));
            }
        }

        // 创建游戏，并设置用户状态
        for(String gameKey: gameMap.keySet()) {
            List<Integer> userList = gameMap.get(gameKey);

            String[] gameInfo = gameKey.split("-");
            String level = gameInfo[0];
            String metaGameCode = gameInfo[1];
            String attendance = "";

            if (!metagameCodeSet.contains(metaGameCode)) {
                for (int i = 0; i < userList.size(); i++) {
                    userQueueService.removeUser(userList.get(i));
                }
                continue;
            }

            List<Integer> singleGameUserList = new LinkedList<>();
            for (int i = 0; i < userList.size(); i++) {
                int userId = userList.get(i);
                if (!isUserInGame(userId)) {
                    singleGameUserList.add(userId);
                    attendance += Integer.toString(userId) + ",";
                }

                if (singleGameUserList.size() == config.minUserCount) {
                    // create game
                    Game game = new Game();
                    game.setMetagameCode(metaGameCode);
                    game.setCreatorId(0);
                    game.setState(GameState.POST_START);
                    game.setServer("");
                    game.setDuration(7000);
                    game.setAttendance(attendance.substring(0, attendance.length() - 1));
                    game.setCreateTime(CommonUtil.getTime(new Date()));
                    game.setUpdateTime(CommonUtil.getTime(new Date()));
                    gameMapper.insert(game);

                    // add game user
                    for (int singleGameUserId : singleGameUserList) {
                        GameUser gameUserInDb = gameUserMapper.selectOneByGameUser(game.getId(), singleGameUserId);
                        if (gameUserInDb != null) {
                            throw new InvalidArgumentException("user is exist");
                        }
                        GameUser gameUser = new GameUser();
                        gameUser.setGameId(game.getId());
                        gameUser.setUserId(singleGameUserId);
                        gameUser.setCreateTime(CommonUtil.getTime(new Date()));
                        gameUserMapper.insert(gameUser);
                        userQueueService.removeUser(singleGameUserId);
                    }
                    userDataMapper.updateUserIntoGame(userList, game.getId(), Constant.UserState.GAMING);
                    log.info("create game success, game id:" + game.getId());
                    count++;
                }
            }
        }
        return count;
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
    public List<UserTr> getGameUser(int gameId) {
        Game gameInDb = gameMapper.selectOne(gameId);
        if (gameInDb == null) {
            throw new InvalidArgumentException("game is not exist");
        }
        String[] userIdStrings = gameInDb.getAttendance().split(",");
        List<Integer> userIdList = new LinkedList<>();
        for (String userId: userIdStrings) {
            userIdList.add(Integer.parseInt(userId));
        }

        List<UserData> userList = new LinkedList<>();
        List<UserData> userDataListForExam = userDataMapper.selectListByIds(userIdList);
        List<User> userInfoList = userMapper.selectListByIds(userIdList);
        List<UserTr> userTrList = new LinkedList<>();
        for (UserData userData: userDataListForExam) {
            //if (userData.getState() == Constant.UserState.GAMING && userData.getGameId() == gameId) {
                UserTr userTr = new UserTr();
                userTr.setId(userData.getId());
                userTr.setGameId(userData.getGameId());
                userTr.setState(userData.getState().byteValue());
                userTr.setDistance(userData.getDistance());
                for (User user: userInfoList) {
                    if (user.getId() == userData.getId()) {
                        userTr.setName(user.getName());
                    }
                }
                userTrList.add(userTr);
            //}
        }
        return userTrList;
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

    /**
     * 获得游戏地图的数据
     */
    public Object getGameMapBytes(String id) {
        byte[] bytes = jedis.get(id.getBytes());
        return Base64.getEncoder().encodeToString(bytes);
    }
}
