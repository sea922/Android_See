package com.example.seeStore.utils;

public class StringUtils {
    public static String long2money(long number) {
        String string = String.valueOf(number);
        StringBuffer buffer = new StringBuffer();

        for (int i = string.length() - 1; i >= 0; i--) {
            buffer.append(string.charAt(i));
            if ((string.length() - i) % 3 == 0 && i != 0) {
                buffer.append('.');
            }
        }

        buffer.reverse();
        buffer.append(" VND");

        return buffer.toString();
    }
}