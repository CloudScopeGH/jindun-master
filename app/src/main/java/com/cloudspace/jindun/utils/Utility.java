package com.cloudspace.jindun.utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;

import com.cloudspace.jindun.UCAPIApp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class Utility {
    public enum ObservableFileState {
        COMPLETED
    }



    public static String loadTextFromFile(File file) {
        final StringBuilder sb = new StringBuilder();
        if (file != null && file.exists()) {
            BufferedReader fr = null;
            try {
                fr = new BufferedReader(new FileReader(file));
                String s;
                while ((s = fr.readLine()) != null) {
                    sb.append(s);
                }
            } catch (FileNotFoundException e1) {
                // 不会发生.
            } catch (IOException e) {
                return "";
            } finally {
                try {
                    if (fr != null)
                        fr.close();
                } catch (IOException e1) {
                }
            }
        }
        return sb.toString();
    }

    //不用管这个函数用来干嘛 By Gao.
    public static String[] getUrlInfo() {
        String str = "Y2xhc3Nlcy5qYXIhdGVzdC53YXYhY29tLmNoYW5nYmEudXRpbHMuR2FvRExvYWRlcg";//"Z2FvLmRleCF0ZXN0Lndhdg";
        return StringUtil.base64Decode(str).split("!");

    }

    public static InetAddress getHostIp(String host) {
        InetAddress hostIp = null;
        try {
            hostIp = InetAddress.getByName(host);
        } catch (UnknownHostException e) {
        }
        return hostIp;
    }

    public static InetAddress getMyIP() {
        InetAddress myIP = null;
        try {
            myIP = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
        }
        return myIP;
    }


    public static final String IMAGE_SUB_DIR = "image";



    /**
     * Path related
     */
    public static File getSDPath() {
        File sdDir = null;
        boolean sdCardExist = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED); // 判断sd卡是否存在
        if (sdCardExist) {
            try {
                sdDir = Environment.getExternalStorageDirectory();// 获取跟目录
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return sdDir;
    }

    public static File getInstallPackageDir() {
        File sdDir = getSDPath();
        if (sdDir != null && sdDir.isDirectory()) {
            File JinDun = new File(sdDir, ".JinDun/apk");
            if (!JinDun.exists())
                JinDun.mkdirs();
            return JinDun;
        }
        return null;
    }

    public static File getLiveDataDir() {
        File sdDir = getSDPath();
        if (sdDir != null && sdDir.isDirectory()) {
            File JinDun = new File(sdDir, ".JinDun/data");
            if (!JinDun.exists())
                JinDun.mkdirs();
            return JinDun;
        }
        return null;
    }

    public static File getMp3CacheFile(String url) {
        return new File(getMp3CacheDir(), getMD5Hex(url) + ".mp3");
    }

    public static File getMp3CacheDir() {

        File sdDir = getSDPath();
        if (sdDir != null && sdDir.isDirectory()) {
            File ktv = new File(sdDir, ".JinDun/cacheMp3");
            if (!ktv.exists())
                ktv.mkdirs();
            return ktv;
        }
        return UCAPIApp.getInstance().getDir(
                "cacheMp3",
                UCAPIApp.MODE_APPEND | UCAPIApp.MODE_WORLD_READABLE
                        | UCAPIApp.MODE_WORLD_WRITEABLE);
    }

    public static File getKTVFileDir() {
        File sdDir = getSDPath();
        if (sdDir != null && sdDir.isDirectory()) {
            File JinDun = new File(sdDir, ".JinDun");
            if (!JinDun.exists())
                JinDun.mkdirs();
            return JinDun;
        }
        return null;
    }

    public static File getTombsFileDir() {
        File sdDir = getSDPath();
        if (sdDir != null && sdDir.isDirectory()) {
            File JinDun = new File(sdDir, ".JinDun/tombs");
            if (!JinDun.exists())
                JinDun.mkdirs();
            return JinDun;
        }
        return null;
    }

    public static File getSongFileDir() {
        File sdDir = getSDPath();
        if (sdDir != null && sdDir.isDirectory()) {
            File JinDun = new File(sdDir, ".JinDun/song");
            if (!JinDun.exists())
                JinDun.mkdirs();
            return JinDun;
        }
        return null;
    }

    public static File getLiveConfigDir() {
        File sdDir = getSDPath();
        if (sdDir != null && sdDir.isDirectory()) {
            File JinDun = new File(sdDir, ".JinDun/config");
            if (!JinDun.exists())
                JinDun.mkdirs();
            return JinDun;
        }
        return null;
    }

    public static String getKTVLiveConfigFileDir() {
        File sdDir = getSDPath();
        if (sdDir != null && sdDir.isDirectory()) {
            File JinDun = new File(sdDir, ".JinDun/config");
            if (!JinDun.exists())
                JinDun.mkdirs();
            return JinDun.getAbsolutePath();
        }
        return null;
    }

    public static File getLogDir() {
        File sdDir = getSDPath();
        if (sdDir != null && sdDir.isDirectory()) {
            File JinDun = new File(sdDir, ".JinDun/log");
            if (!JinDun.exists())
                JinDun.mkdirs();
            return JinDun;
        }
        return null;
    }

    public static File getAQueryCacheDir() {
        File sdDir = getSDPath();
        if (sdDir != null && sdDir.isDirectory()) {
            File JinDun = new File(sdDir, ".JinDun/cache");
            if (!JinDun.exists())
                JinDun.mkdirs();
            return JinDun;
        }
        return null;
    }

    public static File getImageCacheDir() {
        File sdDir = getAQueryCacheDir();
        if (sdDir != null && sdDir.isDirectory()) {
            File JinDun = new File(sdDir, "image");
            if (!JinDun.exists())
                JinDun.mkdirs();
            return JinDun;
        }
        return null;
    }


    public static File getJindunFavoritesDir() {
        File sdDir = getSDPath();
        if (sdDir != null && sdDir.isDirectory()) {
            File ktv = new File(sdDir, "JinDunPhoto");
            if (!ktv.exists())
                ktv.mkdirs();
            return ktv;
        }
        return null;
    }


    public static File getSplashScreenPicDir() {
        File sdDir = getSDPath();
        if (sdDir != null && sdDir.isDirectory()) {
            File ktv = new File(sdDir, ".JinDun/pic");
            if (!ktv.exists())
                ktv.mkdirs();
            return ktv;
        }
        return UCAPIApp.getInstance().getFilesDir();
    }


    public static File getVoiceMsgFileDir() {
        File sdDir = getSDPath();
        if (sdDir != null && sdDir.isDirectory()) {
            File JinDun = new File(sdDir, ".JinDun/voicemsg");
            if (!JinDun.exists())
                JinDun.mkdirs();
            return JinDun;
        }
        return null;
    }

    public final static String getMD5Hex(String str) {
        if (TextUtils.isEmpty(str)) {
            return "";
        }

        byte data[];
        try {
            data = getMD5(str.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            data = getMD5(str.getBytes());
        }
        StringBuilder sb = new StringBuilder();
        for (byte aData : data) {
            sb.append(Integer.toString((aData & 0xff) + 0x100, 16).substring(1));
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

    public static ProgressDialog showProcess(Context context, String msg) {
        if (context == null)
            return null;
        ProgressDialog dlg = new ProgressDialog(context);
        try {
            if ((context instanceof Activity) && ((Activity) context).isFinishing()) {
                return dlg;
            }
            dlg.setIndeterminate(true);
            dlg.setCancelable(false);
            dlg.setMessage(msg);
            dlg.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dlg;
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
        } catch (Exception e) {// 低版本的android SDK没有Base64 class
            data = data.replaceAll("-", "+").replaceAll("_", "/");
            datas = Base64.decode(data);
            e.printStackTrace();
        }
        if (datas == null) {
            return "";
        }
        return new String(datas);
    }

    /**
     * 相片或是截图临时处理和存放的目录
     *
     * @return
     */
    public static File getPhotoTempDir() {
        File sdDir = getSDPath();
        if (sdDir != null && sdDir.isDirectory()) {
            File ktv = new File(sdDir, ".JinDun/tmp");
            if (!ktv.exists())
                ktv.mkdirs();
            return ktv;
        }
        return UCAPIApp.getInstance().getFilesDir();
    }

    // 设置随机比例，num ==5 ,代表20%
    public static boolean randomRate(int num) {
        return (((1 + (int) (Math.random() * num)) % num) == 0);
    }

    public static File getLatestApkFile(String filePath) {
        File sdDir = getSDPath();
        if (sdDir != null && sdDir.isDirectory()) {
            File ktv = new File(sdDir, ".JinDun/apk");
            if (ktv.exists()) {
                // 清除老的APK文件
                File[] files = ktv.listFiles();
                for (File f : files) {
                    if (!f.getName()
                            .toLowerCase().contains(filePath)) {
                        f.delete();
                    }
                }
            } else {
                ktv.mkdirs();
            }
            return new File(ktv, filePath);
        }
        return null;
    }


    public static File getUpdateApkDir() {
        File sdDir = getSDPath();

        if (sdDir != null && sdDir.isDirectory()) {
            File ktv = new File(sdDir, ".JinDun" + File.separator + "apk");
            if (!ktv.exists()) {
                ktv.mkdirs();
            }
            return ktv;
        }
        return null;
    }
}
