namespace java com.prosper.chasing.common.interfaces.data

exception GameException {
  1: i32 exCode,
  2: string desc
}

struct GameTr {
    1: i32 id,
    2: string metagameId,
    3: i32 duration,
    4: byte state,
    5: i32 creatorId,
    6: string startTime,
    7: string createTime,
    8: string updateTime,
}

struct UserTr {
    1: i32 id,
    2: i32 distance,
    3: i32 road,
    4: i32 hill,
    5: i32 river,
}

struct UserPropTr {
    1: i32 propId,
    2: i16 count
}

service GameDataService {
    /*
     * 获取游戏列表
     * @param state: 游戏的状态，state=0表示查询全部
     * @param page: 页码
     * @param pageLength: 页长 
     */
    list<GameTr> getGames(1: i32 state, 2: i32 page, 3:i32 pageLength),
   
    /*
     * 更新游戏
     * @param game: 需要更新的游戏数据
     */
    void updateGame(1: GameTr game),

    /*
     * 获取游戏用户列表
     * @param gameId: 游戏ID
     */
    list<GameTr> getGameUsers(1: i32 gameId),
}
    
service UserDataService {
    /*
     * 获取用户信息
     * @param userId: 用户ID
     */
    UserTr getUser(1: i32 userId),
   
    /*
     * 更新用户信息
     * @param userId: 用户ID
     */
    void updateUser(1: UserTr user),
}

service PropDataService {
    /*
     * 获取用户道具信息
     * @param userId: 用户ID
     */
    list<UserPropTr> getUserProp(1: i32 userId),
   
    /*
     * 更新用户道具信息
     * @param usedUserPropList: 使用过的用户道具
     */
    void updateUserProp(1: i32 userId, 2: list<UserPropTr> usedUserPropList)
}

