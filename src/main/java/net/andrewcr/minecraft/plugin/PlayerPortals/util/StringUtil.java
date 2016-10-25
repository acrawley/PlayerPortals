package net.andrewcr.minecraft.plugin.PlayerPortals.util;

public class StringUtil {
    public static boolean isNullOrEmpty(String str) {
        return (str == null || str.length() == 0);
    }

    public static boolean equals(String a, String b) {
        if (a == null && b == null) {
            return true;
        }

        if (a == null || b == null) {
            return false;
        }

        return a.equals(b);
    }

    public static boolean equalsIgnoreCase(String a, String b) {
        if (a == null && b == null) {
            return true;
        }

        if (a == null || b == null ) {
            return false;
        }

        return a.equalsIgnoreCase(b);
    }
}
