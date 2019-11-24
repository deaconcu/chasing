package com.prosper.chasing.game.mapV3;

import com.prosper.chasing.game.util.ByteBuilder;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.*;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.zip.GZIPOutputStream;

/**
 * Created by deacon on 2019/6/26.
 */
public class PlanetMap {

    private static int SCALE = 100;

    private static class AreaConfig {
        int width;
        int height;
        int count;

        AreaConfig(int width, int height, int count) {
            this.width = width;
            this.height = height;
            this.count = count;
        }
    }

    private static class Area {
        int positionX;
        int positionY;
        byte rotation;
        byte areaTypeId;
        byte areaIndex;

        public Area(int positionX, int positionY, byte rotation, byte areaTypeId, byte areaIndex) {
            this.positionX = positionX;
            this.positionY = positionY;
            this.rotation = rotation;
            this.areaTypeId = areaTypeId;
            this.areaIndex = areaIndex;
        }

        public void appendBytes(ByteBuilder byteBuilder) {
            byteBuilder.append(positionX);
            byteBuilder.append(positionY);
            byteBuilder.append(rotation);
            byteBuilder.append(areaTypeId);
            byteBuilder.append(areaIndex);
        }
    }

    /**
     * 地形配置表，长度最大255
     */
    private static AreaConfig[] areaConfigs = new AreaConfig[] {
            new AreaConfig(5, 5, 1),
            new AreaConfig(4, 4, 1),
            new AreaConfig(3, 3, 1),
            //new AreaConfig(2, 4, 1),
            new AreaConfig(2, 2, 1),
            new AreaConfig(1, 1, 1),
    };

    private List<Area> areaList = new LinkedList<>();

    private int blockBound = 50;

    Set<Integer> freeBlockSet = new HashSet<>();

    public void generate() {
        for (int i = 1; i <= blockBound; i ++) {
            for (int j = 1; j <= blockBound; j ++) {
                freeBlockSet.add(getBlockId(i, j));
            }
        }

        List<Integer> possiblePosition = new LinkedList<>();
        for (byte index = 0; index < areaConfigs.length; index ++) {
            if (index == areaConfigs.length - 1) {
                AreaConfig areaConfig = areaConfigs[index];
                for (Integer positionId: freeBlockSet) {
                    int x = getX(positionId);
                    int y = getY(positionId);
                    byte rotation = (byte)ThreadLocalRandom.current().nextInt(4);
                    byte areaIndex = (byte)ThreadLocalRandom.current().nextInt(areaConfig.count);

                    int positionX = x * SCALE + areaConfig.width * SCALE  / 2;
                    int positionY = y * SCALE + areaConfig.height * SCALE  / 2;
                    byte terrainType = (byte)(index + 1);
                    areaList.add(new Area(positionX, positionY, rotation, terrainType, areaIndex));
                }
            } else {
                possiblePosition.clear();
                for (Integer id: freeBlockSet) {
                    possiblePosition.add(id);
                }

                AreaConfig areaConfig = areaConfigs[index];
                int generateCount = getCount(areaConfig.width, areaConfig.height);

                while (possiblePosition.size() > 0 && generateCount > 0) {
                    Integer positionId = possiblePosition.get(
                            ThreadLocalRandom.current().nextInt(possiblePosition.size()));
                    byte rotation = (byte)ThreadLocalRandom.current().nextInt(4);
                    int x = getX(positionId);
                    int y = getY(positionId);
                    if (rotation % 2 == 0) {
                        if (isValid(x, y, areaConfig.width, areaConfig.height)) {
                            for (int i = x; i < x + areaConfig.width; i ++) {
                                for (int j = y; j < y + areaConfig.height; j ++) {
                                    int blockId = getBlockId(i, j);
                                    freeBlockSet.remove(blockId);
                                    possiblePosition.remove((Object)blockId);
                                }
                            }
                            byte areaIndex = (byte)ThreadLocalRandom.current().nextInt(areaConfig.count);

                            int positionX = x * SCALE + areaConfig.width * SCALE  / 2;
                            int positionY = y * SCALE + areaConfig.height * SCALE  / 2;
                            byte terrainType = (byte)(index + 1);
                            areaList.add(new Area(positionX, positionY, rotation, terrainType, areaIndex));
                            generateCount --;
                        }
                    } else {
                        if (isValid(x, y, areaConfig.height, areaConfig.width)) {
                            for (int i = x; i < x + areaConfig.height; i ++) {
                                for (int j = y; j < y + areaConfig.width; j ++) {
                                    int blockId = getBlockId(i, j);
                                    freeBlockSet.remove(blockId);
                                    possiblePosition.remove((Object)blockId);

                                }
                            }
                            byte areaIndex = (byte)ThreadLocalRandom.current().nextInt(areaConfig.count);

                            int positionX = x * SCALE + areaConfig.height * SCALE  / 2;
                            int positionY = y * SCALE + areaConfig.width * SCALE  / 2;
                            byte terrainType = (byte)(index + 1);
                            areaList.add(new Area(positionX, positionY, rotation, terrainType, areaIndex));
                            generateCount --;
                        }
                    }
                    possiblePosition.remove(positionId);
                }
            }
        }
    }

    public boolean isValid(int x, int y, int width, int height) {
        for (int i = x; i < x + width; i ++) {
            for (int j = y; j < y + height; j ++) {
                if (i > blockBound || j > blockBound) return false;
                if (!freeBlockSet.contains(getBlockId(i, j))) return false;
            }
        }
        return true;
    }

    public int getCount(int areaWidth, int areaHeight) {
        return blockBound * blockBound / areaConfigs.length / (areaWidth * areaHeight);
    }

    public byte[] getBytes() {
        ByteBuilder byteBuilder = new ByteBuilder();
        byteBuilder.append(blockBound);
        byteBuilder.append(areaList.size());
        for(Area area: areaList) {
            area.appendBytes(byteBuilder);
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

    public int getBlockId(int x, int y) {
        return (y - 1) * blockBound + x;
    }

    public int getX(int id) {
        return (id - 1) % blockBound + 1;
    }

    public int getY(int id) {
        return (id - 1) / blockBound + 1;
    }

    public void print() {
        try {
            System.out.println("print map ....");
            BufferedImage bi = new BufferedImage(12000, 12000, BufferedImage.TYPE_INT_RGB);

            Graphics2D g = bi.createGraphics();
            g.setColor(Color.white);
            g.fillRect(0, 0, 10000, 10000);

            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.setStroke(new BasicStroke(8));

            for (Area area: areaList) {
                AreaConfig areaConfig = areaConfigs[area.areaTypeId - 1];
                //int x = getX(area.blockId);
                //int y = getY(area.blockId);
                if (area.rotation % 2 == 0) {
                    g.setColor(Color.ORANGE);
                    fillCenteredRect(g, area.positionX, area.positionY,
                            areaConfig.width * SCALE - 10, areaConfig.height * 100 - 10);
                    //g.fillRect(x * 100 + 5, y * 100 + 5, areaConfig.width * 100 - 10, areaConfig.height * 100 - 10);
                    g.setColor(Color.BLACK);
                    drawCenteredRect(g, area.positionX, area.positionY,
                            areaConfig.width * SCALE - 10, areaConfig.height * 100 - 10);
                    //g.drawRect(x * 100 + 5, y * 100 + 5, areaConfig.width * 100 - 10, areaConfig.height * 100 - 10);
                } else {
                    g.setColor(Color.ORANGE);
                    fillCenteredRect(g, area.positionX, area.positionY,
                            areaConfig.height * SCALE - 10, areaConfig.width * 100 - 10);
                    //g.fillRect(x * 100 + 5, y * 100 + 5, areaConfig.height * 100 - 10, areaConfig.width * 100 - 10);
                    g.setColor(Color.BLACK);
                    drawCenteredRect(g, area.positionX, area.positionY,
                            areaConfig.height * SCALE - 10, areaConfig.width * 100 - 10);
                    //g.drawRect(x * 100 + 5, y * 100 + 5, areaConfig.height * 100 - 10, areaConfig.width * 100 - 10);
                }
            }

            g.dispose();
            ImageIO.write(bi, "png", new File("/Users/deacon/Desktop/planetMap.png"));
            System.out.println("print map done!");
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void fillCenteredRect(Graphics2D g, int x, int y, int width, int height) {
        int positionX = x - (width / 2);
        int positionY = y - (height / 2);
        g.fillRect(positionX, positionY, width, height);
    }

    public void drawCenteredRect(Graphics2D g, int x, int y, int width, int height) {
        int positionX = x - (width / 2);
        int positionY = y - (height / 2);
        g.drawRect(positionX, positionY, width, height);
    }

    public static void main(String... args) {
        PlanetMap planetMap = new PlanetMap();
        planetMap.generate();
        planetMap.getBytes();
        planetMap.print();
    }
}
