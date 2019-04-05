package com.prosper.chasing.game.base;

import com.prosper.chasing.game.util.ByteBuilder;
import com.prosper.chasing.game.util.Constant;
import com.prosper.chasing.game.util.Enums.*;

/**
 * 游戏的开场白数据，包括台词和播放台词时应该聚焦的对象
 * Created by deacon on 2019/3/31.
 */
public class Prologue {

    private PrologueItem[] prologueItems;

    public static class PrologueItem {

        /**
         * 开场白目标类型
         */
        TargetType targetType;

        /**
         * 开场白目标id, 如果id = -1，定义为随机
         */
        int targetId;

        /**
         * 开场白目标位置
         */
        Point3 position;

        /**
         * 开场白的台词
         */
        String[] lines;

        private PrologueItem(TargetType targetType, int targetId, Point3 position, String[] lines) {
            this.targetType = targetType;
            this.targetId = targetId;
            this.position = position;
            this.lines = lines;
        }

        public PrologueItem(TargetType targetType, int targetId, String[] lines) {
            this(targetType, targetId, null, lines);
        }

        public PrologueItem(TargetType targetType, String[] lines) {
            this(targetType, 0, null, lines);
        }

        public PrologueItem(Point3 position, String[] lines) {
            this(TargetType.POSITION, 0, position, lines);
        }

        public void appendBytes(ByteBuilder bb) {
            bb.append(targetType.getValue());
            if (targetType == TargetType.POSITION) {
                bb.append(position.x);
                bb.append(position.y);
                bb.append(position.z);
            } else {
                bb.append(targetId);
            }

            bb.append((byte)lines.length);
            for (String s: lines) {
                bb.append(s.getBytes().length);
                bb.append(s.getBytes());
            }
        }

        public TargetType getTarget() {
            return targetType;
        }

        public int getTargetId() {
            return targetId;
        }

        public void setTargetId(int targetId) {
            this.targetId = targetId;
        }
    }

    public Prologue(PrologueItem[] prologueItems) {
        this.prologueItems = prologueItems;
    }

    public void appendBytes(ByteBuilder bb, int seqId) {

        bb.append(seqId); //seqId
        bb.append(Constant.MessageType.PROLOGUE);
        bb.append((byte)prologueItems.length);

        for (PrologueItem item: prologueItems) {
            item.appendBytes(bb);
        }
    }

    public PrologueItem[] getPrologueItems() {
        return prologueItems;
    }

}
