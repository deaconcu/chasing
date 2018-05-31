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
}
