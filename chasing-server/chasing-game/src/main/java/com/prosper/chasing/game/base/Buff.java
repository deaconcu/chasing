package com.prosper.chasing.game.base;

/**
 * Created by deacon on 2018/1/1.
 */
public class Buff {

    public static class BaseBuff {
        byte id;
        int startSecond; // 起始时间
        short last;      // 持续时间

        public BaseBuff(byte id, short last) {
            this.id = id;
            this.startSecond = (int)(System.currentTimeMillis() / 1000);
            this.last = last;
        }

        public int getRemainSecond() {
            return (int) (System.currentTimeMillis() / 1000 - (startSecond + last));
        }
    }

    public static class ChasingBuff extends BaseBuff {
        public static final byte USER = 1;
        public static final byte NPC = 2;

        public byte type;
        public int targetId;

        public ChasingBuff(byte id, short last, byte type, int targetId) {
            super(id, last);
            if (type != USER && type != NPC) {
                throw new RuntimeException("chasing buff type is not invalid: type:" + type);
            }
            this.type = type;
            this.targetId = targetId;
        }
    }

}
