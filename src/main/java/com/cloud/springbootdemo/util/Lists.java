package com.cloud.springbootdemo.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @version v1.0
 * @ClassName Lists
 * @Author rayss
 * @Datetime 2024/7/27 11:12 PM
 */

public class Lists {

    public static <T> List<T> newArraryList(T... value) {
        return new ArrayList<>(Arrays.asList(value));
    }
}
