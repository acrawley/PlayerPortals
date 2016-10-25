package net.andrewcr.minecraft.plugin.PlayerPortals.util;

public class ArrayUtil {
    public static String[] removeFirst(String[] input, int removeCount) {
        if (input == null || input.length <= removeCount) {
            return new String[0];
        }

        String[] newArray = new String[input.length - removeCount];
        System.arraycopy(input, removeCount, newArray, 0, input.length - removeCount);

        return newArray;
    }
}
