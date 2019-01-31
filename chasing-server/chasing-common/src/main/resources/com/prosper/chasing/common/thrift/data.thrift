namespace java com.prosper.chasing.common.interfaces.data

exception GameException {
  1: i32 exCode,
  2: string desc
}

struct GameTr {
    1: i32 id,
    2: string metagameCode,
    3: i32 duration,
    4: i8 state,
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
    5: i8 state,
    6: string types,
    7: string createTime,
    8: string updateTime,
}

struct UserTr {
    1: i32 id,
    2: string name,
    3: i32 distance,
    4: i8 roleType,
    5: i32 hill,
    6: i32 river,
    7: i8 state,
    8: i32 gameId,
    9: i32 steps
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

    /*
     * 获取用户进入的游戏id
     * @param gameId: 游戏ID
     */
    i32 getUserGame(1: i32 userId),
}

service UserDataService {
    /*
     * 获取用户信息
     * @param userId: 用户ID
     */
    UserTr getUser(1: i32 userId),
   
    /*
     * 更新用户信息
     * @param user: 用户
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

service WrapperService {
    /*
     * 更新用户信息和道具信息
     * @param user: 用户
     * @param usedUserPropList: 使用过的用户道具
     */
    void updateUserProp(1: UserTr user, 2: list<UserPropTr> usedUserPropList)
}

