namespace java com.prosper.chasing.common.interfaces.data

exception GameException {
  1: i32 exCode,
  2: string desc
}

struct GameTr {
    1: i32 id,
    2: i32 metagameId,
    3: i32 duration,
    4: byte state,
    5: i32 creatorId,
    6: string server,
    7: string startTime,
    8: string createTime,
    9: string updateTime,
}

struct MetagameTr {
    1: i32 id,
    2: string code,
    3: string name,
    4: i32 duration,
    5: byte state,
    6: string types,
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
     * 获取游戏
     */
    list<GameTr> ClaimGame(1: string ip, 2: i32 port, 3: i32 count),
   
    /*
     * 更新游戏
     * @param game: 需要更新的游戏数据
     */
    void updateGame(1: GameTr game),

    /*
     * 获取游戏用户列表
     * @param gameId: 游戏ID
     */
    list<UserTr> getGameUsers(1: i32 gameId),
}

service MetagameDataService {

    /*
     * 获取元游戏列表
     * @param metagameId: 元游戏ID
     */
    list<MetagameTr> getMetagame(1: list<i32> metagameIdList),
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

