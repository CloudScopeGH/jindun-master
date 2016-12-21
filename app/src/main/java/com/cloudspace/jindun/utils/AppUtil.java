package com.cloudspace.jindun.utils;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.content.res.Resources;
import android.location.LocationManager;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import com.cloudspace.jindun.UCAPIApp;
import com.cloudspace.jindun.config.Configs;
import com.cloudspace.jindun.log.APPLog;

import org.json.JSONObject;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipException;

public class AppUtil {

    private static final String TAG = "AppUtil";

    public final static int DEFAULT_VERSION = 204;
    private static String versionName = "";
    private static int versionCode = DEFAULT_VERSION;
    private static String macAddress = "";
    private static String channelSource = "";
    private static String sig = null;
    private static String agent = null;
    private static final int ENDHDR = 22;
    private static final Map<String, String> commentsMap = new HashMap<String, String>();

    /**
     * 返回当前程序版本名
     */
    public static String getAppVersionName(Context context) {

        if (!TextUtils.isEmpty(versionName)) {
            return versionName;
        }
        try {
            // ---get the package info---
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
            versionName = pi.versionName;
            if (versionName == null || versionName.length() <= 0) {
                versionName = "" + pi.versionCode;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return versionName;
    }

    public static String getAppVersionName() {

        if (!"".equals(versionName)) {
            return versionName;
        }
        try {
            // ---get the package info---
            PackageManager pm = UCAPIApp.getInstance().getPackageManager();
            PackageInfo pi = pm.getPackageInfo(UCAPIApp.getInstance().getPackageName(), 0);
            versionName = pi.versionName;
            if (versionName == null || versionName.length() <= 0) {
                return "";
            }
        } catch (Exception e) {
            APPLog.e("VersionInfo", "Exception" + e.getMessage());
        }
        return versionName;
    }

    public static void disableConnectionReuseIfNecessary() {
        // HTTP connection reuse which was buggy pre-froyo
        if (Integer.parseInt(Build.VERSION.SDK) < Build.VERSION_CODES.FROYO) {
            System.setProperty("http.keepAlive", "false");
        }
    }

    public static boolean isGpsEnabled(Context context) throws SecurityException {
        try {
            LocationManager locationManager
                    = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            // 通过GPS卫星定位，定位级别可以精确到街（通过24颗卫星定位，在室外和空旷的地方定位准确、速度快）
            boolean gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            // 通过WLAN或移动网络(3G/2G)确定的位置（也称作AGPS，辅助GPS定位。主要用于在室内或遮盖物（建筑群或茂密的深林等）密集的地方定位）
            boolean network = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if (gps || network) {
                return true;
            }
        } catch (Exception e) {
        }
        return false;
    }

    public static int getVersionCode(Context context) {
        if (versionCode > DEFAULT_VERSION) {
            return versionCode;
        }
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo pinfo = pm.getPackageInfo(context.getPackageName(), PackageManager.GET_CONFIGURATIONS);
            versionCode = pinfo.versionCode;
            if (versionCode == 0) {
                return DEFAULT_VERSION;
            }
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionCode;
    }

    public static int getVersionCode() {
        if (versionCode > DEFAULT_VERSION) {
            return versionCode;
        }
        try {
            PackageManager pm = UCAPIApp.getInstance().getPackageManager();
            PackageInfo pinfo = pm.getPackageInfo(UCAPIApp.getInstance().getPackageName(), PackageManager.GET_CONFIGURATIONS);
            versionCode = pinfo.versionCode;
            if (versionCode == 0) {
                return DEFAULT_VERSION;
            }
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionCode;
    }

    public static int getScreenWidth(Context context) {

        WindowManager manager = (WindowManager) context

                .getSystemService(Context.WINDOW_SERVICE);

        Display display = manager.getDefaultDisplay();

        return display.getWidth();

    }


    private static boolean isMacAddressValid(String macAddress) {
        if (TextUtils.isEmpty(macAddress)) {
            return false;
        }
        if (macAddress.contains("00:00:00:00")) {
            return false;
        }
        Pattern p = Pattern.compile("^([0-9a-fA-F]{2})(([/\\s:-][0-9a-fA-F]{2}){5})$");
        Matcher m = p.matcher(macAddress);
        return m.matches();
    }


    /**
     * @return 返回apk文件中的comment的JSONObject, 非null
     */
    private static String getPackageComment(Context context) {
        Context ktvContext = context;
        String packageName = ktvContext.getPackageName();
        String retrieved_comment = commentsMap.get(packageName);
        if (retrieved_comment == null) {
            synchronized (commentsMap) {
                retrieved_comment = commentsMap.get(packageName);
                if (retrieved_comment == null) {
                    try {
                        PackageManager packageManager = ktvContext.getPackageManager();
                        PackageInfo info = packageManager.getPackageInfo(
                                ktvContext.getPackageName(),
                                PackageManager.GET_META_DATA);
                        String apkSourceFile = info.applicationInfo.sourceDir;
                        String comment = getComment(apkSourceFile);
                        if (!TextUtils.isEmpty(comment)) {
                            retrieved_comment = comment;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (retrieved_comment == null)
                        retrieved_comment = "";
                    commentsMap.put(packageName, retrieved_comment);
                }
            }
        }
        return retrieved_comment;
    }

    public static String getComment(String zipFile) {
        RandomAccessFile raf = null;
        try {
            raf = new RandomAccessFile(zipFile, "r");
            // Scan back, looking for the End Of Central Directory field. If the
            // zip file doesn't
            // have an overall comment (unrelated to any per-entry comments),
            // we'll hit the EOCD
            // on the first try.
            long scanOffset = raf.length() - ENDHDR;
            if (scanOffset < 0) {
                throw new ZipException("File too short to be a zip file: "
                        + raf.length());
            }
            long stopOffset = scanOffset - 65536;
            if (stopOffset < 0) {
                stopOffset = 0;
            }
            final int ENDHEADERMAGIC = 0x06054b50;
            while (true) {
                raf.seek(scanOffset);
                if (Integer.reverseBytes(raf.readInt()) == ENDHEADERMAGIC) {
                    break;
                }
                scanOffset--;
                if (scanOffset < stopOffset) {
                    throw new ZipException("EOCD not found; not a zip file?");
                }
            }

            // Read the End Of Central Directory. ENDHDR includes the signature
            // bytes,
            // which we've already read.
            byte[] eocd = new byte[ENDHDR - 4];
            raf.readFully(eocd);

            // Pull out the information we need.
            int commentLength = (eocd[16] & 0xff) | ((eocd[17] & 0xff) << 8);
            if (commentLength > 0) {
                byte[] commentBytes = new byte[commentLength];
                raf.readFully(commentBytes);
                return new String(commentBytes, "UTF-8");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (raf != null) {
                try {
                    raf.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }


    /**
     * 获取app渠道,获取失败默认返回 "eayslive"
     *
     * @return 例如:eayslive
     */
    public static String getChannelSource(Context context) {
        if (TextUtils.isEmpty(channelSource)) {
            String channelStr = null;
            try {
                channelStr = getPackageComment(context);
                channelStr = XorBase64.decode(channelStr, XorBase64.DEFAULT_KEY);
                JSONObject jsonObject = new JSONObject(channelStr);
                channelSource = jsonObject.getString("cs");
                if (TextUtils.isEmpty(channelSource)) {
                    channelSource = "xiaochang";
                } else {
                    channelSource = channelSource.trim();
                }
            } catch (Exception e) {
                channelSource = "xiaochang";
                Log.w(TAG, "getChannelSource Exception : " + (channelStr == null ? "null" : channelStr));
            }
        }
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "channel:" + channelSource);
        }
        return channelSource;
    }


    public static String checkOfficialSign(Context context) {
        if (null == sig) {
            byte[] code = null;
            Signature[] s = getSign(context);
            if (s != null && s[0] != null) {
                code = getMD5(s[0].toByteArray());
                StringBuilder sb = new StringBuilder();
                for (byte aCode : code) {
                    sb.append(Integer.toString((aCode & 0xff) + 0x100, 16).substring(1));
                }
                sb.append(DeviceUtil.getMacAddress(context));
                sb.append("apk");
                sig = getMD5Hex(sb.toString());
            }
        }
        return sig;
    }

    public static String checkOfficialSign() {
        if (null == sig) {
            byte[] code = null;
            Signature[] s = getSign();
            if (s != null && s[0] != null) {
                code = StringUtil.getMD5(s[0].toByteArray());
                StringBuilder sb = new StringBuilder();
                for (byte aCode : code) {
                    sb.append(Integer.toString((aCode & 0xff) + 0x100, 16).substring(1));
                }
                sb.append(getMacAddress());
                sb.append("apk");
                sig = StringUtil.getMD5Hex(sb.toString());
            }
        }
        return sig;
    }

    public static String getMacAddress() {
        if (StringUtil.isEmpty(macAddress)) {
            String mac = UCAPIApp.getInstance().pre.getString(Configs.MACADDRESS, null);
            if (mac != null && mac.length() > 5) {
                macAddress = mac;
                return macAddress;
            }
            try {
                if (Build.VERSION.SDK_INT >= 23) {
                    macAddress = Console.execute("getprop ro.boot.wifimacaddr", 10000);
                } else {
                    WifiManager wifi = (WifiManager) UCAPIApp.getInstance().getSystemService(Context.WIFI_SERVICE);
                    WifiInfo info = wifi.getConnectionInfo();
                    macAddress = info.getMacAddress();
                }
                if (StringUtil.isEmpty(macAddress)) {
                    TelephonyManager tm = (TelephonyManager) UCAPIApp.getInstance().getSystemService(Context.TELEPHONY_SERVICE);
                    macAddress = tm.getDeviceId();
                }
                if (StringUtil.isEmpty(macAddress)) {
                    macAddress = UUID.randomUUID().toString();
                }
            } catch (Exception e) {
                macAddress = UUID.randomUUID().toString();
            }
            UCAPIApp.getInstance().pre.edit().putString(Configs.MACADDRESS, macAddress).commit();
        }
        return macAddress;
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


    //获取程序本身签名
    private static Signature[] getSign(Context context) {
        PackageManager pm = context.getPackageManager();
        try {
            if (pm != null) {
                List<PackageInfo> apps = pm.getInstalledPackages(PackageManager.GET_SIGNATURES);
                if (apps != null) {
                    for (PackageInfo info : apps) {
                        String packageName = info.packageName; //按包名 取签名
                        if (packageName.equals(context.getPackageName())) {
                            return info.signatures;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //获取程序本身签名
    private static Signature[] getSign() {
        PackageManager pm = UCAPIApp.getInstance().getPackageManager();
        if (pm != null) {
            List<PackageInfo> apps = pm.getInstalledPackages(PackageManager.GET_SIGNATURES);
            if (apps != null) {
                for (PackageInfo info : apps) {
                    String packageName = info.packageName; //按包名 取签名
                    if (packageName.equals(UCAPIApp.getInstance().getPackageName())) {
                        return info.signatures;
                    }
                }
            }
        }
        return null;
    }

    public static void initConstParams(Context context) {
        checkOfficialSign(context);
        getAppVersionName(context);
        getChannelSource(context);
    }

    /**
     * 程序是否在前台运行
     *
     * @return
     */
    public static boolean isAppOnForeground(Context context) {
        try {
            ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            String packageName = context.getPackageName();
            List<RunningTaskInfo> tasks = activityManager.getRunningTasks(1);
            if (!tasks.isEmpty()) {
                ComponentName topActivity = tasks.get(0).topActivity;
                if (topActivity.getPackageName().equals(packageName)) {
                    return true;
                }
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean isAppOnForeground() {
        ActivityManager activityManager = (ActivityManager) UCAPIApp.getInstance().getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        String packageName = UCAPIApp.getInstance().getApplicationContext().getPackageName();
        List<RunningTaskInfo> tasks = activityManager.getRunningTasks(1);
        if (!tasks.isEmpty()) {
            ComponentName topActivity = tasks.get(0).topActivity;
            if (topActivity.getPackageName().equals(packageName)) {
                return true;
            }
        }
        return false;
    }


    public static boolean isPackageInstalled(Context context, String packagename) {
        if (TextUtils.isEmpty(packagename)) {
            return false;
        }
        PackageManager pm = context.getPackageManager();
        try {
            pm.getPackageInfo(packagename, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (NameNotFoundException e) {
            return false;
        }
    }

    public static String getResourceUri(Context context, int res) {
        Resources resources = context.getResources();
        Uri uri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://"
                + resources.getResourcePackageName(res) + "/"
                + resources.getResourceTypeName(res) + "/"
                + res);
        return uri.toString();
    }

    // 是否禁止部分行为的模拟器
    public static boolean isSimulator() {
        return (null != Build.BRAND && Build.BRAND.toLowerCase().contains("bluestacks"));
    }

    // 是否不支持部分功能，硬件减速等的模拟器
    public static boolean isUnSupportSimulator() {
        return (null != Build.BRAND && Build.BRAND.toLowerCase().contains("bluestacks")) ||
                (null != Build.DEVICE && Build.DEVICE.equalsIgnoreCase("nox")) ||
                (null != Build.DEVICE && Build.DEVICE.equalsIgnoreCase("vbox86p"));
    }

    public static int getDisplayHeight(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        @SuppressWarnings("deprecation")
        int displayHeight = wm.getDefaultDisplay().getHeight();
        return displayHeight;
    }

    public static int getStatusBarHeight(Context context) {
        Class<?> c = null;
        Object obj = null;
        Field field = null;
        int x = 0, statusBarHeight = 0;
        try {
            c = Class.forName("com.android.internal.R$dimen");
            obj = c.newInstance();
            field = c.getField("status_bar_height");
            x = Integer.parseInt(field.get(obj).toString());
            statusBarHeight = context.getResources().getDimensionPixelSize(x);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return statusBarHeight;
    }


    /**
     * 通过package name检查APP是否已经安装
     */
    public static boolean isInstalled(Context context, String packageName) {
        PackageManager pm = context.getPackageManager();
        List<PackageInfo> packs = pm.getInstalledPackages(0);
        for (PackageInfo pi : packs) {
            if (pi.applicationInfo.packageName.equals(packageName)) {
                return true;
            }
        }
        return false;
    }


    public static boolean hasGingerbread() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD;
    }


    /**
     * checkPermissions
     *
     * @param context
     * @param permission
     * @return true or  false
     */
    public static boolean checkPermissions(Context context, String permission) {
        PackageManager localPackageManager = context.getPackageManager();
        return localPackageManager.checkPermission(permission, context
                .getPackageName()) == PackageManager.PERMISSION_GRANTED;
    }

    public static String getDeviceID(Context context) {
        try {
            if (Device.isPreM()) {
                TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                String deviceId = tm.getDeviceId();
                String backId = "";
                if (deviceId != null) {
                    backId = new String(deviceId);
                    backId = backId.replace("0", "");
                }

                if (((TextUtils.isEmpty(deviceId)) || TextUtils.isEmpty(backId)) && (Build.VERSION.SDK_INT >= 9)) {
                    try {
                        Class c = Class.forName("android.os.SystemProperties");
                        Method get = c.getMethod("get", new Class[]{String.class, String.class});
                        deviceId = (String) get.invoke(c, new Object[]{"ro.serialno", "unknown"});
                    } catch (Exception t) {
                        deviceId = null;
                    }
                }

                if (!TextUtils.isEmpty(deviceId)) {
                    return deviceId;
                }
            } else {
                return Console.execute("getprop ro.serialno", 1000);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private static long lastClickTime;

    public static boolean isFastDoubleClick() {
        long time = System.currentTimeMillis();
        long timeD = time - lastClickTime;
        if (0 < timeD && timeD < 800) {
            return true;
        }
        lastClickTime = time;
        return false;
    }

    public static boolean isFastDoubleClick(long intervalTime) {
        long time = System.currentTimeMillis();
        long timeD = time - lastClickTime;
        if (0 < timeD && timeD < intervalTime) {
            return true;
        }
        lastClickTime = time;
        return false;
    }

    /**
     * 获取已安装Apk文件的源Apk文件
     * 如：/data/app/com.sina.weibo-1.apk
     *
     * @param context
     * @param packageName
     * @return
     */
    public static String getSourceApkPath(Context context, String packageName) {
        if (TextUtils.isEmpty(packageName))
            return null;

        try {
            ApplicationInfo appInfo = context.getPackageManager()
                    .getApplicationInfo(packageName, 0);
            return appInfo.sourceDir;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }
}
