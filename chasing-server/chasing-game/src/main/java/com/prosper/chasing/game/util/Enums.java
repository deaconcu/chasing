package com.prosper.chasing.game.util;

/**
 * Created by deacon on 2018/5/12.
 */
public class Enums {

    public enum Orientation {
        FREE(0),
        EAST(1),
        SOUTH(2),
        WEST(3),
        NORTH(4);

        private int value;

        Orientation(int value) {
            this.value = value;
        }

        public byte getValue() {
            return (byte)value;
        }
    }

    public enum TerrainType {
        NONE(0),
        ANIMAL(1),
        BLANK(2),
        BUILDING(3),
        FOG(4),
        FOREST(5),
        GRASS(6),
        LAVA(7),
        RAIN(8),
        PAVEMENT(9),
        ROCK(10),
        SAND(11),
        SNOW(12),
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
        MOUNTAIN_L3(24);

        private int value;

        TerrainType(int value) {
            this.value = value;
        }

        public byte getValue() {
            return (byte)value;
        }
    }

    public enum BuildingType {
        NONE(0),
        WALL(1),
        WALL_2_BLOCK(1),
        TOWER_A(2),
        TOWER_B(3),
        GATE(4),
        CASTLE(5),
        ROTUNDA(6),
        WAREHOUSE(7),
        STORE(8),
        TALL_TREE(9),
        GRAVEYARD(10),
        WELL(11),
        JACKSTRAW(12);

        private int value;

        BuildingType(int value) {
            this.value = value;
        }

        public byte getValue() {
            return (byte)value;
        }
    }

    public enum BlockType {
        ARTERY(1),
        SHORTCUT(2),
        BRANCH(3),
        ROAD_EXTENSION(4),
        MOUNTAIN_L1(5),
        MOUNTAIN_L2(6),
        MOUNTAIN_L3(7),
        SEA_L1(8),
        SEA_L2(9);

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
        SELF(0),
        RIGHT(1),
        LEFT(2),
        UP(3),
        DOWN(4),
        UP_LEFT(5),
        UP_RIGHT(6),
        DOWN_LEFT(7),
        DOWN_RIGHT(8);

        private int value;

        Direction(int value) {
            this.value = value;
        }

        public byte getValue() {
            return (byte)value;
        }
    }

}
