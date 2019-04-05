package com.prosper.chasing.game.base;

import com.prosper.chasing.game.util.ByteBuilder;
import com.prosper.chasing.game.util.Enums;

import java.util.HashMap;
import java.util.Map;

/**
 * 所有可以和用户交互的游戏对象称为Interactive Object,
 * 它们有自己的逻辑，需要在game中执行
 * 有自己的状态，可以接受用户的命令改变状态
 * Created by deacon on 2019/3/18.
 */
public class InteractiveObjects {

    public static abstract class Interactive extends GameObject {

        /**
         * 生成对象的ID顺序号，从1开始，在有些场景中，定义0为随机id，-1为对象不存在
         */
        private static int id = 1;

        public static Map<Enums.InteractiveType, boolean[][]> transferMatrixMap = new HashMap<>();

        private byte currentState;

        static void init(Enums.InteractiveType type, int stateSize) {
            transferMatrixMap.put(type, new boolean[stateSize][stateSize]);
        }

        Interactive(Point3 position, int rotateY) {
            super(id ++, position, rotateY);
        }

        void allowTransfer(int fromState, int toState) {
            transferMatrixMap.get(getType())[fromState][toState] = true;
        }

        boolean isAllowed(int fromState, int toState) {
            return transferMatrixMap.get(getType())[fromState][toState];
        }

        @Override
        public Enums.GameObjectType getObjectType() {
            return Enums.GameObjectType.INTERACTIVE;
        }

        public void appendBornBytes(ByteBuilder byteBuilder) {
            byteBuilder.append(getType().getValue());
            byteBuilder.append(getCurrentState());
            byteBuilder.append(getPoint3().x);
            byteBuilder.append(getPoint3().y);
            byteBuilder.append(getPoint3().z);
            byteBuilder.append(getRotateY());
        }

        public void appendAliveBytes(ByteBuilder byteBuilder) {
            byteBuilder.append(getCurrentState());
        }

        public byte getCurrentState() {
            return currentState;
        }

        public void setCurrentState(byte currentState) {
            this.currentState = currentState;
        }

        abstract Enums.InteractiveType getType();

        public void logic(Game game){}

        public boolean interact(Game game, User user, byte state) {
            byte currentState =  getCurrentState();
            if (!isAllowed(getCurrentState(), state)) return false;
            setCurrentState(state);
            return true;
        }

        @Override
        public String toString() {
            return "[id:" + getId() + ", type:" + getType() + ", position:" + getPoint3().toString() + "]";
        }
    }

    public static class River extends Interactive {

        public static byte NO_BRIDGE = 0;
        public static byte WITH_BRIDGE = 1;

        static {
            init(Enums.InteractiveType.RIVER, 2);
        }

        public River(Point3 position, int rotateY) {
            super(position, rotateY);
            allowTransfer(NO_BRIDGE, WITH_BRIDGE);
            setCurrentState(NO_BRIDGE);
        }

        @Override
        Enums.InteractiveType getType() {
            return Enums.InteractiveType.RIVER;
        }

    }

    public static class Stone extends Interactive {

        public static byte INTACT = 0;
        public static byte BROKEN = 1;

        static {
            init(Enums.InteractiveType.STONES, 2);
        }

        public Stone(Point3 position, int rotateY) {
            super(position, rotateY);
            allowTransfer(INTACT, BROKEN);
            setCurrentState(INTACT);
        }

        @Override
        Enums.InteractiveType getType() {
            return Enums.InteractiveType.STONES;
        }
    }

    public static class Gate extends Interactive {

        public static byte CLOSE = 0;
        public static byte OPEN = 1;

        static {
            init(Enums.InteractiveType.GATE, 2);
        }

        public Gate(Point3 position, int rotateY) {
            super(position, rotateY);
            allowTransfer(CLOSE, OPEN);
            setCurrentState(CLOSE);
        }

        @Override
        Enums.InteractiveType getType() {
            return Enums.InteractiveType.GATE;
        }

    }

    public static class FireFence extends Interactive {

        public static byte FIRE_ON = 0;
        public static byte FIRE_PUT_OUT = 1;

        static {
            init(Enums.InteractiveType.FIRE_FENCE, 2);
        }

        public FireFence(Point3 position, int rotateY) {
            super(position, rotateY);
            allowTransfer(FIRE_ON, FIRE_PUT_OUT);
            setCurrentState(FIRE_ON);
        }

        @Override
        Enums.InteractiveType getType() {
            return Enums.InteractiveType.FIRE_FENCE;
        }
    }

    public static class SignPost extends Interactive {

        public static byte DIRECTION_1 = 0;
        public static byte DIRECTION_2 = 1;
        public static byte DIRECTION_3 = 2;

        static {
            init(Enums.InteractiveType.SIGNPOST, 3);
        }

        public SignPost(Point3 position, int rotateY) {
            super(position, rotateY);
            allowTransfer(DIRECTION_1, DIRECTION_2);
            allowTransfer(DIRECTION_1, DIRECTION_3);
            allowTransfer(DIRECTION_2, DIRECTION_1);
            allowTransfer(DIRECTION_2, DIRECTION_3);
            allowTransfer(DIRECTION_3, DIRECTION_1);
            allowTransfer(DIRECTION_3, DIRECTION_2);
            setCurrentState(DIRECTION_1);
        }

        @Override
        Enums.InteractiveType getType() {
            return Enums.InteractiveType.SIGNPOST;
        }
    }

    public static class Tent extends Interactive {

        private Point3 enterPosition;

        public Tent(Point3 position, int rotateY, Point3 enterPosition) {
            super(position, rotateY);
            this.enterPosition = enterPosition;
        }

        @Override
        Enums.InteractiveType getType() {
            return Enums.InteractiveType.TENT;
        }

        @Override
        public void logic(Game game) {
            // TODO 需要优化, 效率太低，每帧都需要遍历
            for (User user: game.getUserMap().values()) {
                if (!isNear(user, 500)) continue;
                Interactive interactive = game.getRandomInteractive(Enums.InteractiveType.TENT, this);
                if (interactive == null) continue;

                Tent tent = (Tent)interactive;
                user.resetPoint(tent.enterPosition);
            }
        }
    }
}
