package com.prosper.chasing.game.base;

import com.prosper.chasing.game.util.ByteBuilder;
import com.prosper.chasing.game.util.Enums;

import java.util.Map;

/**
 * Created by deacon on 2019/3/18.
 */
public class InteractiveObjects {

    public static abstract class InteractiveObject extends GameObject implements Interactive {

        private static int id = 0;

        public static Map<Enums.InteractiveType, boolean[][]> transferMatrixMap;

        private int currentState;

        static void init(Enums.InteractiveType type, int stateSize) {
            transferMatrixMap.put(type, new boolean[stateSize][stateSize]);
        }

        InteractiveObject(Point3 position, int rotateY) {
            super(id ++, position, rotateY);
        }

        void allowTransfer(int fromState, int toState) {
            transferMatrixMap.get(getType())[fromState][toState] = true;
        }

        boolean isAllowed(int fromState, int toState) {
            return transferMatrixMap.get(getType())[fromState][toState];
        }

        public boolean change(int fromState, int toState) {
            if (!isAllowed(fromState, toState)) return false;
            setCurrentState(toState);
            return true;
        }

        public void appendPrefixBytes(ByteBuilder byteBuilder) {
            byteBuilder.append(Enums.GameObjectType.INTERACTIVE.getValue());
            byteBuilder.append(getId());
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
            byteBuilder.append(getRotateY());
        }

        public int getCurrentState() {
            return currentState;
        }

        public void setCurrentState(int currentState) {
            this.currentState = currentState;
        }

        abstract Enums.InteractiveType getType();

        abstract void interact(Game game, User user, byte state);
    }

    public static class River extends InteractiveObject {

        public static byte NO_BRIDGE = 1;
        public static byte WITH_BRIDGE = 2;

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

        @Override
        void interact(Game game, User user, byte state) {
            // todo need prop bridge
            change(getCurrentState(), state);
        }
    }

    public static class Stone extends InteractiveObject {

        public static byte INTACT = 1;
        public static byte BROKEN = 2;

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
            return Enums.InteractiveType.RIVER;
        }

        @Override
        void interact(Game game, User user, byte state) {
            change(getCurrentState(), state);
        }
    }

    public static class Gate extends InteractiveObject {

        public static byte CLOSE = 1;
        public static byte OPEN = 2;

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
            return Enums.InteractiveType.RIVER;
        }

        @Override
        void interact(Game game, User user, byte state) {
            change(getCurrentState(), state);
        }
    }

    public static class FireFence extends InteractiveObject {

        public static byte FIRE_ON = 1;
        public static byte FIRE_PUT_OUT = 2;

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
            return Enums.InteractiveType.RIVER;
        }

        @Override
        void interact(Game game, User user, byte state) {
            change(getCurrentState(), state);
        }
    }

    public static class SignPost extends InteractiveObject {

        public static byte DIRECTION_1 = 1;
        public static byte DIRECTION_2 = 2;
        public static byte DIRECTION_3 = 3;

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
            return Enums.InteractiveType.RIVER;
        }

        @Override
        void interact(Game game, User user, byte state) {
            change(getCurrentState(), state);
        }
    }
}
