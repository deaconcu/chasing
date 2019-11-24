package com.prosper.chasing.game.mapV3;

import com.prosper.chasing.game.base.*;
import com.prosper.chasing.game.base.InteractiveObjects.InteractiveInfo;
import com.prosper.chasing.game.util.ByteBuilder;
import com.prosper.chasing.game.util.Enums.*;
import com.prosper.chasing.game.util.Graph;
import com.prosper.chasing.game.util.Util;

import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.zip.GZIPOutputStream;

import static com.prosper.chasing.game.util.Enums.HexTerrainType.*;

/**
 * Created by deacon on 2019/3/5.
 */
public class MapSkeleton {

    // 地图上的全部块
    public Map<Integer, Hexagon> hexagonMap;
    // 已使用的路径块
    public Map<Integer, Hexagon> occupiedMap;
    // 空闲的空白块
    public Map<Integer, Hexagon> freeMap;

    public int start;

    public int end;

    public int bound;

    public Set<Hexagon> vertexSet = new HashSet<>();
    public Map<Hexagon, RoadPoint[]> branchCrossPoint = new HashMap<>();
    public Set<Branch> branchSet = new HashSet<>();
    public Map<Integer, Integer> distancesToEnd = new HashMap<>();
    public Map<Integer, Map<Integer, List<Integer>>> pathMap;
    public Map<Integer, Segment> segmentMap = new HashMap<>();
    public Map<Short, SpecialSection> specialSectionMap = new HashMap<>();
    public Map<Point2, Short> pointMap = new HashMap<>();
    public Map<Integer, Short> pointIdMap = new HashMap<>();
    public Map<Integer, View> viewMap = new HashMap<>();
    public Set<InteractiveInfo> interactiveSet = new HashSet<>();
    public List<OpenArea> openAreaList = new LinkedList<>();
    public HashMap<Point2, ViewV2> viewV2Map = new HashMap<>();

    private short nextGroupId = 1;
    private short nextLampId = 1;

    public static class TreeNode {
        int hexagonId;
        List<TreeNode> children;
        TreeNode parents;
        boolean onPath;

        public TreeNode(int hexagonId, TreeNode parents) {
            this.hexagonId = hexagonId;
            this.parents = parents;
            children = new LinkedList<>();
            onPath = false;
        }

        public void addChild(int childHexagonId) {
            if (childHexagonId == -1 || (parents != null && childHexagonId == parents.hexagonId)) return;
            children.add(new TreeNode(childHexagonId, this));
        }

        @Override
        public String toString() {
            String childString = "";
            for (TreeNode treeNode: children) {
                childString += treeNode.hexagonId + ",";
            }
            return "block objectId:" + hexagonId + ", child: [" + childString + "]";
        }
    }

    public MapSkeleton(int bound) {
        if (bound % 2 != 0) throw new RuntimeException("map bound must be even");
        this.bound = bound;
        this.start = 1;
        this.end = bound * bound;

        init();
    }

    public MapSkeleton(int bound, int length) {
        if (bound % 2 != 0) throw new RuntimeException("map bound must be even");
        this.bound = bound;
        //this.start = 1;
        //this.end = bound * bound;
        this.start = getHexagonId(11, 11);
        this.end = getHexagonId(bound - 10, bound - 10);

        int fillCount = 1;
        int pruningCount = 1;
        while (true) {
            while (true) {
                init();
                fill();
                System.out.println("fill: " + fillCount);
                fillCount ++;
                if (isContainStartEnd()) {
                    break;
                }
            }
            pruning(start, end);
            System.out.println("pruning: " + pruningCount);
            pruningCount ++;
            if (length() > length) {
                break;
            }
        }
    }

    private void fill() {
        Hexagon hexagon = randomUnusedHexagon();
        addBlock(hexagon, null);

        LinkedList<Hexagon> pathBlockIdList = new LinkedList<>();
        pathBlockIdList.add(hexagon);
        while (true) {
            Hexagon nextFreeHexagon = getRandomFreeSibling(hexagon);
            while (nextFreeHexagon == null && pathBlockIdList.size() > 0) {
                hexagon = pathBlockIdList.get(ThreadLocalRandom.current().nextInt(pathBlockIdList.size()));

                nextFreeHexagon = getRandomFreeSibling(hexagon);
                if (nextFreeHexagon == null) {
                    pathBlockIdList.remove(hexagon);
                }
            }
            if (pathBlockIdList.size() == 0) break;

            addBlock(nextFreeHexagon, hexagon);
            hexagon = nextFreeHexagon;
            if (!pathBlockIdList.contains(hexagon)) {
                pathBlockIdList.add(hexagon);
            }
        }
    }

    private void pruning(int startHexagonId, int endHexagonId) {
        TreeNode startNode = new TreeNode(startHexagonId, null);
        TreeNode endNode = null;
        TreeNode currentNode = startNode;
        LinkedList<TreeNode> treeNodeList = new LinkedList<>();
        while(true) {
            if (currentNode == null) break;
            if (currentNode.hexagonId == endHexagonId) endNode = currentNode;

            List<Hexagon> siblings = getRoadSibling(occupiedMap.get(currentNode.hexagonId));
            for (Hexagon hexagon: siblings) {
                currentNode.addChild(hexagon.getId());
            }

            for (TreeNode childNode: currentNode.children) {
                treeNodeList.add(childNode);
            }
            currentNode = treeNodeList.pollFirst();
        }

        currentNode = endNode;
        while (currentNode != null) {
            currentNode.onPath = true;
            currentNode = currentNode.parents;
        }

        currentNode = startNode;
        treeNodeList.clear();
        while(true) {
            if (currentNode == null) break;
            Hexagon hexagon = occupiedMap.get(currentNode.hexagonId);
            if (!currentNode.onPath) {
                occupiedMap.remove(hexagon.getId());
                freeMap.put(hexagon.getId(), hexagon);
            }
            hexagon.clear();

            for (TreeNode childNode: currentNode.children) {
                treeNodeList.add(childNode);
            }
            currentNode = treeNodeList.pollFirst();
        }

        currentNode = endNode;
        while (currentNode != null) {
            Hexagon hexagon = occupiedMap.get(currentNode.hexagonId);
            if (currentNode.parents != null) {
                 Hexagon parentHexagon = occupiedMap.get(currentNode.parents.hexagonId);
                 hexagon.setBridges(hexagon.getDirection(parentHexagon), true);
                 parentHexagon.setBridges(parentHexagon.getDirection(hexagon), true);
            }
            currentNode = currentNode.parents;
        }
    }

    public MapSkeleton merge(MapSkeleton mapSkeleton) {
        for (Hexagon hexagon: mapSkeleton.occupiedMap.values()) {
            getHexagon(hexagon.getId()).merge(hexagon);
            if (!occupiedMap.containsKey(hexagon.getId())) {
                occupiedMap.put(hexagon.getId(), getHexagon(hexagon.getId()));
            }
            freeMap.remove(hexagon.getId());
        }
        return this;
    }

    private void removeBranch(Branch branch) {
        if (branch.distance() > 1) {
            for (Hexagon hexagon: branch.hexagonList) {
                removeBlock(hexagon);
            }
        } else {
            branch.head.setBridges(branch.head.getDirection(branch.tail), false);
            branch.tail.setBridges(branch.tail.getDirection(branch.head), false);
        }
    }

    protected void removeBlock(Hexagon hexagon) {
        if (hexagon == null) return;
        for (Hexagon sibling: getRoadSibling(hexagon)) {
            sibling.setBridges(sibling.getDirection(hexagon), false);
        }

        occupiedMap.remove(hexagon.getId());
        hexagon.clear();
        freeMap.put(hexagon.getId(), hexagon);
    }

    public void optimize() {
        while (true) {
            boolean branchRemoved = false;
            trimAndCalculate(1);
            trimAndCalculate(2);

            for (Branch branch: branchSet) {
                if (branch.head.bridgeCount() < 3 || branch.tail.bridgeCount() < 3) continue;
                else if (branch.distance() < 6 && branch.detourDistance > 0 &&
                        Math.abs(branch.detourDistance - branch.distance()) < 5) {
                    removeBranch(branch);
                    branchRemoved = true;
                    System.out.println("branch removed, " + branch);
                    break;
                }
            }
            if (!branchRemoved) break;
        }
        doSegment();
    }

    private Hexagon getFreeCenterHexagon(Hexagon... hexagons) {
        if (hexagons == null || hexagons.length < 3) return null;
        List<Hexagon> listA = getAroundHexagon(hexagons[0], HexagonOccupiedType.FREE);
        int size = hexagons.length;
        for (Hexagon aroundHexagon: listA) {
            boolean isCenter = true;
            for (int i = 1; i < size; i ++) {
                if (!aroundHexagon.isAround(hexagons[i])) isCenter = false;
            }
            if (isCenter) return aroundHexagon;
        }
        return null;
    }

    /*
    private int addLamp(Map<String, Integer> pointPressMap, LampType type, Point2 position)
    {
        String pointPress = pointPress(position);
        int currLampId;
        if (!pointPressMap.containsKey(pointPress)) {
            currLampId = nextLampId++;
            pointPressMap.put(pointPress, currLampId);
            lampMap.put(currLampId, new Lamp(currLampId, type, new Point3(position.x, 0, position.y), 0));
            return currLampId;
        } else {
            return pointPressMap.get(pointPress);
        }
    }
    */

    private List<Hexagon> getAroundHexagon(Hexagon hexagon, HexagonOccupiedType type) {
        List<Hexagon> hexagonList = new LinkedList<>();
        for (HexagonDirection direction: HexagonDirection.values()) {
            if (direction == HexagonDirection.FREE) continue;
            int aroundHexagonId = getHexagonIdByDirection(hexagon, direction);

            if (type == HexagonOccupiedType.ROAD) {
                if (occupiedMap.containsKey(aroundHexagonId) &&
                        occupiedMap.get(aroundHexagonId).getType() == HexagonOccupiedType.ROAD) {
                    hexagonList.add(occupiedMap.get(aroundHexagonId));
                }
            } else if (type == HexagonOccupiedType.OPEN_AREA) {
                if (occupiedMap.containsKey(aroundHexagonId) &&
                        occupiedMap.get(aroundHexagonId).getType() == HexagonOccupiedType.OPEN_AREA) {
                    hexagonList.add(occupiedMap.get(aroundHexagonId));
                }
            } else if (type == HexagonOccupiedType.FREE) {
                if (freeMap.containsKey(aroundHexagonId)) {
                    hexagonList.add(freeMap.get(aroundHexagonId));
                }
            }
        }
        return hexagonList;
    }

    private boolean isAroundHexagon(Hexagon hexagon, HexagonOccupiedType type) {
        for (HexagonDirection direction: HexagonDirection.values()) {
            if (direction == HexagonDirection.FREE) continue;
            int aroundHexagonId = getHexagonIdByDirection(hexagon, direction);

            if (type == HexagonOccupiedType.ROAD) {
                if (occupiedMap.containsKey(aroundHexagonId) &&
                        occupiedMap.get(aroundHexagonId).getType() == HexagonOccupiedType.ROAD) {
                    return true;
                }
            } else if (type == HexagonOccupiedType.OPEN_AREA) {
                if (occupiedMap.containsKey(aroundHexagonId) &&
                        occupiedMap.get(aroundHexagonId).getType() == HexagonOccupiedType.OPEN_AREA) {
                    return true;
                }
            } else if (type == HexagonOccupiedType.FREE) {
                if (freeMap.containsKey(aroundHexagonId)) {
                    return true;
                }
            }
        }
        return false;
    }

    /*
    public Point2 getNearestRoadPoint(Point2 point, List<Segment> segments) {
        int distance = Integer.MAX_VALUE;
        Point2 nearestPoint = null;
        for (Segment segment: segments) {
            List<RoadPoint> roadPoints = segment.getAllRoadPoint();
            for (RoadPoint roadPoint: roadPoints) {
                int currDistance =  roadPoint.getPoint().distance(point);
                if (currDistance < distance) {
                    distance = currDistance;
                    nearestPoint = roadPoint.getPoint();
                }
            }
        }
        return nearestPoint;
    }
    */

    /**
     * 创建开阔地带，分为两种类型
     * 1. 单个六边形
     * 2. 多个六边形，六边形总数不超过最大值
     */
    public void generateOpenArea() {
        /*
        List<Hexagon> hexagonList = new LinkedList<>();
        hexagonList.addAll(freeMap.values());
        Collections.shuffle(hexagonList);

        // 生成单个六边形的开阔地
        int singleAreaCount = 20;
        int index = 0;

        while (index < hexagonList.size() && singleAreaCount > 0) {
            Hexagon hexagon = hexagonList.get(index ++);
            // 必须在路边
            if (!isAroundHexagon(hexagon, HexagonOccupiedType.ROAD)) continue;
            // 必须不能临近交叉点
            List<Hexagon> aroundHexagons = getAroundHexagon(hexagon, HexagonOccupiedType.ROAD);
            boolean nearCross = false;
            for (Hexagon aroundHexagon: aroundHexagons) {
                if (isCrossPoint(aroundHexagon.getId())) {
                    nearCross = true;
                    break;
                }
            }
            if (nearCross) continue;

            // 获取一个随机的连接点
            Hexagon connectHexagon = Util.random(aroundHexagons);

            List<Segment> segments = getSegments(connectHexagon);
            Point2 connectPoint = getNearestRoadPoint(hexagon.coordinatePoint(), segments);

            // 创建single hexagon openArea
            OpenArea openArea = new OpenArea(hexagon);
            openArea.addConnectPoint(new Point2[] {hexagon.coordinatePoint(), connectPoint});

            hexagon.setOpenAreaId(openArea.getId());
            openAreaList.add(openArea);

            freeMap.remove(hexagon.getId());
            occupiedMap.put(hexagon.getId(), hexagon);
            singleAreaCount --;
        }

        for (OpenArea openArea: openAreaList) {
            hexagonList.removeAll(openArea.getArea());
        }

        // 生成多个六边形的开阔地
        int maxSize = 11;
        int minSize = 5;
        int multiHexagonAreaCount = 15;

        while (multiHexagonAreaCount > 0) {
            Hexagon hexagon = hexagonList.get(ThreadLocalRandom.current().nextInt(hexagonList.size()));

            Set<Hexagon> area = new HashSet<>();
            Map<Hexagon, boolean[]> hexagonSiblings = new HashMap<>();
            area.add(hexagon);

            List<Hexagon> borderList = new LinkedList<>();
            borderList.add(hexagon);

            Hexagon current;

            while (area.size() < maxSize && borderList.size() > 0) {
                current = borderList.get(ThreadLocalRandom.current().nextInt(borderList.size()));
                List<Hexagon> aroundHexagons = getAroundHexagon(current, HexagonOccupiedType.FREE);

                Iterator<Hexagon> iterator = aroundHexagons.iterator();
                while (iterator.hasNext()) {
                    if (area.contains(iterator.next())) {
                        iterator.remove();
                    }
                }

                if (aroundHexagons.size() == 0) {
                    borderList.remove(current);
                    continue;
                }

                Hexagon freeHexagon = aroundHexagons.get(ThreadLocalRandom.current().nextInt(aroundHexagons.size()));
                area.add(freeHexagon);
                borderList.add(freeHexagon);
            }

            // 检查开阔地必须满足的条件：
            // 节点数量不能超过限制
            if (area.size() < minSize) continue;

            // 每个节点必须有两个以上相邻方向的临近节点
            boolean isAreaValid = true;
            for(Hexagon areaHexagon: area) {
                //List<Hexagon> aroundHexagons = getAroundHexagon(areaHexagon, HexagonOccupiedType.FREE);

                boolean[] seq = new boolean[6];
                if (area.contains(getHexagonByDirection(areaHexagon, HexagonDirection.RIGHT))) seq[0] = true;
                if (area.contains(getHexagonByDirection(areaHexagon, HexagonDirection.DOWN_RIGHT))) seq[1] = true;
                if (area.contains(getHexagonByDirection(areaHexagon, HexagonDirection.DOWN_LEFT))) seq[2] = true;
                if (area.contains(getHexagonByDirection(areaHexagon, HexagonDirection.LEFT))) seq[3] = true;
                if (area.contains(getHexagonByDirection(areaHexagon, HexagonDirection.UP_LEFT))) seq[4] = true;
                if (area.contains(getHexagonByDirection(areaHexagon, HexagonDirection.UP_RIGHT))) seq[5] = true;
                hexagonSiblings.put(areaHexagon, seq);

                boolean isHexagonValid = false;
                for (int i = 0; i < 6; i ++) {
                    int j = i + 1 >= 6 ? 0 : i + 1;
                    if (seq[i] && seq[j]) {
                        isHexagonValid = true;
                        break;
                    }
                }

                if (!isHexagonValid) {
                    isAreaValid = false;
                    break;
                }
            }
            if (!isAreaValid) continue;

            // 必须和两个以上道路节点相邻，相邻道路节点的最大距离应该大于5
            Map<Hexagon, Hexagon> roadSiblingMap = new HashMap<>();
            for (Hexagon areaHexagon: area) {
                List<Hexagon> aroundHexagons = getAroundHexagon(areaHexagon, HexagonOccupiedType.ROAD);
                for (Hexagon aroundHexagon: aroundHexagons) {
                    if (roadSiblingMap.containsKey(areaHexagon)) return;
                    else roadSiblingMap.put(aroundHexagon, areaHexagon);
                }
            }
            if (roadSiblingMap.size() < 2) continue;

            // TODO 相邻道路节点的最大距离应该大于5

            //  创建多个地块的开阔地以及连接点
            OpenArea openArea = new OpenArea(area);
            for (int i = 0; i < 2; i ++) {
                Hexagon roadHexagon = Util.randomKey(roadSiblingMap);

                List<Segment> segments = getSegments(roadHexagon);
                if (segments == null || segments.size() == 0) continue;

                Point2 connectPoint = getNearestRoadPoint(
                        roadSiblingMap.get(roadHexagon).coordinatePoint(), segments);
                openArea.addConnectPoint(new Point2[] {
                        connectPoint, roadSiblingMap.get(roadHexagon).coordinatePoint()});
                roadSiblingMap.remove(roadHexagon);
            }

            for (Hexagon areaHexagon: area) {
                freeMap.remove(areaHexagon.getId());
                occupiedMap.put(areaHexagon.getId(), areaHexagon);
                areaHexagon.setOpenAreaSiblings(hexagonSiblings.get(areaHexagon));
            }
            openAreaList.add(openArea);

            multiHexagonAreaCount --;
            System.out.println("generate open area");
        }
        */
    }

    public void generateTerrainV2() {
        for (Branch branch: branchSet) {
            System.out.println("distance:" + branch.distance() + " edr:" + branch.getExtraDistanceRate());

            SpecialSectionType sSectionType = null;
            HexTerrainType hexTerrainType = FOREST_ROAD;
            // 获取当前branch的特殊地段类型
            if (branch.distance() >= SpecialSection.MIN_SIZE && branch.getExtraDistanceRate() > 20) {
                if (branch.getExtraDistanceRate() > 200) {
                    sSectionType = SpecialSectionType.WIND;
                }
                else if (branch.getExtraDistanceRate() > 150) {
                    sSectionType = SpecialSectionType.DREAM;
                }
                else if (branch.getExtraDistanceRate() > 100) {
                    sSectionType = SpecialSectionType.SNOW;
                    hexTerrainType = SNOW_ROAD;
                }
                else if (branch.getExtraDistanceRate() > 60) {
                    //sSectionType = SpecialSectionType.RAIN;
                    sSectionType = SpecialSectionType.WOLF;
                    hexTerrainType = DARK_FOREST_ROAD;
                }
                else if (branch.getExtraDistanceRate() > 20) {
                    sSectionType = SpecialSectionType.FOG;
                }
            }

            branch.head.setTerrainType(FOREST_ROAD);
            branch.tail.setTerrainType(FOREST_ROAD);
            for (Hexagon hexagon: branch.hexagonList) {
                hexagon.setSpecialSectionType(sSectionType);
                hexagon.setTerrainType(hexTerrainType);
            }
        }

        boolean isChanged;
        int step = 1;
        Set<Integer> hexagonSet = new HashSet();
        while (step <= 4) {
            isChanged = false;
            for (Hexagon hexagon : hexagonMap.values()) {
                if (step == 1) {
                    if (hexagon.getTerrainType() != null) continue;
                } else if (step == 4) {
                    //if (hexagonSet.contains(hexagon.getId())) continue;
                }

                Hexagon[] siblings = getSibling(hexagon);
                if (setTerrainType(hexagon, siblings, step)) isChanged = true;
                hexagonSet.add(hexagon.getId());
            }
            if (isChanged == false) {
                step ++;
                hexagonSet.clear();
            }
        }
    }

    /**
     * 根据周围的hexagon设置当前块的地形
     * 规则(优先级从上到下):
     * * 和道路相邻的空白块设置为对应的路旁地形, 如果空白块和多个不同地形相邻，按如下规则
     * * * 选择临近块中地形占比最多的那个
     * * * 如果存在多个地形占比相同，在特殊地形中随机选择一个
     * *
     *
     *
     * * 雪地
     * * *
     * * *
     * @param siblings
     */
    public boolean setTerrainType(Hexagon hexagon, Hexagon[] siblings, int step) {
        boolean isChanged = false;
        if (step <= 1) {
            HexTerrainType[] terrainTypes = getLargestTerrainType(siblings);
            if (terrainTypes == null || terrainTypes.length == 0) return false;

            HexTerrainType terrainType;
            if (terrainTypes.length == 1) {
                terrainType = terrainTypes[0];
            } else {
                terrainType = Util.random(terrainTypes);
            }

            if (terrainType == FOREST_ROAD || terrainType == FOREST
                    || terrainType == FOREST_MOUNTAIN_L1) {
                isChanged = hexagon.setTerrainType(Util.random(
                        FOREST, FOREST, FOREST, FOREST_MOUNTAIN_L1));
            } else if (terrainType == SNOW_ROAD || terrainType == SNOW_FOREST
                    || terrainType == SNOW_MOUNTAIN_L1) {
                isChanged = hexagon.setTerrainType(Util.random(
                        SNOW_FOREST, SNOW_FOREST, SNOW_MOUNTAIN_L1));
            } else if (terrainType == DARK_FOREST_ROAD) {
                isChanged = hexagon.setTerrainType(Util.random(
                        DARK_FOREST, DARK_FOREST, DARK_FOREST, DARK_FOREST, FOREST_MOUNTAIN_L1));
            }
        } else if (step == 2) {
            if (!occupiedAny(siblings)) {
                if (isBorder(hexagon) || hasTerrain(siblings, OCEAN)) {
                    isChanged = hexagon.setTerrainType(OCEAN);
                }
            }
        } else if (step == 3) {
            if (isGroundForest(hexagon.getTerrainType()) &&  hasTerrain(siblings, OCEAN)) {
                isChanged = hexagon.setTerrainType(SHORE);
            }
        } else if (step == 4) {
            if (inTerrain(siblings, FOREST, FOREST_MOUNTAIN_L1)){
                HexTerrainType terrainType = Util.random(LAKE, FOREST_MOUNTAIN_L2);
                if (terrainType == FOREST_MOUNTAIN_L2) {
                    isChanged = hexagon.setTerrainType(FOREST_MOUNTAIN_L2);
                    for (Hexagon sibling: siblings) {
                        if (sibling.getTerrainType() == FOREST)
                            isChanged = sibling.setTerrainType(NONE);
                    }
                } else if (terrainType == LAKE) {
                    isChanged = hexagon.setTerrainType(LAKE);
                }
            } else if (inTerrain(siblings, SNOW_FOREST, SNOW_MOUNTAIN_L1)) {
                isChanged = hexagon.setTerrainType(SNOW_MOUNTAIN_L2);
                for (Hexagon sibling: siblings) {
                    if (sibling.getTerrainType() == SNOW_FOREST)
                        isChanged = sibling.setTerrainType(NONE);
                }
            } else if (isGroundForest(hexagon.getTerrainType()) && !occupiedAny(siblings) && hasTerrain(siblings, LAKE)) {
                isChanged = hexagon.setTerrainType(LAKE);
            } else if (isGroundForest(hexagon.getTerrainType()) && occupiedAny(siblings) && hasTerrain(siblings, LAKE)) {
                isChanged = hexagon.setTerrainType(LAKE_SHORE);
            }
        }
        return isChanged;
    }

    private HexTerrainType[] getLargestTerrainType(Hexagon[] hexagons) {
        if (hexagons.length == 0) return null;
        int size = hexagons.length;

        int[] counts = new int[size];
        for (int i = 0; i < size; i ++) {
            for (int j = 0; j < size; j ++) {
                if (i == j || hexagons[i] == null || hexagons[j] == null) continue;
                if (hexagons[j].getTerrainType() == hexagons[i].getTerrainType());
                counts[i] ++;
            }
        }

        int maxCount = 0;
        for (int i = 0; i < size; i ++) {
            if (counts[i] > maxCount) maxCount = counts[i];
        }
        if (maxCount == 0) return null;

        Set<HexTerrainType> hexTerrainTypes = new HashSet<>();
        for (int i = 0; i < size; i ++) {
            if (counts[i] == maxCount) hexTerrainTypes.add(hexagons[i].getTerrainType());
        }

        return hexTerrainTypes.toArray(new HexTerrainType[]{});
    }

    /**
     * 判断地形是否为地面森林区
     */
    private boolean isGroundForest(HexTerrainType terrainType) {
        if (terrainType == FOREST) return true;
        return false;
    }

    /**
     * 判断地形是否为道路
     */
    private boolean isRoad(HexTerrainType terrainType) {
        if (terrainType == DARK_FOREST_ROAD || terrainType == FOREST_ROAD || terrainType == SNOW_ROAD) return true;
        return false;
    }

    /**
     * 判断六边形是否位于地图边界上
     */
    private boolean isBorder(Hexagon hexagon) {
        if (hexagon.getX() == 0 || hexagon.getY() == 0 || hexagon.getX() == bound || hexagon.getY() == bound )
            return true;
        return false;
    }

    /**
     * 判断多个六边形中是否含有某一个地形
     */
    private boolean hasTerrain(Hexagon[] hexagons, HexTerrainType terrainType) {
        for (Hexagon hexagon: hexagons) {
            if (hexagon.getTerrainType() == terrainType) return true;
        }
        return false;
    }

    /**
     * 判断多个六边形的地形是否都在给定的地形中
     */
    private boolean inTerrain(Hexagon[] hexagons, HexTerrainType... terrainTypes) {
        for (Hexagon hexagon: hexagons) {
            if (!Util.contains(terrainTypes, hexagon.getTerrainType())) return false;
        }
        return true;
    }

    private boolean occupiedAny(Hexagon... hexagons) {
        for (Hexagon hexagon: hexagons) {
            if (occupiedMap.containsKey(hexagon.getId())) return true;
        }
        return false;
    }

    private void addView(ViewV2 view) {
        viewV2Map.put(view.getPosition(), view);
    }

    public MapSkeleton expandBorder() {
        MapSkeleton newMapSkeleton = new MapSkeleton(bound + 4);
        for (Hexagon hexagon: occupiedMap.values()) {
            Hexagon newHexagon;
            newHexagon = newMapSkeleton.getHexagon(hexagon.getX() + 2, hexagon.getY() + 2);

            if (hexagon.getId() == start) newMapSkeleton.start = newHexagon.getId();
            if (hexagon.getId() == end) newMapSkeleton.end = newHexagon.getId();
            for (HexagonDirection roadDirection: hexagon.getRoadDirection()) {
                Hexagon newSibling1 = newMapSkeleton.getHexagonByDirection(newHexagon, roadDirection);
                newMapSkeleton.addBlock(newHexagon, newSibling1);
                newMapSkeleton.addBlock(newSibling1, newHexagon);
            }
        }
        return newMapSkeleton;
    }

    public MapSkeleton expand()
    {
        MapSkeleton newMapSkeleton = new MapSkeleton(bound * 3 + 4);
        for (Hexagon hexagon: occupiedMap.values()) {
            Hexagon newHexagon;
            if (hexagon.getY() % 2 == 1)
                newHexagon = newMapSkeleton.getHexagon(hexagon.getX() * 3, hexagon.getY() * 3);
            else
                newHexagon = newMapSkeleton.getHexagon(hexagon.getX() * 3 + 1, hexagon.getY() * 3);

            if (hexagon.getId() == start) newMapSkeleton.start = newHexagon.getId();
            if (hexagon.getId() == end) newMapSkeleton.end = newHexagon.getId();
            for (HexagonDirection roadDirection: hexagon.getRoadDirection()) {
                Hexagon newSibling1 = newMapSkeleton.getHexagonByDirection(newHexagon, roadDirection);
                newMapSkeleton.addBlock(newHexagon, newSibling1);
                newMapSkeleton.addBlock(newSibling1, newHexagon);

                Hexagon newSibling2 = newMapSkeleton.getHexagonByDirection(newSibling1, roadDirection);
                newMapSkeleton.addBlock(newSibling1, newSibling2);
                newMapSkeleton.addBlock(newSibling2, newSibling1);
            }
        }
        return newMapSkeleton;
    }

    /**
     * 生成地形，view，interactive， lamp
     * TODO 需要处理blockGroup过长的问题, 在某些地形上是跑不完的
     * TODO 可能不同的地形在extra distance rate上并不是完全错开的，而是有重叠和随机的
     * TODO 可能不需要这么多的地形，1/2应该就足够, 可以开放一些近路用来保证不确定性
     */
    public void generateTerrain() {
        generateOpenArea();

        InteractiveType[] singleInteractiveType = new InteractiveType[] {
                InteractiveType.FIRE_FENCE,
                InteractiveType.GATE,
                InteractiveType.RIVER,
                InteractiveType.STONES
        };

        Map<String, Integer> pointPressMap = new HashMap<>();
        for (Branch branch: branchSet) {
            System.out.println("distance:" + branch.distance() + " edr:" + branch.getExtraDistanceRate());

            SpecialSectionType tType = null;
            // 获取当前branch的特殊地段类型
            if (branch.distance() >= SpecialSection.MIN_SIZE && branch.getExtraDistanceRate() > 20) {
                if (branch.getExtraDistanceRate() > 200) tType = SpecialSectionType.WIND;
                else if (branch.getExtraDistanceRate() > 150) tType = SpecialSectionType.DREAM;
                else if (branch.getExtraDistanceRate() > 100) tType = SpecialSectionType.SNOW;
                else if (branch.getExtraDistanceRate() > 60) tType = SpecialSectionType.RAIN;
                else if (branch.getExtraDistanceRate() > 20) tType = SpecialSectionType.FOG;

                // for test
                tType = SpecialSectionType.TIGER;
            }

            // TODO
            RoadPoint[] roadPoints = null;
            //RoadPoint[] roadPoints = getAllRoadPoint(branch);
            if (tType != null) {
                short specialSectionId = getNextSpecialSectionId();
                if (tType == SpecialSectionType.TIGER) interactiveSet.add(
                        new InteractiveInfo(InteractiveType.TIGER, new Point3(0, 0, 0), 0, specialSectionId));
                else if (tType == SpecialSectionType.WOLF) interactiveSet.add(
                        new InteractiveInfo(InteractiveType.WOLF, new Point3(0, 0, 0), 0, specialSectionId));

                int prevId = 0;
                int count = 0;

                specialSectionMap.put(specialSectionId, new SpecialSection(specialSectionId, tType, roadPoints));
                for (int i = 5; i < roadPoints.length - 5; i ++) {
                    expandRoadPoints(roadPoints[i], specialSectionId);
                    if (roadPoints[i].getDivisionPos() != 0 && roadPoints[i].getDivisionPos() != RoadSection.SUB_SECTION_SIZE / 2) continue;
                    count ++;

                    // create view
                    ViewType sType = getViewType(tType);
                    if (sType != null) {
                        View view = new View(sType, roadPoints[i].getPoint().toPoint3(),
                                (int)Math.toDegrees(roadPoints[i].getDeflection()) * 1000);
                        viewMap.put(view.getId(), view);
                    }

                    /*
                    // create lamp
                    int currLampId = 0;
                    Point3 position = new Point3(roadPoints[i].getPoint().x, 0, roadPoints[i].getPoint().y);
                    if (tType == SpecialSectionType.WOLF || tType == SpecialSectionType.TIGER) {
                        if (count % 10 == 0){
                            interactiveSet.add(new InteractiveInfo(
                                    InteractiveType.CAMP_FIRE, position, - roadPoints[i].getDegree()));
                            currLampId = addLamp(pointPressMap, LampType.INVISIBLE, roadPoints[i].getPoint());
                        }
                        else if (count % 2 == 0) {
                            currLampId = addLamp(pointPressMap, LampType.TORCH, roadPoints[i].getPoint());
                        }
                    } else if (tType == SpecialSectionType.DARK) {
                        currLampId = addLamp(pointPressMap, LampType.INVISIBLE, roadPoints[i].getPoint());
                    } else if (tType == SpecialSectionType.OPEN_LAND || tType == SpecialSectionType.GATES) {

                    } else {
                        if (roadPoints[i].getDivisionPos() == 0 ) {
                            currLampId = addLamp(pointPressMap, LampType.NORMAL, roadPoints[i].getPoint());
                        } else {
                            currLampId = addLamp(pointPressMap, LampType.NORMAL_SMALL, roadPoints[i].getPoint());
                        }
                    }

                    if (currLampId != 0 && prevId != 0) {
                        lampMap.get(prevId).addSiblings(currLampId);
                        lampMap.get(currLampId).addSiblings(prevId);
                    }
                    prevId = currLampId;
                    */
                }
            } else {
                // 不满足特殊地形的条件下
                if (branch.distance() < SpecialSection.MIN_SIZE && branch.getExtraDistanceRate() > 50) {
                    Hexagon[] hexagons = branch.getCenterHexagons();
                    Segment segment = getSegment(hexagons[0], hexagons[1]);

                    //RoadPoint roadPoint = segment.getMiddleRoadSection().getMiddleRPoint();
                    // TODO
                    RoadPoint roadPoint = null;

                    InteractiveType itrType = singleInteractiveType[
                            ThreadLocalRandom.current().nextInt(singleInteractiveType.length)];
                    interactiveSet.add(new InteractiveInfo(
                            itrType, roadPoint.getPoint().toPoint3(), - roadPoint.getDegree()));
                }

                /*
                int prevId = 0;
                for (RoadPoint roadPoint: roadPoints) {
                    if (roadPoint.getDivisionPos() != 0 && roadPoint.getDivisionPos() != RoadSection.SUB_SECTION_SIZE / 2) continue;
                    int currLampId;
                    if (roadPoint.getDivisionPos() == 0 ) {
                        currLampId = addLamp(pointPressMap, LampType.NORMAL, roadPoint.getPoint());
                    } else {
                        currLampId = addLamp(pointPressMap, LampType.NORMAL_SMALL, roadPoint.getPoint());
                    }


                    if (prevId != 0) {
                        lampMap.get(prevId).addSiblings(currLampId);
                        lampMap.get(currLampId).addSiblings(prevId);
                    }
                    prevId = currLampId;
                }
                */
            }
        }
    }

    private void doSegment() {
        for (Hexagon hexagon: occupiedMap.values()) {
            for (Hexagon sibling: getRoadSibling(hexagon)) {
                if (hexagon.getId() > sibling.getId()) continue;
                // TODO 可能有溢出问题
                int id = hexagon.getId() * 10000 + sibling.getId();
                segmentMap.put(id, new Segment(hexagon, sibling));
            }
        }

        /*
        Map<Hexagon, List<Segment>> forkMap = new HashMap<>();
        for (Segment segment1: segmentMap.values()) {
            for (Segment segment2: segmentMap.values()) {
                if (!vertexSet.contains(segment1.getH1())) {
                    if (segment1.getH1().getId() == segment2.getH2().getId()) {
                        double deflection = (segment1.getFirstRoadSection().getStart().getDeflection() +
                                segment2.getLastRoadSection().getEnd().getDeflection()) / 2;
                        segment1.getFirstRoadSection().resetP1Deflection(deflection);
                        segment2.getLastRoadSection().resetP2Deflection(deflection);
                    }
                    if (segment1.getH1().getId() == segment2.getH1().getId()) {
                        double deflection = (segment1.getFirstRoadSection().getStart().getDeflection() +
                                segment2.getFirstRoadSection().getStart().getDeflection()) / 2;
                        segment1.getFirstRoadSection().resetP1Deflection(deflection + Math.PI);
                        segment2.getFirstRoadSection().resetP1Deflection(deflection);
                    }
                }
                if (!vertexSet.contains(segment1.getH2())) {
                    if (segment1.getH2().getId() == segment2.getH1().getId()) {
                        double deflection = (segment1.getLastRoadSection().getEnd().getDeflection() +
                                segment2.getFirstRoadSection().getStart().getDeflection()) / 2;
                        segment1.getLastRoadSection().resetP2Deflection(deflection);
                        segment2.getFirstRoadSection().resetP1Deflection(deflection);
                    }
                    if (segment1.getH2().getId() == segment2.getH2().getId()) {
                        double deflection = (segment1.getLastRoadSection().getEnd().getDeflection() +
                                segment2.getLastRoadSection().getEnd().getDeflection()) / 2;
                        segment1.getLastRoadSection().resetP2Deflection(deflection + Math.PI);
                        segment2.getLastRoadSection().resetP2Deflection(deflection);
                    }
                }
            }

            if (!forkMap.containsKey(segment1.getH1()))
                forkMap.put(segment1.getH1(), new LinkedList<>());
            if (!forkMap.containsKey(segment1.getH2()))
                forkMap.put(segment1.getH2(), new LinkedList<>());

            forkMap.get(segment1.getH1()).add(segment1);
            forkMap.get(segment1.getH2()).add(segment1);
        }

        for (Hexagon hexagon: vertexSet) {
            List<Segment> segmentList = forkMap.get(hexagon);
            if (segmentList.size() != 3) continue;

            double[] roadDeflections = new double[3];
            int index = 0;
            for (Segment segment: segmentList) {
                if (hexagon.getId() == segment.getH1().getId())
                    roadDeflections[index ++] = segment.getFirstRoadSection().getStart().getDeflection();
                else {
                    double value = segment.getLastRoadSection().getEnd().getDeflection() + Math.PI;
                    roadDeflections[index ++] = value > 2 * Math.PI ? (value - 2 * Math.PI) : value;
                }
            }

            double[] deflection = new double[3];
            for (int i = 0; i < 3; i ++) {
                int j = (i + 1) > 2 ? 0 : i + 1;
                if (Math.abs(roadDeflections[i] - roadDeflections[j]) > Math.PI)
                    deflection[i] = (roadDeflections[i] + roadDeflections[j]) / 2 + Math.PI + Math.PI / 2;
                else
                    deflection[i] = (roadDeflections[i] + roadDeflections[j]) / 2 + Math.PI / 2;
            }

            RoadPoint[] roadPoints = new RoadPoint[3];
            for (int i = 0; i < 3; i ++) {
                roadPoints[i] = new RoadPoint(Point2.getEdge(hexagon.coordinatePoint(),
                        deflection[i], RoadSection.ROAD_WIDTH * 3 / 2)[1], deflection[i]);
            }
            branchCrossPoint.put(hexagon, roadPoints);

            for (Segment segment: segmentList) {
                if (hexagon.getId() == segment.getH1().getId()) segment.getFirstRoadSection().setEdgeOfStart(roadPoints);
                else segment.getLastRoadSection().setEdgeOfEnd(roadPoints);
            }
        }
        */
    }

    protected void addBlock(Hexagon hexagon, Hexagon fromHexagon) {
        if (hexagon == null) return;

        if (fromHexagon != null) {
            hexagon.setBridges(hexagon.getDirection(fromHexagon), true);
            fromHexagon.setBridges(fromHexagon.getDirection(hexagon), true);
        }

        if (occupiedMap.containsKey(hexagon)) return;

        occupiedMap.put(hexagon.getId(), hexagon);
        freeMap.remove(hexagon.getId());

        if (fromHexagon != null) {
            //System.out.println("add Block: [" + hexagon.getId() + ", " + fromHexagon.getId() + "]");
        } else {
            //System.out.println("add Block: [" + hexagon.getId() + ", null]");
        }
    }

    /**
     * 删除不需要的branch
     * 1:起点和终点相同的
     * 2:起点和终点间有多条路线，保留最长的那条，其余删除
     *
     * @param step 1:trim 2:calculate
     */
    public void trimAndCalculate(int step) {
        // get vertex set
        vertexSet.clear();
        branchSet.clear();

        vertexSet.add(occupiedMap.get(start));
        vertexSet.add(occupiedMap.get(end));

        for (Hexagon hexagon: occupiedMap.values()) {
            if (hexagon.bridgeCount() > 2) {
                vertexSet.add(hexagon);
            }
        }

        System.out.println("vertex set count: " + vertexSet.size());
        for (Hexagon hexagon: vertexSet) {
            System.out.println("vertex - " + hexagon.getId() + ": [" + hexagon.getX() + "," + hexagon.getY() + "]");
        }

        // get branch set
        for (Hexagon hexagon: vertexSet) {
            for (Hexagon current : getOccupiedSibling(hexagon)) {
                if (vertexSet.contains(current)) {
                    if (hexagon.getId() < current.getId()) {
                        addBranch(new Branch(hexagon, current));
                    }
                } else {
                    List<Hexagon> blockList = new LinkedList<>();
                    blockList.add(current);

                    boolean reachEnd = false;
                    while (!reachEnd) {
                        boolean isAdd = false;
                        for (Hexagon sibling : getOccupiedSibling(current)) {
                            if (blockList.contains(sibling)) {
                                continue;
                            }
                            else if (blockList.size() == 1 && sibling.getId() == hexagon.getId()) {
                                continue;
                            }
                            else if (vertexSet.contains(sibling)) {
                                if (hexagon.getId() <= sibling.getId()) {
                                    Branch branch = new Branch(hexagon, sibling);
                                    branch.hexagonList = blockList;
                                    addBranch(branch);
                                }
                                reachEnd = true;
                                break;
                            } else {
                                blockList.add(sibling);
                                current = sibling;
                                isAdd = true;
                                break;
                            }
                        }

                        if (!isAdd) reachEnd = true;
                    }
                }
            }
        }

        // 把曲折的道路变直
        /*
        for (Branch branch: branchSet) {
            for (int i = 0; i <= branch.hexagonList.size() - 4; i ++) {
                Hexagon center1 = getFreeCenterHexagon(
                        branch.hexagonList.get(i), branch.hexagonList.get(i + 1),
                        branch.hexagonList.get(i + 2), branch.hexagonList.get(i + 3));

                Hexagon center2 = null;
                if (i < branch.hexagonList.size() - 4) {
                    center2 = getFreeCenterHexagon(
                            branch.hexagonList.get(i), branch.hexagonList.get(i + 1), branch.hexagonList.get(i + 2),
                            branch.hexagonList.get(i + 3), branch.hexagonList.get(i + 4));
                } else {
                    center2 = getFreeCenterHexagon(
                            branch.hexagonList.get(i), branch.hexagonList.get(i + 1), branch.hexagonList.get(i + 2),
                            branch.hexagonList.get(i + 3), branch.tail);
                }

                Hexagon center3 = null;
                if (i > 0) {
                     center3 = getFreeCenterHexagon(
                            branch.hexagonList.get(i - 1), branch.hexagonList.get(i), branch.hexagonList.get(i + 1),
                            branch.hexagonList.get(i + 2), branch.hexagonList.get(i + 3));
                } else {
                    center3 = getFreeCenterHexagon(
                            branch.head, branch.hexagonList.get(i), branch.hexagonList.get(i + 1),
                            branch.hexagonList.get(i + 2), branch.hexagonList.get(i + 3));
                }

                if (center1 == null || (center1 != null && center2 != null) || (center1 != null && center3 != null))
                    continue;

                if (ThreadLocalRandom.current().nextBoolean()) continue;

                Hexagon h1 = branch.hexagonList.get(i + 1);
                Hexagon h2 = branch.hexagonList.get(i + 2);
                Hexagon h3 = branch.hexagonList.get(i + 3);

                branch.hexagonList.remove(h1);
                branch.hexagonList.remove(h2);
                branch.hexagonList.add(i + 1, center1);

                removeBlock(h1);
                removeBlock(h2);
                addBlock(center1, branch.hexagonList.get(i));
                addBlock(h3, center1);

                i = i + 3;
            }
        }
        */

        if (step > 1) {
            int[] blockIds = new int[vertexSet.size()];
            int index = 0;
            for (Hexagon hexagon: vertexSet) {
                blockIds[index++] = hexagon.getId();
            }

            Graph graph = new Graph(blockIds);
            for (Branch branch : branchSet) {
                graph.setEdge(branch.head.getId(), branch.tail.getId(), branch.hexagonList.size() + 1);
            }

            for (Branch branch : branchSet) {
                int detourDistance = graph.countDetourDistance(branch.head.getId(), branch.tail.getId());
                branch.detourDistance = detourDistance;
            }

            distancesToEnd = graph.countDistanceWithVertexId(end);
            pathMap = graph.countPath();

            // 按branch长度排序后打印, 用来调试，可以注释
            Branch[] branches = new Branch[branchSet.size()];
            int i = 0;
            for (Branch branch : branchSet) {
                int m = i;
                for (int j = 0; j < i; j ++) {
                    if (branch.hexagonList.size() >= branches[j].hexagonList.size()) continue;
                    else {
                        m = j;
                        break;
                    }
                }
                for (int k = i; k > m; k --) {
                    branches[k] = branches[k - 1];
                }
                branches[m] = branch;
                i ++;
            }

            for (Branch branch : branches) {
                System.out.println("branch: " + branch);
            }
        }
    }

    /*
    private RoadPoint[] getAllRoadPoint(Branch branch) {
        List<RoadPoint> roadPointList = new LinkedList<>();
        Hexagon h1, h2;
        for (int i = 0; i < branch.distance(); i ++) {
            if (i == 0) h1 = branch.head;
            else h1 = branch.hexagonList.get(i - 1);
            if (i == branch.distance() - 1) h2 = branch.tail;
            else h2 = branch.hexagonList.get(i);

            Segment segment = getSegment(h1, h2);
            List<RoadPoint> roadPoints = segment.getAllRoadPoint();
            if (h1 != segment.getH1()) {
                Collections.reverse(roadPoints);
            }
            if (i == 0) roadPointList.addAll(roadPoints);
            else roadPointList.addAll(roadPoints.subList(1, roadPoints.size()));
        }
        return roadPointList.toArray(new RoadPoint[]{});
    }
    */

    /**
     * 把特殊路段的blockId和特殊路段Id的对应关系保存起来
     * @param roadPoint
     * @param specialSectionId
     */
    private void expandRoadPoints(RoadPoint roadPoint, short specialSectionId) {
        int x = roadPoint.getPoint().x;
        int y = roadPoint.getPoint().y;
        int offset = RoadSection.ROAD_WIDTH;
        for (int i = x - offset; i <= x + offset; i += 1000) {
            for (int j = y - offset; j <= y + offset; j += 1000) {
                pointMap.put(new Point2((i / 1000) * 1000, (j / 1000) * 1000), specialSectionId);
                pointIdMap.put(getPointId(i / 1000, j / 1000), specialSectionId);
            }
        }
    }

    private Integer getPointId(int i, int j) {
        // TODO 暂时设定地图宽度不超过40000
        return i * 40000 + j;
    }

    private void addBranch(Branch branch) {
        if (branch.head == branch.tail) {
            removeBranch(branch);
            return;
        }

        for(Branch existBranch: branchSet) {
            if (existBranch.head.getId() == branch.head.getId() &&
                    existBranch.tail.getId() == branch.tail.getId()) {
                if (existBranch.distance() > branch.distance()) {
                    removeBranch(branch);
                } else {
                    removeBranch(existBranch);
                    branchSet.add(branch);
                }
                return;
            }
        }
        branchSet.add(branch);
    }

    private Hexagon getHexagonByDirection(Hexagon hexagon, HexagonDirection direction) {
        return hexagonMap.get(getHexagonIdByDirection(hexagon, direction));
    }

    private int getHexagonIdByDirection(Hexagon hexagon, HexagonDirection direction) {
        int x = hexagon.getX();
        int y = hexagon.getY();
        if (hexagon.getY() % 2 == 0) {
            if (direction == HexagonDirection.RIGHT) {
                if (isInBound(x + 1, y)) return getHexagonId(x + 1, y);
            } else if (direction == HexagonDirection.DOWN_RIGHT) {
                if (isInBound(x + 1, y - 1)) return getHexagonId(x + 1, y - 1);
            } else if (direction == HexagonDirection.DOWN_LEFT) {
                if (isInBound(x, y - 1)) return getHexagonId(x, y - 1);
            } else if (direction == HexagonDirection.LEFT) {
                if (isInBound(x - 1, y)) return getHexagonId(x - 1, y);
            } else if (direction == HexagonDirection.UP_LEFT) {
                if (isInBound(x, y + 1)) return getHexagonId(x, y + 1);
            } else if (direction == HexagonDirection.UP_RIGHT) {
                if (isInBound(x + 1, y + 1)) return getHexagonId(x + 1, y + 1);
            }
        } else {
            if (direction == HexagonDirection.RIGHT) {
                if (isInBound(x + 1, y)) return getHexagonId(x + 1, y);
            } else if (direction == HexagonDirection.DOWN_RIGHT) {
                if (isInBound(x, y - 1)) return getHexagonId(x, y - 1);
            } else if (direction == HexagonDirection.DOWN_LEFT) {
                if (isInBound(x - 1, y - 1)) return getHexagonId(x - 1, y - 1);
            } else if (direction == HexagonDirection.LEFT) {
                if (isInBound(x - 1, y)) return getHexagonId(x - 1, y);
            } else if (direction == HexagonDirection.UP_LEFT) {
                if (isInBound(x - 1, y + 1)) return getHexagonId(x - 1, y + 1);
            } else if (direction == HexagonDirection.UP_RIGHT) {
                if (isInBound(x, y + 1)) return getHexagonId(x, y + 1);
            }
        }
        return -1;
    }

    private Hexagon[] getSibling(Hexagon hexagon) {
        Hexagon[] hexagons = new Hexagon[6];
        int index = 0;
        for (HexagonDirection direction: HexagonDirection.values()) {
            if (direction == HexagonDirection.FREE) continue;
            int hexagonId = getHexagonIdByDirection(hexagon, direction);
            if (hexagonId > 0) {
                hexagons[index ++] = hexagonMap.get(hexagonId);
            }
        }

        if (index == 6) return hexagons;
        else return Arrays.copyOf(hexagons, index);
    }

    private Hexagon getOccupiedSibling(Hexagon hexagon, HexagonDirection direction) {
        int hexagonId = getHexagonIdByDirection(hexagon, direction);
        return occupiedMap.get(hexagonId);
    }

    private Hexagon[] getOccupiedSibling(Hexagon hexagon) {
        Hexagon[] hexagons = new Hexagon[hexagon.bridgeCount()];
        int index = 0;
        for (HexagonDirection direction: hexagon.getRoadDirection()) {
            int hexagonId = getHexagonIdByDirection(hexagon, direction);
            hexagons[index ++] = occupiedMap.get(hexagonId);
        }
        return hexagons;
    }

    private Hexagon getRandomFreeSibling(Hexagon hexagon) {
        Hexagon[] siblings = new Hexagon[6];
        int siblingCount = 0;

        //for (HexagonDirection direction: HexagonDirection.values()) {
        for (HexagonDirection direction: hexagon.getPossibleRoadDirection()) {
            if (direction == null || direction == HexagonDirection.FREE) continue;
            int hexagonId = getHexagonIdByDirection(hexagon, direction);
            if (hexagonId != -1 && freeMap.containsKey(hexagonId)) {
                if (hexagonId == start || hexagonId == end) {
                    return freeMap.get(hexagonId);
                }
                siblings[siblingCount ++] = freeMap.get(hexagonId);
            }
        }

        if (siblingCount == 0) return null;
        else return siblings[ThreadLocalRandom.current().nextInt(siblingCount)];
    }

    public List<Hexagon> getRoadSibling(Hexagon hexagon) {
        List<Hexagon> roadSiblingList = new LinkedList<>();
        for (HexagonDirection direction: HexagonDirection.values()) {
            if (direction == HexagonDirection.FREE) continue;
            if (hexagon.getBridge(direction)) roadSiblingList.add(
                    occupiedMap.get(getHexagonIdByDirection(hexagon, direction)));
        }

        for (Hexagon hexagon1: roadSiblingList) {
            if (hexagon1 == null) {
                int a  = 1;
            }
        }
        return roadSiblingList;
    }

    public int getSegmentId(int id1, int id2) {
        if (id1 <= id2) return id1 * 10000 + id2;
        else return id2 * 10000 + id1;
    }

    public List<Segment> getSegments(Hexagon h) {
        List<Segment> segments = new LinkedList<>();
        for (Segment segment: segmentMap.values()) {
            if (segment.getH1() == h || segment.getH2() == h) segments.add(segment);
        }
        return segments;
    }

    public Segment getSegment(Hexagon h1, Hexagon h2) {
        return segmentMap.get(getSegmentId(h1.getId(), h2.getId()));
    }

    public Segment getSegment(int id1, int id2) {
        return segmentMap.get(getSegmentId(id1, id2));
    }

    /**
     * 返回一个随机的空白点
     * @return
     */
    protected Hexagon randomUnusedHexagon() {
        if (freeMap == null || freeMap.size() == 0) return null;
        int choice = ThreadLocalRandom.current().nextInt(freeMap.size());
        for (Hexagon hexagon: freeMap.values()) {
            if (choice -- == 0) {
                return hexagon;
            }
        }
        return null;
    }

    public void init() {
        hexagonMap = new HashMap<>();
        occupiedMap = new HashMap<>();
        freeMap = new HashMap<>();

        for (int height = 1; height <= bound; height ++) {
            for (int width = 1; width <= bound; width ++) {
                Hexagon hexagon = new Hexagon(getHexagonId(width, height), width, height);
                hexagonMap.put(hexagon.getId(), hexagon);
                freeMap.put(hexagon.getId(), hexagon);
            }
        }
    }

    private String pointPress(Point2 point) {
        return Integer.toString(point.x) + "-" + Integer.toString(point.y);

    }

    public int getHexagonId(int x, int y) {
        return (y - 1) * bound + x;
    }

    private boolean isInBound(int x, int y) {
        if (x < 1 || y < 1 || x > bound || y > bound) return false;
        return true;
    }

    public boolean isContainStartEnd() {
        if (!occupiedMap.containsKey(start) || !occupiedMap.containsKey(end)) return false;
        return true;
    }

    public int length() {
        return occupiedMap.size();
    }

    public int getDistanceToEnd(int id) {
        return distancesToEnd.get(id);
    }

    public HexagonDirection getRoadDirectionToEnd(int hexagonId) {
        return getRoadDirection(hexagonId, end);
    }

    public HexagonDirection getRoadDirection(int fromHexagonId, int toHexagonId) {
        if (!isCrossPoint(fromHexagonId) || !isCrossPoint(toHexagonId)) return null;

        List<Integer> path = getPath(fromHexagonId, toHexagonId);
        if (path == null || path.size() == 0) return null;

        int firstWayPointId = path.get(1);
        Branch branch = getBranch(fromHexagonId, firstWayPointId);
        if (branch == null) return null;

        int nextHexagonId;
        if (fromHexagonId == branch.head.getId()) {
            if (branch.distance() == 1) {
                nextHexagonId = branch.tail.getId();
            } else {
                nextHexagonId = branch.hexagonList.get(0).getId();
            }
        } else {
            if (branch.distance() == 1) {
                nextHexagonId = branch.head.getId();
            } else {
                nextHexagonId = branch.hexagonList.get(branch.hexagonList.size() - 1).getId();
            }
        }
        return getOccupiedHexagon(fromHexagonId).getDirection(getOccupiedHexagon(nextHexagonId));
    }

    public Hexagon getOccupiedHexagon(int id) {
        return occupiedMap.get(id);
    }

    public Hexagon getHexagon(int x, int y) {
        if (!isInBound(x, y)) return null;
        return hexagonMap.get(getHexagonId(x, y));
    }

    public Hexagon getHexagon(int id) {
        return hexagonMap.get(id);
    }

    /**
     * 获取从节点a到节点b的所有crossPoint的list集合
     */
    private List<Integer> getPath(int fromHexagonId, int toHexagonId) {
        Map<Integer, List<Integer>> pointPath = pathMap.get(fromHexagonId);
        if (pointPath != null)  return pointPath.get(toHexagonId);
        return null;
    }

    /**
     * 判断是否为交叉点
     */
    public boolean isCrossPoint(int hexagonId) {
        return vertexSet.contains(getOccupiedHexagon(hexagonId));
    }

    /**
     * 根据起点和终点获取branch
     */
    private Branch getBranch(int startBlockId, int endBlockId) {
        for  (Branch branch: branchSet) {
            if ((branch.head.getId() == startBlockId && branch.tail.getId()  == endBlockId) ||
                    (branch.head.getId()  == endBlockId && branch.tail.getId() == startBlockId) ) {
                return branch;
            }
        }
        return null;
    }

    public short getNextSpecialSectionId() {
        if (nextGroupId == Short.MAX_VALUE) {
            throw new RuntimeException("invalid group objectId");
        }
        return nextGroupId ++;
    }

    public ViewType getViewType(SpecialSectionType ssType) {
        if (ssType == SpecialSectionType.FOG) return ViewType.FOG;
        if (ssType == SpecialSectionType.SNOW) return ViewType.SNOW;
        if (ssType == SpecialSectionType.RAIN) return ViewType.RAIN;
        if (ssType == SpecialSectionType.DREAM) return ViewType.DREAM;
        return null;
    }

    public SpecialSection getSpecialSection(Point3 p) {
        int pointId = getPointId(p.x / 1000, p.z / 1000);
        if (pointIdMap.containsKey(pointId) && specialSectionMap.containsKey(pointIdMap.get(pointId)))
            return specialSectionMap.get(pointIdMap.get(pointId));
        else return null;
    }

    /**
     * 获取一个随机的roadPoint, roadPoint是指生成在地图上生成各种游戏对象的固定位置
     * @param type roadPoint类型, 路中央还是路旁边
     * @return
     */
    public RoadPoint getRandomRoadPoint(RoadPointType type) {
        return  null;
        /*
        int index = ThreadLocalRandom.current().nextInt(segmentMap.size());
        Segment chosen = null;
        for (Segment segment: segmentMap.values()) {
            if ((-- index) == 0) chosen = segment;
        }

        // TODO if position is taken by others
        return chosen.getRandomRoadPoint(type);
        */
    }

    public List<Segment> getRandomEndpointSegment(float percent) {
        if (percent > 1) percent = 1;
        else if (percent < 0) percent = 0;

        List<Hexagon> hexagonList = new LinkedList<>();
        for (Hexagon vertex: vertexSet) {
            hexagonList.add(vertex);
        }
        Collections.shuffle(hexagonList);
        List<Hexagon> selectedList = hexagonList.subList(0, (int)(hexagonList.size() * percent));
        List<Segment> segmentList = getSegments(selectedList);
        return segmentList;
    }

    public List<Segment> getSegments(List<Hexagon> vertexList) {
        List<Segment> segmentList = new LinkedList<>();
        for (Segment segment : segmentMap.values()) {
            if (vertexList.contains(segment.getH1()) || vertexList.contains((segment.getH2())))
                segmentList.add(segment);
        }
        return segmentList;
    }

    /**
     * 获得起点坐标
     */
    public Point2 getStart() {
        return occupiedMap.get(start).coordinatePoint();
    }

    /**
     * 获得终点坐标
     */
    public Point2 getEnd() {
        return occupiedMap.get(end).coordinatePoint();
    }

    /**
     * 获取随机比例的交叉点
     * @param percent 比例值
     * @return
     */
    public Map<Hexagon, RoadPoint[]> randomBranchCrossList(float percent) {
        List<Hexagon> allRoadPoints = new LinkedList<>();
        for (Hexagon hexagon: branchCrossPoint.keySet()) {
            allRoadPoints.add(hexagon);
        }

        Collections.shuffle(allRoadPoints);
        Map<Hexagon, RoadPoint[]> subCrossPointMap = new HashMap<>();
        for (Hexagon hexagon: allRoadPoints.subList(0, (int)(allRoadPoints.size() * percent))) {
            subCrossPointMap.put(hexagon, branchCrossPoint.get(hexagon));
        }
        return subCrossPointMap;
    }

    public  byte[] toBytesV2() {
        ByteBuilder byteBuilder = new ByteBuilder();
        byteBuilder.append(bound);
        for (int x = 1; x <= bound; x ++) {
            for (int y = 1; y <= bound; y ++) {
                Hexagon hexagon = hexagonMap.get(getHexagonId(x, y));
                byteBuilder.append(hexagon.getTerrainType().getValue());
                if (isRoad(hexagon.getTerrainType())) {
                    byteBuilder.append(hexagon.getBridgeByte());
                }
            }
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        GZIPOutputStream gzip;
        try {
            gzip = new GZIPOutputStream(out);
            gzip.write(byteBuilder.getBytes());
            gzip.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        byte[] mapBytes = out.toByteArray();

        System.out.println("bytes: " + mapBytes.length);
        int line = 1;
        for (byte blockByte: mapBytes) {
            System.out.print(blockByte & 0xFF);
            System.out.print(",");

            if (line ++ % 100 == 0) {
                System.out.print("\n");
            }
        }
        System.out.print("\n");

        return mapBytes;
    }

    public byte[] toBytes() {
        Map<Integer, int[]> segmentIndexIdMap = new HashMap<>();

        int currentIndex = 0;
        for (Hexagon hexagon: occupiedMap.values()) {
            if (currentIndex <= hexagon.getId()) currentIndex = hexagon.getId() + 1;
        }

        System.out.println("current index: " + currentIndex);

        for (Segment segment: segmentMap.values()) {
            /*
            segmentIndexIdMap.put(segment.getId(), new int[segment.getWayPoints().length]);
            for (int i = 0; i < segment.getWayPoints().length; i ++) {
                segmentIndexIdMap.get(segment.getId())[i] = currentIndex ++;
            }
            */
        }
        System.out.println("current index: " + currentIndex);

        ByteBuilder byteBuilder = new ByteBuilder();
        int roadHexagonSize = 0;
        for (Hexagon hexagon: occupiedMap.values()) {
            // 开阔地的六边形数据在openArea中, 不需要在这里同步
            if (hexagon.getOpenAreaId() > 0) continue;
            roadHexagonSize ++;
        }
        byteBuilder.append(roadHexagonSize);
        for (Hexagon hexagon: occupiedMap.values()) {
            // 开阔地的六边形数据在openArea中, 不需要在这里同步
            if (hexagon.getOpenAreaId() > 0) continue;
            byteBuilder.append((short) hexagon.getId());
            byteBuilder.append(hexagon.coordinateX());
            byteBuilder.append(hexagon.coordinateY());
            byteBuilder.append((byte) hexagon.bridgeCount());
            for (HexagonDirection direction: hexagon.getRoadDirection()) {
                //byteBuilder.append((short)getHexagonIdByDirection(hexagon, direction));

                /*
                int siblingId = getHexagonIdByDirection(hexagon, direction);
                Segment segment = segmentMap.get(Segment.getId(hexagon.getId(), siblingId));
                int index = segment.getAdjacentPointIndex(hexagon.getId());
                int id = segmentIndexIdMap.get(segment.getId())[index];
                byteBuilder.append((short)id);
                */
            }
        }

        byteBuilder.append(segmentMap.size());
        for (Segment segment: segmentMap.values()) {
        }

        byteBuilder.append(openAreaList.size());
        for (OpenArea openArea: openAreaList) {
            openArea.appendBytes(byteBuilder);
        }

        byteBuilder.append(specialSectionMap.size());
        for (SpecialSection specialSection: specialSectionMap.values()) {
            byteBuilder.append(specialSection.getId());
            byteBuilder.append(specialSection.getType().getValue());
            int size = 0;
            for (RoadPoint roadPoint: specialSection.getRoadPoints()) {
                if (roadPoint.getDivisionPos() == RoadSection.SUB_SECTION_SIZE / 2) {
                    size ++;
                }
            }

            // 起点和终点
            int roadPointsSize = specialSection.getRoadPoints().length;
            byteBuilder.append(specialSection.getRoadPoints()[0].getPoint().x);
            byteBuilder.append(specialSection.getRoadPoints()[0].getPoint().y);
            byteBuilder.append(specialSection.getRoadPoints()[roadPointsSize - 1].getPoint().x);
            byteBuilder.append(specialSection.getRoadPoints()[roadPointsSize - 1].getPoint().y);

            byteBuilder.append(size);
            for (RoadPoint roadPoint: specialSection.getRoadPoints()) {
                if (roadPoint.getDivisionPos() == RoadSection.SUB_SECTION_SIZE / 2) {
                    byteBuilder.append(roadPoint.getPoint().x);
                    byteBuilder.append(roadPoint.getPoint().y);
                }
            }
        }

        /*
        byteBuilder.append(lampMap.size());
        for (Lamp lamp: lampMap.values()) {
            lamp.getBytes(byteBuilder);
        }
        */

        byteBuilder.append(viewMap.size());
        for (View view: viewMap.values()) {
            byteBuilder.append(view.getId());
            byteBuilder.append(view.getType().getValue());
            byteBuilder.append(view.getPoint3().x);
            byteBuilder.append(view.getPoint3().z);
            byteBuilder.append(view.getRotateY());
        }

        byteBuilder.append(interactiveSet.size());
        for (InteractiveInfo interactiveInfo: interactiveSet) {
            byteBuilder.append(interactiveInfo.getType().getValue());
            byteBuilder.append(interactiveInfo.getSpecialSectionId());
            byteBuilder.append(interactiveInfo.getPoint3().x);
            byteBuilder.append(interactiveInfo.getPoint3().z);
            byteBuilder.append(interactiveInfo.getRotateY());
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        GZIPOutputStream gzip;
        try {
            gzip = new GZIPOutputStream(out);
            gzip.write(byteBuilder.getBytes());
            gzip.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        byte[] mapBytes = out.toByteArray();

        System.out.println("bytes: " + mapBytes.length);
        int line = 1;
        for (byte blockByte: mapBytes) {
            System.out.print(blockByte & 0xFF);
            System.out.print(",");

            if (line ++ % 100 == 0) {
                System.out.print("\n");
            }
        }
        System.out.print("\n");

        return mapBytes;
    }
}
