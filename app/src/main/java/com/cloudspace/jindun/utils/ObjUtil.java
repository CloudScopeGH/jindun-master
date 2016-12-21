package com.cloudspace.jindun.utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public class ObjUtil {
    public static boolean isEmpty(Object obj) {
        return (null == obj);
    }

    public static boolean isNotEmpty(Object obj) {
        return !isEmpty(obj);
    }

    public static boolean isEmpty(List<?> list) {
        return (null == list || list.isEmpty());
    }

    public static boolean isNotEmpty(Collection<?> collection) {
        return !isEmpty(collection);
    }

    public static boolean isNotEmpty(List<?> list) {
        return !isEmpty(list);
    }

    public static boolean isEmpty(Set<?> set) {
        return (null == set || set.isEmpty());
    }

    public static boolean isEmpty(String str) {
        return str == null || str.length() == 0 || str.trim().length() == 0;
    }

    public static String getValueByKey(String objStr, String key) {
        if (!StringUtil.isEmpty(objStr) && objStr.startsWith("{") && objStr.endsWith("}")) {
            JSONObject obj;
            try {
                obj = new JSONObject(objStr);
                if (obj.has(key)) {
                    return obj.getString(key);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
