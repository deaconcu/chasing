package com.prosper.chasing.game.old;

/**
 * Created by deacon on 2018/4/25.
 */
public class MarathonGameMapVersion3 {

    public void generate(int boundX, int boundY) {
        GameMapVersion3 gameMap = new GameMapVersion3(boundX, boundY);

        int startBlockId = gameMap.getBlockId(1, 1);
        int endBlockId = gameMap.getBlockId(boundX - 2, boundY - 2);

        gameMap.start = startBlockId;
        gameMap.end = endBlockId;

        gameMap.clear();
        gameMap.fillWithRandomRoads();
        gameMap.removeBrand(startBlockId, endBlockId);
        gameMap.printTerrainBlocks(4);

        int mergeCount = 8;
        for (int count = 0; count < mergeCount; count ++) {
            GameMapVersion3 gameMapForMerge = new GameMapVersion3(boundX, boundY);

            gameMapForMerge.start = startBlockId;
            gameMapForMerge.end = endBlockId;

            gameMapForMerge.clear();
            gameMapForMerge.fillWithRandomRoads();
            gameMapForMerge.removeBrand(startBlockId, endBlockId);
            gameMapForMerge.printTerrainBlocks(4);

            gameMap.merge(gameMapForMerge);
            gameMap.printTerrainBlocks(4);
        }
    }

    public static void main(String[] args) {
        int i = 1;
        while(i -- > 0) {
            MarathonGameMapVersion3 marathonGameMap = new MarathonGameMapVersion3();
            //marathonGameMap.generate(10, 10);
            marathonGameMap.generate(50, 50);
        }
    }
}
