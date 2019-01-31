package com.prosper.chasing.game.util;

/**
 * Created by deacon on 2018/5/12.
 */
public class Enums {

    public enum TerrainType {
        NONE(0),

        RAIN(8),
        SNOW(12),

        ANIMAL(1),
        BLANK(2),
        BUILDING(3),
        FOG(4),
        FOREST(5),
        DREAM_L1(6),
        DREAM_L2(7),
        PAVEMENT(9),
        ANIMAL_OSTRICH(10),
        SAND(11),
        SWAMP(13),
        VEGETABLE(14),
        VIEW(15),
        WATER(16),
        WHEAT(17),
        WILD_WIND(18),
        WIND(19),
        UPLAND(20),
        WALL(21),
        MOUNTAIN_L1(22),
        MOUNTAIN_L2(23),
        MOUNTAIN_L3(24),
        RIVER(25),
        RIVER_WITH_BRIDGE(26),
        FIRE(27),
        FIRE_PUT_OUT(28),
        GATE(27),
        GATE_OPEN(28),
        WILD_ANIMAL(29),
        WILD_FIRE(30),
        FENCE(32),
        FENCE_OPEN(33);

        private int value;

        TerrainType(int value) {
            this.value = value;
        }

        public byte getValue() {
            return (byte)value;
        }
    }

    public enum StationaryType {
        LAMP(1),
        BRIDGE(2),
        FLAG(3),
        SNOW(4),
        FIRE(5),
        STORE(6),

        RIVER(7), // 河流
        RIVER_WITH_BRIDGE(8),   // 有桥的河流
        GATE(9),   // 不能通过的门
        GATE_OPEN(10),   // 可以通过的门
        FIRE_FENCE(11),   // 火焰
        FIRE_FENCE_PUT_OUT(12),   // 已经熄灭的火焰
        STONES(13), // 石墙
        STONES_BROKEN(14); // 已破裂的石墙

        private int value;

        StationaryType(int value) {
            this.value = value;
        }

        public byte getValue() {
            return (byte)value;
        }
    }

    public enum AnimalType {
        TIGER(1),
        WOLF(2);

        private int value;

        AnimalType(int value) {
            this.value = value;
        }

        public byte getValue() {
            return (byte)value;
        }
    }

    public enum GameObjectType {
        NPC(1),
        INTERACT(2),
        DYNAMIC(3),
        PROP(4);

        private int value;

        GameObjectType(int value) {
            this.value = value;
        }

        public byte getValue() {
            return (byte)value;
        }
    }

    public enum BuildingType {
        NONE(0),
        WAREHOUSE(1),
        STORE(2),
        TALL_TREE(3),
        GRAVEYARD(4),
        WELL(5),
        JACKSTRAW(6);

        private int value;

        BuildingType(int value) {
            this.value = value;
        }

        public byte getValue() {
            return (byte)value;
        }
    }

    public enum BlockType {
        MAIN_ROAD(1),
        SHORTCUT(2),
        BRANCH(3),
        ROAD_EXTENSION(4),
        BUILDING(5),
        MOUNTAIN_L1(6),
        MOUNTAIN_L2(7),
        MOUNTAIN_L3(8),
        MOUNTAIN_SLOP(9),
        MOUNTAIN_ROCK(10),
        SEA_L1(11),
        SEA_L2(12),
        SEA_L3(13),
        SEA_L4(14),
        SEA_L5(15),
        SEA_L6(16),
        WOODS(17),
        HILL(18);

        private int value;

        BlockType(int value) {
            this.value = value;
        }

        public byte getValue() {
            return (byte)value;
        }
    }

    public enum RoadDirection {
        NONE,           // block不在道路上
        VERTICAL,       // 道路在当前block位置是竖直的，且为连续道路
        HORIZONTAL,     // 道路在当前block位置是水平的，且为连续道路
        VERTICAL_END,   // 道路在当前block位置是竖直的，且终止在这个位置
        HORIZONTAL_END, // 道路在当前block位置是水平的，且终止在这个位置
        TURNING,        // 道路在当前block位置转向
        CROSS,          // 道路在当前block位置交叉
        STAND_ALONE     // 单块道路
    }

    public enum Direction {
        FREE((byte)0),
        RIGHT((byte)1),
        LEFT((byte)2),
        UP((byte)3),
        DOWN((byte)4),
        UP_LEFT((byte)5),
        UP_RIGHT((byte)6),
        DOWN_LEFT((byte)7),
        DOWN_RIGHT((byte)8);

        private byte value;

        Direction(byte value) {
            this.value = value;
        }

        public byte getValue() {
            return value;
        }
    }

    public enum TargetType {
        NONE((byte)0),
        USER((byte)1),
        PROP((byte)2),
        POSITION((byte)3);

        private byte value;

        TargetType(byte value) {
            this.value = value;
        }

        public byte getValue() {
            return value;
        }
    }

    public enum ResourceType{
        MONEY,
        PROP
    }

    public enum SyncAction {
        BORN((byte)0),
        ALIVE((byte)1),
        DEAD((byte)2);

        private byte value;

        SyncAction(byte value) {
            this.value = value;
        }

        public byte getValue() {
            return value;
        }
    }

}
