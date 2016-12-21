package com.cloudspace.jindun.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.WindowManager;

import com.cloudspace.jindun.UCAPIApp;
import com.cloudspace.jindun.config.Configs;

import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class DeviceUtil {

    public static final String MAC_ADDRESS = "mac_address";
    public static final String DEVICE_ID = "device_id";

    private static String macAddress;
    private static String deviceid;
    private static Point point;

    public static Point getDisplaySizePixels(Context context) {
        if (point != null) {
            return point;
        }
        point = new Point();
        WindowManager wm = ((Activity) context).getWindowManager();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            wm.getDefaultDisplay().getSize(point);
        } else {
            int width = wm.getDefaultDisplay().getWidth();
            int height = wm.getDefaultDisplay().getHeight();
            point.set(width, height);
        }
        return point;
    }

    public static String getDeviceId(Context context) {

        if (TextUtils.isEmpty(deviceid)) {
            deviceid = (String) SPUtil.get(context, DEVICE_ID, "");
            if (!TextUtils.isEmpty(deviceid)) {
                return deviceid;
            }

            // 方法1
            try {
                TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                deviceid = tm.getDeviceId();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (!TextUtils.isEmpty(deviceid)) {
                SPUtil.put(context, DEVICE_ID, deviceid);
                return deviceid;
            }

            // 方法3
            deviceid = Settings.System.getString(context.getContentResolver(), Settings.System.ANDROID_ID);
            if (!TextUtils.isEmpty(deviceid)) {
                SPUtil.put(context, DEVICE_ID, deviceid);
                return deviceid;
            }

            // 方法2
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            deviceid = tm.getSimSerialNumber();
            if (!TextUtils.isEmpty(deviceid)) {
                SPUtil.put(context, DEVICE_ID, deviceid);
                return deviceid;
            }

            // md,自己搞一个
            deviceid = UUID.randomUUID().toString();
            SPUtil.put(context, DEVICE_ID, deviceid);
        }
        return deviceid;
    }

    public static String getMacAddress(Context context) {
        if (TextUtils.isEmpty(macAddress)) {
            macAddress = (String) SPUtil.get(context, MAC_ADDRESS, "");
            if (isMacAddressValid(macAddress)) {
                return macAddress;
            }

            WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            WifiInfo info = wifi.getConnectionInfo();
            macAddress = info.getMacAddress();

            if (!isMacAddressValid(macAddress)) {
                try {
                    Process pp = Runtime.getRuntime().exec("cat /sys/class/net/wlan0/address ");
                    InputStreamReader ir = new InputStreamReader(pp.getInputStream());
                    LineNumberReader input = new LineNumberReader(ir);
                    String str = "";
                    for (; null != str; ) {
                        str = input.readLine();
                        if (str != null) {
                            macAddress = str.trim();// 去空格
                            break;
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            SPUtil.put(context, MAC_ADDRESS, macAddress);

        }
        return macAddress;
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
}
