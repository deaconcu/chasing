package com.prosper.chasing.game.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by deacon on 2018/5/11.
 */
public class Util {

    public static <T> boolean arrayContains(final T[] array, final T v) {
        for (final T e : array)
            if (e == v || v != null && v.equals(e)) return true;
        return false;
    }

    public static boolean arrayContains(final byte[] array, final byte v) {
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
        else if (direction == Enums.HexagonDirection.DOWN_RIGHT) return 300;
        else if (direction == Enums.HexagonDirection.DOWN_LEFT) return 240;
        else if (direction == Enums.HexagonDirection.LEFT) return 180;
        else if (direction == Enums.HexagonDirection.UP_LEFT) return 120;
        else if (direction == Enums.HexagonDirection.UP_RIGHT) return 60;
        else return -1;
    }
}
