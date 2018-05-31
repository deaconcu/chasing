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
        MOUNTAIN_L2(22),
        MOUNTAIN_L3(23);

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
        WALL(1),
        OBSTACLE(2),
        MAIN_ROAD(3),
        SHORTCUT(4),
        BRANCH(5),
        SLASH_ROAD_MOUNTAIN_L1(6),
        SLASH_MOUNTAIN_L1_ROAD(7),
        BACKSLASH_ROAD_MOUNTAIN_L1(8),
        BACKSLASH_MOUNTAIN_L1_ROAD(9),
        SLASH_MOUNTAIN_L2_MOUNTAIN_L1(10),
        SLASH_MOUNTAIN_L1_MOUNTAIN_L2(11),
        BACKSLASH_MOUNTAIN_L2_MOUNTAIN_L1(12),
        BACKSLASH_MOUNTAIN_L1_MOUNTAIN_L2(13),
        SLASH_MOUNTAIN_L2_MOUNTAIN_L3(14),
        SLASH_MOUNTAIN_L3_MOUNTAIN_L2(15),
        BACKSLASH_MOUNTAIN_L2_MOUNTAIN_L3(16),
        BACKSLASH_MOUNTAIN_L3_MOUNTAIN_L2(17),
        TRANSITION(18),
        MOUNTAIN_L2(19),
        MOUNTAIN_L4(20),
        MOUNTAIN_L6(21),
        PAVEMENT(22),
        SLOPE(23),
        MOUNTAIN_L1(24),
        MOUNTAIN_L3(25),
        MOUNTAIN_L5(26);

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
