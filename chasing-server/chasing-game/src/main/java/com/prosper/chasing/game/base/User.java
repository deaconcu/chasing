package com.prosper.chasing.game.base;

import com.prosper.chasing.game.util.ByteBuilder;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class User {


    public static class UserState {
        public static int LOADED = 1;   // 加载完成状态
        public static int ACTIVE = 2;   // 活动状态
        public static int QUITING = 3;  // 正在退出
        public static int QUIT = 4;     // 已退出
    }

    // 用户所属的游戏
    private Game game;
    
    // 用户id
    private int id;
    
    // 用户位置
    private Position position;

    // 出生位置
    private Position initPosition;

    // 速度
    private int speed;

    // 生命值
    private short life;
    
    // 所拥有的道具
    private Map<Byte, Byte> propMap;
    
    // Buff Map
    private Map<Byte, Buff> buffMap;
    
    // 用户状态 @see #User.UserState
    private int state;

    // 位置是否有修改
    boolean isPositionChanged = false;

    // 生命值是否有修改
    boolean isLifeChanged = false;

    // 速度是否有修改
    boolean isSpeedChanged = false;

    boolean isStateChanged = false;

    // 动作列表
    List<User.Action> actionList;

    // buff变化列表
    Set<Byte> buffChangedSet;

    // 道具变化列表
    Set<Byte> propChangedSet;

    // buff类
    public static class Buff {
        byte id;
        int startSecond; // 起始时间
        short last;      // 持续时间

        public int getRemainSecond() {
            return (int) (System.currentTimeMillis() / 1000 - (startSecond + last));
        }
    }

    // 动作类
    private static class Action {
        short actionId;
        byte code;  // 动作结果标识符 0:接受方，1:成功，2-255:失败
        byte type; // 0:不需要显示影响的类型 1: 生命值变化
        Object value;

        public Action(short actionId, byte code, byte type, Object value) {
            this.code = code;
            this.actionId = actionId;
            this.type = type;
            this.value = value;
        }
    }

    public void addAction(short actionId, byte code, byte type, Object value) {
        if (actionId > 1000) {
            actionList.add(new Action(actionId, code, type, value));
        }
    }

    // 检查道具是否满足要求的数量
    public boolean checkProp(int propId, byte need) {
        Byte count = propMap.get(propId);
        if (count == null) {
            return false;
        }
        if (count < need) {
            return false;
        }
        return true;
    }
    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        if (position.equals(this.position)) return;
        this.position = position;
        isPositionChanged = true;
    }

    public Position getInitPosition() {
        return initPosition;
    }

    public void setInitPosition(Position initPosition) {
        this.initPosition = initPosition;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        if (this.speed == speed) return;
        this.speed = speed;
        isSpeedChanged = true;
    }

    public short getLife() {
        return life;
    }

    public void setLife(short life) {
        if (this.life == life) return;
        this.life = life;
        isLifeChanged = true;
    }

    public Map<Byte, Byte> getPropMap() {
        return propMap;
    }

    public void setPropMap(Map<Byte, Byte> propMap) {
        this.propMap = propMap;
    }

    public void setProp(byte propId, byte count) {
        propMap.put(propId, count);
        propChangedSet.add(propId);
    }

    public Map<Byte, Buff> getBuffMap() {
        return buffMap;
    }

    public void setBuff(Buff buff) {
        if (buffMap.get(buff.id) != null) {
            Buff exist = buffMap.get(buff.id);
            exist.last = buff.last;
            exist.startSecond = buff.startSecond;
        } else {
            buffMap.put(buff.id, buff);
        }
        buffChangedSet.add(buff.id);
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
        this.isStateChanged = true;
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    /**
     * 用32个bit位表示buff
     */
    private int getBuffBytes() {
        int value = 0;
        for (Buff buff: buffMap.values()) {
            value = value | (1 << (buff.id - 1));
        }
        return value;
    }

    /**
     * 将修改写成byte[]，用来同步客户端用户数据
     * 格式：
     * state(2)
     * envPropCount(2)|list<EnvProp>|
     * actionCount(2)|list<Action>
     * moveState(1)|positionX(4)|positionY(4)|positionZ(4)|rotateY(4)
     * lifeValue(2)
     * speedValue(4)
     * buffCount(1)|list<Buff>
     * propCount(1)|list<Prop>
     * userCount(1)|list<UserPostion>
     * userCount(1)|list<UserBuff>
     *
     * state: reserved(7bit)|envProp(1bit)|action(1bit)|position(1bit)|life(1bit)|speed(1bit)|buff(1bit)|
     *        prop(1bit)|otherUserPosition(1bit)|otherUserBuff(1bit)|
     * EnvProp: id(1)|positionX(4)|positionY(4)|positionZ(4)|remainSecond(4)|
     * Action: id(2)|code(1)|type(1)|value(4)|
     * Buff: buffId(1)|remainSecond(4)|
     * Prop: propId(1)|count(1)|
     * UserPostion moveState(1)|positionX(4)|positionY(4)|positionZ(4)|rotateY(4)
     * UserBuff buffByte(4)
     */
    public ByteBuffer ChangesToBytes() {
        ByteBuilder byteBuilder =  new ByteBuilder();
        short state = 0;
        byteBuilder.append(state);
        if (game.envPropChangedList.size() != 0) {
            state = (short) (state | 256);
            byteBuilder.append((short)game.envPropChangedList.size());
            for (Game.EnvProp envProp: game.envPropChangedList) {
                byteBuilder.append(envProp.propId);
                byteBuilder.append(envProp.positionX);
                byteBuilder.append(envProp.positionY);
                byteBuilder.append(envProp.positionZ);
                byteBuilder.append(envProp.getRemainSecond());
            }
        }
        if (actionList.size() != 0) {
            state = (short) (state | 128);
            byteBuilder.append((short)actionList.size());
            for (Action action: actionList) {
                byteBuilder.append(action.actionId);
                byteBuilder.append(action.code);
                byteBuilder.append(action.type);
                if (action.type == 1) {
                    byteBuilder.append((Integer)action.value);
                }
            }
        }
        if (isPositionChanged) {
            state = (short) (state | 64);
            byteBuilder.append(position.moveState);
            byteBuilder.append(position.positionX);
            byteBuilder.append(position.positionY);
            byteBuilder.append(position.positionZ);
            byteBuilder.append(position.rotateY);
        }
        if (isLifeChanged) {
            state = (short) (state | 32);
            byteBuilder.append(getLife());
        }
        if (isSpeedChanged) {
            state = (short) (state | 16);
            byteBuilder.append(getSpeed());
        }
        if (buffChangedSet.size() != 0) {
            state = (short) (state | 8);
            for (byte buffId: buffChangedSet) {
                Buff buff = buffMap.get(buffId);
                byteBuilder.append(buff.id);
                byteBuilder.append(buff.getRemainSecond());
            }
        }
        if (propChangedSet.size() != 0) {
            state = (short) (state | 4);
            for (byte propId: propChangedSet) {
                byteBuilder.append(propId);
                byteBuilder.append(propMap.get(propId));
            }
        }
        if (game.positionChangedSet.size() != 0) {
            state = (short) (state | 2);
            byteBuilder.append((short)game.positionChangedSet.size());
            for (int userId: game.positionChangedSet) {
                Position position = game.getUser(userId).getPosition();
                byteBuilder.append(userId);
                byteBuilder.append(position.moveState);
                byteBuilder.append(position.positionX);
                byteBuilder.append(position.positionY);
                byteBuilder.append(position.positionZ);
                byteBuilder.append(position.rotateY);
            }
        }
        if (game.buffChangedSet.size() != 0) {
            state = (short) (state | 1);
            byteBuilder.append((short)game.buffChangedSet.size());
            for (int userId: game.buffChangedSet) {
                byteBuilder.append(userId);
                byteBuilder.append(game.getUser(userId).getBuffBytes());
            }
        }
        byteBuilder.append(state);
        return ByteBuffer.wrap(byteBuilder.getBytes());
    }

    /**
     * 在同步数据后清空修改状态位和数据
     */
    public void clearAfterSync() {
        isLifeChanged = false;
        isPositionChanged = false;
        isSpeedChanged = false;
        isStateChanged = false;

        buffChangedSet.clear();
        propChangedSet.clear();
    }
}
