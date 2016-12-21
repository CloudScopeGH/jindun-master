package com.cloudspace.jindun.utils;

import android.content.Intent;
import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class ParseUtil {

    public static final String STRING_PARSE_ERROR = "string_parse_error";
    public static final String TAG_PARSE_UTIL = "parse";
    private static final double EARTH_RADIUS = 6378137;

    /**
     * 从Intent中获取String数据
     */
    public static String parseString(Intent intent, String key) {
        if (null != intent && null != key && key.length() > 0 && intent.hasExtra(key)) {
            String value = intent.getStringExtra(key);
            if (null != value) {
                return value;
            }
        }
//        KTVLog.d(TAG_PARSE_UTIL, "parse string error, key: " + key);
        return STRING_PARSE_ERROR;
    }

    /**
     * 从Intent中获取int数据, 异常返回0
     */
    public static int parseInt(Intent intent, String key) {
        if (null != intent && null != key && key.length() > 0 && intent.hasExtra(key)) {
            return intent.getIntExtra(key, 0);
        }
//        KTVLog.d(TAG_PARSE_UTIL, "parse int error, key: " + key);
        return 0;
    }


    private static double rad(double d) {
        return d * Math.PI / 180.0;
    }

    /**
     * 根据两点间经纬度坐标（double值），计算两点间距离，
     *
     * @param lat1
     * @param lng1
     * @param lat2
     * @param lng2
     * @return 距离：单位为米
     */
    public static int DistanceOfTwoPoints(double lat1, double lng1,
                                          double lat2, double lng2) {
        double radLat1 = rad(lat1);
        double radLat2 = rad(lat2);
        double a = radLat1 - radLat2;
        double b = rad(lng1) - rad(lng2);
        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2)
                + Math.cos(radLat1) * Math.cos(radLat2)
                * Math.pow(Math.sin(b / 2), 2)));
        s = s * EARTH_RADIUS;
        s = Math.round(s * 10000) / 10000 / 1000;
        return (int) Math.ceil(s);
    }

    /**
     * 从Intent中获取long数据, 异常返回0
     */
    public static long parseLong(Intent intent, String key) {
        if (null != intent && null != key && key.length() > 0 && intent.hasExtra(key)) {
            return intent.getLongExtra(key, 0);
        }
//        KTVLog.d(TAG_PARSE_UTIL, "parse long error, key: " + key);
        return 0;
    }

    /**
     * 从Intent中获取double数据, 异常返回0
     */
    public static double parseDouble(Intent intent, String key) {
        if (null != intent && null != key && key.length() > 0 && intent.hasExtra(key)) {
            return intent.getDoubleExtra(key, 0);
        }
//        KTVLog.d(TAG_PARSE_UTIL, "parse double error, key: " + key);
        return 0;
    }


    /**
     * 从json中解析String，异常返回""
     */
    public static String parseString(JSONObject object, String key) {
        if (object != null && object.has(key)) {
            try {
                return object.getString(key);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
//        KTVLog.d(TAG_PARSE_UTIL, "parse string error, key: " + key);
        return STRING_PARSE_ERROR;
    }

    /**
     * 从json中解析boolean，异常返回false.
     */
    public static boolean parseBoolean(JSONObject object, String key) {
        if (object != null && object.has(key)) {
            try {
                return object.getBoolean(key);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
//        KTVLog.d(TAG_PARSE_UTIL, "parse boolean error, key: " + key);
        return false;
    }

    /**
     * 从json中解析double，异常返回0
     */
    public static double parseDouble(JSONObject object, String key) {
        String str = parseString(object, key);
        return parseDouble(str);
    }

    /**
     * 从json中解析long，异常返回0
     */
    public static long parseLong(JSONObject object, String key) {
        String str = parseString(object, key);
        return parseLong(str);
    }

    /**
     * 从json中解析int，异常返回0
     */
    public static int parseInt(JSONObject object, String key) {
        String str = parseString(object, key);
        return parseInt(str);
    }

    /**
     * 把字符串parse成double，若传入字符串为空或转换时异常则返回0。
     */
    public static double parseDouble(String str) {
        if (str == null || str.length() == 0 || str.trim().length() == 0) {
            return 0;
        }
        try {
            return Double.parseDouble(str.trim());
        } catch (Exception e) {
            e.printStackTrace();
        }
//        KTVLog.d(TAG_PARSE_UTIL, "parse double error, str: " + str);
        return 0;
    }

    public static int parseInt(String str, int defaultInt) {
        int num = defaultInt;
        if (!StringUtil.isEmpty(str)) {
            try {
                num = Integer.parseInt(str.trim());
            } catch (NumberFormatException nfe) {
            } catch (Exception e) {
            }
        }
        return num;
    }

    public static float parseFloat(String str) {
        if (str == null || str.length() == 0 || str.trim().length() == 0) {
            return 0;
        }
        try {
            return Float.valueOf(str.trim());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }


    /**
     * 把字符串parse成int，若传入字符串为空或转换时异常则返回0。
     */
    public static int parseInt(String str) {
        return parseInt(str, 0);
    }

    public static int parseInt(long val) {
        return (Long.valueOf(val)).intValue();
    }

    public static int parseInt(float val) {
        return (Float.valueOf(val)).intValue();
    }

    /**
     * 使用BigDecimal把字符串转换成long，若传入字符串为空或转换时异常则返回0。
     */
    public static long parseLong(String str) {
        if (StringUtil.isEmpty(str)) {
            return 0;
        }
        try {
            return Long.parseLong(str.trim());
        } catch (NumberFormatException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
//        KTVLog.d(TAG_PARSE_UTIL, "parse long error, str: " + str);
        return 0;
    }

    /**
     * 使用BigDecimal把字符串转换成double，若传入字符串为空或转换时异常则返回0。
     */
    public static double doubleValue(String str) {
        if (StringUtil.isEmpty(str)) {
            return 0;
        }
        try {
            BigDecimal res = new BigDecimal(str);
            return res.doubleValue();
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
//        KTVLog.d(TAG_PARSE_UTIL, "parse double error, str: " + str);
        return 0;
    }


    public static long StringToLong(String string) {
        try {
            return Long.parseLong(string);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static int StringToInt(String string) {
        try {
            return Integer.parseInt(string);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }


    public static float StringToFloat(String string) {
        try {
            return Float.parseFloat(string);
        } catch (Exception e) {
            e.printStackTrace();
            return 0.0f;
        }
    }

    public static String toFormatTime(String time) {
        if (TextUtils.isEmpty(time)) {
            return "";
        }

        try {
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss'.0'");
            DateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            return sdf.format(df.parse(time));
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return time;//不能解析时直接返回原值
    }

}
