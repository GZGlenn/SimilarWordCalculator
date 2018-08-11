package com.pr.nlp.util;

public class StringUtils {

    public static final String EMPTY = "";

    public static int count(String s, char c) {
        int count = 0;
        for (char e : s.toCharArray())
            if (e == c)
                count++;
        return count;
    }

    public static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    public static boolean isNotBlank(String s) {
        return !isNotBlank(s);
    }

    public static boolean isEmpty(String s) {
        return s == null || s.isEmpty();
    }

    public static boolean isNotEmpty(String s) {
        return !isEmpty(s);
    }
}
