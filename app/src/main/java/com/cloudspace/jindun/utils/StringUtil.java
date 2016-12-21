package com.cloudspace.jindun.utils;

import android.content.Context;
import android.graphics.Color;
import android.text.ClipboardManager;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.widget.EditText;
import android.widget.TextView;

import com.cloudspace.jindun.net.manager.ImageManager;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;


public class StringUtil {

    public static boolean containsEnglishCharacter(String content) {
        byte[] buf = content.getBytes();
        for (byte aBuf : buf) {
            if ((aBuf & 0x80) == 0) {
                return true;
            }
        }
        return false;
    }

    public static boolean containsChineseCharacter(String content) {
        for (int i = 0; i < content.length(); i++) {
            String temStr = content.substring(i, i + 1);
            if (Pattern.matches("[\u4E00-\u9FA5]", temStr)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isNull(EditText editText) {
        String text = editText.getText().toString().trim();
        if (text != null && text.length() > 0) {
            return false;
        }
        return true;
    }

    public static boolean matchPhone(String text) {
        if (Pattern.compile("(\\d{11})|(\\+\\d{3,})").matcher(text).matches()) {
            return true;
        }
        return false;
    }

    public static boolean matchAcount(String text) {
        if (Pattern.compile("^[a-zA-Z][a-zA-Z\\d_\\-]{5,21}").matcher(text)
                .matches()) {
            return true;
        }
        return false;
    }

    /**
     * 实现文本复制功能 add by wangqianzhou
     *
     * @param content
     */
    public static void copy(String content, Context context) {
        // 得到剪贴板管理器
        ClipboardManager cmb = (ClipboardManager) context
                .getSystemService(Context.CLIPBOARD_SERVICE);
        cmb.setText(content.trim());
    }

    /**
     * 实现粘贴功能 add by wangqianzhou
     *
     * @param context
     * @return
     */
    public static String paste(Context context) {
        // 得到剪贴板管理器
        ClipboardManager cmb = (ClipboardManager) context
                .getSystemService(Context.CLIPBOARD_SERVICE);
        return cmb.getText().toString().trim();
    }

    public static boolean isEmpty(String str) {
        return str == null || str.length() == 0 || str.trim().length() == 0;
    }

    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    public static SpannableStringBuilder reviseTextViewColor(
            String text, String color, int start, int end) {
        SpannableStringBuilder builder = new SpannableStringBuilder(text);
        ForegroundColorSpan redSpan = new ForegroundColorSpan(Color.parseColor(color));
        builder.setSpan(redSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return builder;
    }

    // Format one section mask
    public static SpannableStringBuilder formatDigitColorByMask(
            String content, String mask, String color, int startPos, int endPos) {
        long digit = ParseUtil.parseLong(content);
        long maskDigit = ParseUtil.parseLong(mask);
        SpannableStringBuilder result = new SpannableStringBuilder(content);
        if (digit != 0 && maskDigit != 0 && content.length() == mask.length()) {
            int start = 0;
            int end = 0;
            int length = mask.length();
            ForegroundColorSpan colorSpan = new ForegroundColorSpan(Color.parseColor(color));
            for (int i = 0; i < length; ) {
                if (mask.charAt(i) == '0') {
                    i++;
                    continue;
                }
                start = i;
                for (int j = i; j < length; j++) {
                    if (mask.charAt(j) == '0') {
                        end = j - 1;
                        result.setSpan(colorSpan, start, end + 1, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                        i = j + 1;
                        break;
                    } else if (j == length - 1) {
                        end = j;
                        result.setSpan(colorSpan, start, end + 1, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                        i = j + 1;
                        break;
                    }
                }
            }
        }
        return result;
    }


    /**
     * 将array中的内容以delimiter为间隔拼接字符串
     *
     * @param array
     * @param delimiter
     * @return
     */
    public static String join(Object[] array, String delimiter) {
        if (array == null) {
            throw new IllegalArgumentException();
        }

        if (array.length == 0) {
            return "";
        }

        StringBuilder builder = new StringBuilder();
        for (Object item : array) {
            builder.append(item.toString() + delimiter);
        }
        builder.delete(builder.length() - delimiter.length(), builder.length());
        return builder.toString();
    }

    /**
     * 将list中的内容以delimiter为间隔拼接字符串
     *
     * @param list
     * @param delimiter
     * @return
     */
    @SuppressWarnings("unchecked")
    public static String join(List list, String delimiter) {
        if (list == null) {
            throw new IllegalArgumentException();
        }

        return join(list.toArray(), delimiter);
    }

    /**
     * 将每三个数字加上逗号处理（通常使用金额方面的编辑）
     *
     * @param str 无逗号的数字
     * @return 加上逗号的数字
     */
    public static String addComma(String str) {

        // 将传进数字反转
        String reverseStr = new StringBuilder(str).reverse().toString();

        String strTemp = "";
        for (int i = 0; i < reverseStr.length(); i++) {
            if (i * 3 + 3 > reverseStr.length()) {
                strTemp += reverseStr.substring(i * 3, reverseStr.length());
                break;
            }
            strTemp += reverseStr.substring(i * 3, i * 3 + 3) + ",";
        }
        // 将 【789,456,】 中最后一个【,】去除
        if (strTemp.endsWith(",")) {
            strTemp = strTemp.substring(0, strTemp.length() - 1);
        }

        // 将数字重新反转
        return new StringBuilder(strTemp).reverse().toString();
    }

    public static String[] getHours() {
        List<String> list = new ArrayList<String>();
        for (int i = 0; i < 24; i++) {
            list.add(i + ":00");
        }
        String[] hourStr = new String[list.size()];
        list.toArray(hourStr);

        return hourStr;
    }

    public static String[] getMinutes() {
        List<String> list = new ArrayList<String>();
        for (int i = 0; i < 60; i++) {
            list.add(i + "");
        }
        String[] minutes = new String[list.size()];
        list.toArray(minutes);

        return minutes;
    }

    public static String subStringByCount(String str, int subCount) {
        if (TextUtils.isEmpty(str) && subCount > 0) {
            return str;
        }
        ArrayList<String> list = new ArrayList<String>();
        int length = 0;
        int start = 0;
        final int size = str.length();
        for (int i = 0, step = 1; i < size; i += step) {
            final int codePoint = str.codePointAt(i);
            step = (codePoint >= 0x10000 ? 2 : 1);
            length += step;
            list.add(str.substring(start, length));
            start = length;
        }

        StringBuilder sb = new StringBuilder();
        int count = list.size() > subCount ? subCount : list.size();
        for (int j = 0; j < count; j++) {
            sb.append(list.get(j));
        }
        if (list.size() > subCount) {
            sb.append("...");
        }
        return sb.toString();
    }

    // 计算出该TextView中文字的长度(像素)
    public static float getTextViewLength(TextView textView, String text) {
        TextPaint paint = textView.getPaint();
        // 得到使用该paint写上text的时候,像素为多少
        float textLength = paint.measureText(text);
        return textLength;
    }

    public static String truncate(String s, int n, String encodeName) {
        if (isEmpty(s) || n <= 0) {
            return s;
        }
        try {
            if (n > s.getBytes(encodeName).length) {
                n = s.getBytes(encodeName).length;
            }
            byte[] resultBytes = new byte[n];
            int j = 0;
            for (int i = 0; i < s.length(); i++) {
                byte[] bytes = String.valueOf(s.charAt(i)).getBytes(encodeName);
                if (bytes.length <= n - j) {
                    for (int k = 0; k < bytes.length; k++) {
                        resultBytes[j] = bytes[k];
                        j++;
                    }
                } else {
                    break;
                }
            }
            return new String(resultBytes, 0, j, encodeName);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return s;
    }

    public static String truncate(String s, int n) {
        return truncate(s, n, Charset.defaultCharset().toString());
    }

    public static String getHost(String url) {
        if (StringUtil.isEmpty(url)) {
            return "";
        }
        String host = "";
        try {
            URI uri = new URI(url);
            host = uri.getHost();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return host;
    }

    public static List<String> stringsToList(String[] strings) {
        if (strings == null || strings.length == 0) {
            return null;
        }

        List<String> list = new ArrayList<String>();
        for (String str : strings) {
            list.add(str);
        }
        return list;
    }

    public static String StringFilter(String str) throws PatternSyntaxException {
        if (str == null)
            str = "";
        // 只允许字母和数字
        // String regEx = "[^a-zA-Z0-9]";
        // 清除掉所有特殊字符
        String regEx = "[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？\\s\\\\]";
        Pattern p = Pattern.compile(regEx, Pattern.CASE_INSENSITIVE
                | Pattern.UNICODE_CASE);
        Matcher m = p.matcher(str);
        return m.replaceAll("").trim();
    }

    public static String byte2hex(byte[] b) {// 二行制转字符串

        String hs = "";
        String stmp = "";
        for (byte aB : b) {
            stmp = (Integer.toHexString(aB & 0XFF));
            if (stmp.length() == 1)
                hs = hs + "0" + stmp;
            else
                hs = hs + stmp;
        }
        return hs.toUpperCase();
    }

    public static boolean isMobileNO(String mobiles) {
        if (StringUtil.isEmpty(mobiles)) {
            return false;
        }
        Pattern p = Pattern.compile("^((13[0-9])|(14[0-9])|(15[0-9])|(18[0-9]))\\d{8}$");
        Matcher m = p.matcher(mobiles);
//        System.out.println(m.matches()+"---");
        return m.matches();
    }

    public final static String getMD5Hex(String str) {
        byte data[];
        try {
            data = getMD5(str.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            data = getMD5(str.getBytes());
        }
        StringBuilder sb = new StringBuilder();
        for (byte aData : data) {
            sb.append(Integer.toString((aData & 0xff) + 0x100, 16).substring(
                    1));
        }
        return sb.toString();
    }

    public static byte[] getMD5(byte data[]) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(data);
            return digest.digest();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getSha1(String content) {
        if (content == null) {
            return null;
        }
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("sha-1");
            byte[] byteText = content.getBytes();
            md.update(byteText);
            byte[] sha1 = md.digest();
            return StringUtil.byte2hex(sha1);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String base64Encode(String data) {
        byte[] bytes = data.getBytes();
        try {
            data = android.util.Base64.encodeToString(bytes, android.util.Base64.URL_SAFE);
        } catch (NoClassDefFoundError e) {
            data = Base64.encode(bytes);
        }
        data = data.replaceAll("\\+", "-").replaceAll("/", "_")
                .replaceAll("=", "").replaceAll("\r", "").replaceAll("\n", "")
                .replaceAll(" ", "");
        return data;
    }

    public static String base64Decode(String data) {
        if (data == null) {
            return "";
        }
        byte[] datas = null;
        try {
            datas = android.util.Base64.decode(data, android.util.Base64.URL_SAFE);
        } catch (NoClassDefFoundError e) { // 低版本的android SDK没有Base64 class
            data = data.replaceAll("-", "+").replaceAll("_", "/");
            datas = Base64.decode(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (datas == null) {
            return "";
        }
        return new String(datas);
    }

    public static String resizeImageUrl(String url, int size) {
        if (TextUtils.isEmpty(url)) {
            return null;
        }
        int index = url.lastIndexOf('.');
        if (index < 0) {
            return null;
        }
        return String.format("%s_%d_%d%s", url.substring(0, index), size, size, url.substring(index));
    }

    public static String resizeImageUrl(String url, ImageManager.ImageType type) {
        if (TextUtils.isEmpty(url)) {
            return null;
        }
        if (ImageManager.ImageType.ORIGINAL.equals(type) || !url.startsWith("http://") || url.endsWith(type.getHolder())) {
            return url;
        }
        int index = url.lastIndexOf('.');
        if (index < 0) {
            return null;
        }
        StringBuilder sb = new StringBuilder(url.substring(0, index)).append(type.getHolder());
        return sb.toString();
    }


    public static String getGoldCountText(double gold) {
        DecimalFormat format = new DecimalFormat("0.00");
        return format.format(gold);
    }
}
