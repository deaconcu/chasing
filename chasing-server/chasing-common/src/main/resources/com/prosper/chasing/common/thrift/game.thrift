namespace java com.prosper.chasing.common.interfaces.game

exception GameException {
  1: i32 exCode,
  2: string desc
}

service GameService {
   
   void executeData(1: i32 gameId, 2: i32 userId, 3:binary message) throws (1:GameException ex)

}

