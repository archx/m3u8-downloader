package com.github.archx.m3u8d.uitl;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

/**
 * Utils
 *
 * @author archx
 * @since 2020/9/4 23:17
 */
public final class Utils {

    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static String getDatetime() {
        return dateFormat.format(Calendar.getInstance().getTime());
    }

    public static String getRandomDirectoryName() {
        Random random = new Random();
        int i = random.nextInt(999) + 100;
        return new SimpleDateFormat("yyyyMMdd").format(new Date()) + i;
    }
}
