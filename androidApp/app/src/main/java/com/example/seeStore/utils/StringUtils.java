package com.example.seeStore.utils;

import java.text.DecimalFormat;

public class StringUtils {
    public static String vndFormatPrice(Long price) {
        DecimalFormat formatter = new DecimalFormat("###,###,###");
        return formatter.format(price) + " VNĐ";
    }
}
