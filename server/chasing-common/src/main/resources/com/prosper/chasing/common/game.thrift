namespace java com.prosper.chasing.common.interfaces

exception GameException {
  1: i32 exCode,
  2: string desc
}

service GameMessageService {
   void send(1:binary bs) throws (1:GameException ex),
}

