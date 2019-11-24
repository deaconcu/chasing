package com.prosper.chasing.game.util;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by deacon on 2018/5/11.
 */
public class Util {

    public static <T> boolean contains(final T[] array, final T v) {
        for (final T e : array)
            if (e == v || v != null && v.equals(e)) return true;
        return false;
    }

    public static boolean contains(final byte[] array, final byte v) {
        for (final byte e : array)
            if (e == v) return true;
        return false;
    }

    public static int[] intersect(int[] a, int[] b) {
        return Arrays.stream(a)
                .distinct()
                .filter(x -> Arrays.stream(b).anyMatch(y -> y == x))
                .toArray();
    }

    public static int getDegree(Enums.HexagonDirection direction) {
        if (direction == Enums.HexagonDirection.RIGHT) return 0;
        else if (direction == Enums.HexagonDirection.DOWN_RIGHT) return 300 * 1000;
        else if (direction == Enums.HexagonDirection.DOWN_LEFT) return 240 * 1000;
        else if (direction == Enums.HexagonDirection.LEFT) return 180 * 1000;
        else if (direction == Enums.HexagonDirection.UP_LEFT) return 120 * 1000;
        else if (direction == Enums.HexagonDirection.UP_RIGHT) return 60 * 1000;
        else return -1;
    }

    public static Enums.HexagonDirection getOppositeDirection(Enums.HexagonDirection direction) {
        if (direction == Enums.HexagonDirection.LEFT) return Enums.HexagonDirection.RIGHT;
        if (direction == Enums.HexagonDirection.RIGHT) return Enums.HexagonDirection.LEFT;
        if (direction == Enums.HexagonDirection.DOWN_LEFT) return Enums.HexagonDirection.UP_RIGHT;
        if (direction == Enums.HexagonDirection.DOWN_RIGHT) return Enums.HexagonDirection.UP_LEFT;
        if (direction == Enums.HexagonDirection.UP_RIGHT) return Enums.HexagonDirection.DOWN_LEFT;
        if (direction == Enums.HexagonDirection.UP_LEFT) return Enums.HexagonDirection.DOWN_RIGHT;
        return Enums.HexagonDirection.FREE;
    }

    public static double getRadians(int degree) {
        return degree % 360 * Math.PI / 180;
    }

    public static int distance(long x1, long y1, long x2, long y2) {
        return (int)Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
    }

    public static <K, V> V randomValue(Map<K, V> map) {
        if (map == null || map.size() == 0) return null;
        Object[] values = map.values().toArray();
        return (V)values[ThreadLocalRandom.current().nextInt(values.length)];
    }

    public static <K, V> K randomKey(Map<K, V> map) {
        if (map == null || map.size() == 0) return null;
        Object[] values = map.keySet().toArray();
        return (K)values[ThreadLocalRandom.current().nextInt(values.length)];
    }

    public static <T> T random(List<T> list) {
        if (list == null || list.size() == 0) return null;
        return list.get(ThreadLocalRandom.current().nextInt(list.size()));
    }

    public static <T> T random(T... list) {
        if (list == null || list.length == 0) return null;
        return list[ThreadLocalRandom.current().nextInt(list.length)];
    }

    public static <T> T random(T[] list, int length) {
        if (list == null || list.length == 0 || length == 0) return null;
        if (length > list.length) length = list.length;
        return list[ThreadLocalRandom.current().nextInt(length)];
    }



    public static void main(String... args) {
        double a = Math.atan2(-1, 1);
        int b = 1;
    }
}
