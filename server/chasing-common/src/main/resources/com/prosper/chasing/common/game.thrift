namespace java com.prosper.chasing.common.interfaces

exception GameException {
  1: i32 exCode,
  2: string desc
}

struct Message {
}

service GameService {
   
   bool createGame(1:binary bs) throws (1:GameException ex),
   
   bool closeGame(1:string gameId) throws (1:GameException ex),
   
   void sendData(1:Message message) throws (1:GameException ex),

}

