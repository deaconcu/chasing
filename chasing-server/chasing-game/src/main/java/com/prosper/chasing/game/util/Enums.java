package com.prosper.chasing.game.util;

/**
 * Created by deacon on 2018/5/12.
 */
public class Enums {

    public enum TerrainType {
        NONE(0),

        FOG(1),
        RAIN(2),
        SNOW(3),
        DREAM(4),
        WIND(5),
        GRAVEYARD(6),
        LAKE(7),

        ANIMAL(1),
        BLANK(2),
        BUILDING(3),
        FOREST(5),
        PAVEMENT(9),
        ANIMAL_OSTRICH(10),
        SAND(11),
        SWAMP(13),
        VEGETABLE(14),
        VIEW(15),
        WHEAT(17),
        WILD_WIND(18),
        WIND_OLD(19),
        UPLAND(20),
        WALL(21),
        MOUNTAIN_L1(22),
        MOUNTAIN_L2(23),
        MOUNTAIN_L3(24),
        RIVER(25),
        RIVER_WITH_BRIDGE(26),
        FIRE_FENCE(27),
        FIRE_PUT_OUT(28),
        GATE(27),
        GATE_OPEN(28),
        WILD_ANIMAL(29),
        WILD_FIRE(30),
        FENCE(32),
        FENCE_OPEN(33),
        STONE(34);

        private int value;

        TerrainType(int value) {
            this.value = value;
        }

        public byte getValue() {
            return (byte)value;
        }
    }

    public enum SpecialSectionType {
        NONE(0),
        DEFAULT(1),
        SNOW(2),
        DREAM(3),
        WIND(4),
        GRAVEYARD(5),
        FOG(6),
        WATER(7),
        SLEEPING_MONSTER(8),
        TIGER(9),
        WOLF(10),
        DARK(11),
        OPEN_LAND(12),
        GATES(13),
        WILD_FIRE(14),
        LIGHTING(15),
        RAIN(16),
        INVISIBLE(17);

        private int value;

        SpecialSectionType(int value) {
            this.value = value;
        }

        public byte getValue() {
            return (byte)value;
        }
    }

    public enum HexTerrainType {
        NONE(0),

        FOREST_ROAD(1),
        FOREST(2),
        FOREST_MOUNTAIN_L1(3),
        FOREST_MOUNTAIN_L2(4),

        DARK_FOREST(13),
        DARK_FOREST_ROAD(14),

        SNOW_ROAD(5),
        SNOW_FOREST(6),
        SNOW_MOUNTAIN_L1(7),
        SNOW_MOUNTAIN_L2(8),

        OCEAN(9),
        SHORE(10),

        LAKE(11),
        LAKE_SHORE(12);

        private int value;

        HexTerrainType(int value) {
            this.value = value;
        }

        public byte getValue() {
            return (byte)value;
        }
    }

    public enum LampType {
        NORMAL(0),
        NORMAL_SMALL(1),
        TORCH(2),
        INVISIBLE(3);

        private int value;

        LampType(int value) {
            this.value = value;
        }

        public byte getValue() {
            return (byte)value;
        }
    }

    public enum StationaryType {
        FLAG(1),
        STORE(2),
        FIRE(3);

        private int value;

        StationaryType(int value) {
            this.value = value;
        }

        public byte getValue() {
            return (byte)value;
        }
    }

    public enum InteractiveType {
        RIVER(1), // 河流
        GATE(2),   // 不能通过的门
        FIRE_FENCE(3),   // 火焰
        STONES(4), // 石墙
        SIGNPOST(5), // 路标
        TENT(6), // 帐篷
        HOUSE(7), // 房子
        CAMP_FIRE(8), // 营火
        WOLF(9),
        TIGER(10);

        private int value;

        InteractiveType(int value) {
            this.value = value;
        }

        public byte getValue() {
            return (byte)value;
        }
    }

    public enum ViewTypeV2 {

    }

    public enum ViewType {
        FOG(8),
        RAIN(9),
        SNOW(10),
        DREAM(11);

        private int value;

        ViewType(int value) {
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
        NONE(-1),
        SELF(0),
        PLAYER(1),
        PROP(2),
        STATIONARY(3),
        INTERACTIVE(4);

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

    public enum BlockDirection {
        //FREE((byte)0),
        RIGHT((byte)1),
        DOWN((byte)4),
        LEFT((byte)2),
        UP((byte)3);

        private byte value;

        BlockDirection(byte value) {
            this.value = value;
        }

        public byte getValue() {
            return value;
        }
    }

    public enum HexagonRoadType {
        NONE((byte)0),
        RIGHT_DOWN_LEFT_UP_LEFT((byte)1),
        LEFT_DOWN_RIGHT_UP_RIGHT((byte)2),
        RIGHT_DOWN_LEFT((byte)5),
        RIGHT_UP_LEFT((byte)3),
        LEFT_DOWN_RIGHT((byte)6),
        LEFT_UP_RIGHT((byte)8),
        UP_RIGHT_DOWN_RIGHT((byte)4),
        UP_LEFT_DOWN_LEFT((byte)7),
        RIGHT((byte)9),
        LEFT((byte)10),
        UP_LEFT((byte)11),
        UP_RIGHT((byte)12),
        DOWN_LEFT((byte)13),
        DOWN_RIGHT((byte)14);

        private byte value;

        HexagonRoadType(byte value) {
            this.value = value;
        }

        public byte getValue() {
            return value;
        }
    }

    public enum HexagonDirection {
        FREE((byte)0),
        RIGHT((byte)1),
        LEFT((byte)2),
        UP_LEFT((byte)3),
        UP_RIGHT((byte)4),
        DOWN_LEFT((byte)5),
        DOWN_RIGHT((byte)6);

        private byte value;

        HexagonDirection(byte value) {
            this.value = value;
        }

        public byte getValue() {
            return value;
        }
    }

    public enum HexagonOccupiedType {
        FREE((byte)0),
        ROAD((byte)1),
        OPEN_AREA((byte)2);

        private byte value;

        HexagonOccupiedType(byte value) {
            this.value = value;
        }

        public byte getValue() {
            return value;
        }
    }

    public enum TargetType {
        NONE((byte)0),
        SELF((byte)1),
        PLAYER((byte)2),
        PROP((byte)3),
        STATIONARY((byte)4),
        INTERACTIVE((byte)5),
        POSITION((byte)6);

        private byte value;

        TargetType(byte value) {
            this.value = value;
        }

        public static TargetType getTargetType(byte value) {
            for(TargetType t: TargetType.values()) {
                if(t.value == value) {
                    return t;
                }
            }
            return TargetType.NONE;
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

    public enum RoadPointType {
        CENTER,
        WAYSIDE
    }

    public enum RankValueType {
        TIME_ASCEND((byte)0),
        TIME_DESCEND((byte)1),
        INT_ASCEND((byte)2),
        INT_DESCEND((byte)3);

        private byte value;

        RankValueType(byte value) {
            this.value = value;
        }

        public byte getValue() {
            return value;
        }
    }

    public enum AbilityType {
        SPEED_RATE_ADD_ON,
        STOP_MOVING,
        ADD_BUFF
    }

    public enum PropType {
        NONE((short)0),
        MARK((short)1), // 单人位置显示 标记一个离你位置最近的不同队伍的人，点亮离他最近的那一盏灯，方便在追踪的时候有一个目标
        INVISIBLE_LEVEL_1((short)2),   // 隐身30秒，不能被标记，不在地图和场景中显示，可以移动
        INVISIBLE_LEVEL_2((short)3),   // 隐身5分钟，不能被标记，不在地图和场景中显示，不能移动
        ANTI_INVISIBLE((short)4),  // 对玩家所在地点使用，以该地点为中心点的周围200米距离内，使用了隐形药水的玩家立即显形
        RETURN_TO_INIT_POSITION((short)5),  // 单人模式下使用，回到出发点
        RANDOM_POSITION((short)6), // 随机传送到一个位置
        FLASH_LEVEL_1((short)7),  // 立即传送到被标记目标2米范围内
        FLASH_LEVEL_2((short)8),  // 向被标记目标前进50米
        FOLLOW((short)9), // 跟随某一个目标移动，两人速度为正常值的一半
        SPEED_UP_LEVEL_1((short)10), // 加速道具 20%
        SPEED_UP_LEVEL_2((short)11), // 加速道具 40%
        SPEED_DOWN_LEVEL_1((short)12), // 减速道具 20%
        SPEED_DOWN_LEVEL_2((short)13), // 减速道具 20%
        HOLD_POSITION((short)14), // 停止移动
        BLOOD_PILL((short)15), // 加血一点
        BLOOD_BAG((short)16), // 加血到满
        REBIRTH((short)17), // 死亡后可以重生
        DARK_VISION((short)18), // 让目标视野变黑
        IMMUNITY((short)19), // 对所有道具免疫5分钟，不能移动
        REBOUND((short)21),            // 反弹，持续3分钟
        NEAR_ENEMY_REMIND((short)22),     // 100米接近提醒，正常为50米
        PROP_BOMB((short)23),  // 摧毁目标道具
        MONEY((short)24),  // 金钱
        GIFT_BOX((short)25),  // 未知道具礼盒
        SCEPTER((short)26), // 灵魂权杖
        BRIDGE((short)27), // 桥梁
        RAIN_CLOUD((short)28), // 云雨
        WOOD((short)29); // 云雨

        private short value;

        PropType(short value) {
            this.value = value;
        }

        public static PropType getPropType(short value) {
            for(PropType t: PropType.values()) {
                if (t.value == value) return t;
            }
            return PropType.NONE;
        }

        public short getValue() {
            return value;
        }
    }

    public enum BuffType {
        FLASH_LEVEL_1((byte)1),
        FLASH_LEVEL_2((byte)2),
        SPEED_UP_LEVEL_1((byte)3),
        SPEED_UP_LEVEL_2((byte)4),
        SPEED_DOWN_LEVEL_1((byte)5),
        SPEED_DOWN_LEVEL_2((byte)6),
        HOLD_POSITION((byte)7),
        FOLLOW((byte)8),
        FOLLOWED((byte)9),
        INVISIBLE_LEVEL_1((byte)10),
        INVISIBLE_LEVEL_2((byte)11),
        DARK_VISION((byte)12),
        IMMUNITY((byte)13),
        NEAR_ENEMY_REMIND((byte)14),
        REBOUND((byte)15),
        HOLD_SCEPTER((byte)16),// 持有权杖，可以淘汰对手
        EXPEL((byte)17),// 持有权杖，可以淘汰对手
        SPEED_DOWN_LEVEL_1_TERRAIN ((byte)18),
        SPEED_DOWN_LEVEL_2_TERRAIN((byte)19),
        SPEED_DOWN_LEVEL_3_TERRAIN((byte)20),
        WIND((byte)21),
        ANIMAL((byte)22),
        SLEEPY_LEVEL_1((byte)23),
        SLEEPY_LEVEL_2((byte)24),
        DREAMING ((byte)25);

        private byte value;

        BuffType(byte value) {
            this.value = value;
        }

        public byte getValue() {
            return value;
        }
    }

    public enum FixtureType {
        GATE((byte)1),
        PORTAL((byte)2),
        STORE((byte)3),
        GRASS((byte)4),
        WHEEL((byte)5);

        private byte value;

        FixtureType(byte value) {
            this.value = value;
        }

        public byte getValue() {
            return value;
        }
    }

    /**
     * 道具的使用类型
     * 1：持有，不能使用，比如任务道具，权杖
     * 3：使用，比如卷轴，血瓶之类的
     */
    public enum PropUsageType {
        HOLD, USE
    }

    /**
     * 开场白跟踪目标的类型
     * 1: 自己
     * 2：
     */
    public enum PrologueTargetType {
        SELF,

    }
}
