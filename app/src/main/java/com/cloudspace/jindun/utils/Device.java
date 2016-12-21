package com.cloudspace.jindun.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.cloudspace.jindun.module.Module;
import com.cloudspace.jindun.network.NetworkMonitor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Pattern;


public final class Device implements Module<Global> {

    public static String DEVICE_INFO;
    public static String MAC_ADDRESS;
    public static boolean BRAND_XIAOMI;

    public static String DEVICE_ID;
    public static String APN;

    public static String updateDeviceInfo() {
        WindowManager manager = (WindowManager) Global.getSystemService(Context.WINDOW_SERVICE);
        TelephonyManager mTelephonyMgr = (TelephonyManager) Global.getSystemService(Context.TELEPHONY_SERVICE);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(displayMetrics);

        StringBuilder builder = new StringBuilder();
        {
            String DEVICE_ID = null;

            try {
                DEVICE_ID = mTelephonyMgr.getDeviceId();
            } catch (Exception e) {
                DEVICE_ID = "N/A";
            }

            if (NetworkMonitor.isWifi())
                APN = "wifi";
            else if (NetworkMonitor.isMobile())
                APN = "mobile";
            else if (!NetworkMonitor.isNetworkAvailable())
                APN = "N/A";

            builder.append("imei=").append(DEVICE_ID).append('&');

            builder.append("model=").append(Build.MODEL).append('&');
            builder.append("os=").append(Build.VERSION.RELEASE).append('&');
            builder.append("apilevel=").append(Build.VERSION.SDK_INT).append('&');

            builder.append("network=").append(APN).append('&');
//            builder.append("sdcard=").append(Device.Storage.hasExternal() ? 1 : 0).append('&');
            builder.append("sddouble=").append("0").append('&');
            builder.append("display=").append(displayMetrics.widthPixels).append('*')
                    .append(displayMetrics.heightPixels).append('&');
            builder.append("manu=").append(Build.MANUFACTURER)/*.append('&')*/;
//            builder.append("wifi=").append(WifiDash.getWifiInfo()).append('&');
//            builder.append("storage=").append(getStorageInfo()).append('&');
//            builder.append("cell=").append(NetworkDash.getCellLevel()).append('&');

        }

        DEVICE_INFO = builder.toString();

        return DEVICE_INFO;
    }

    /**
     * 6.0以后mac地址获取要用getprop获取
     */
    public static void updateMacAddress() {
        try {
            if (Build.VERSION.SDK_INT >= 23) {
                MAC_ADDRESS = Console.execute("getprop ro.boot.wifimacaddr", 10000);
            } else {
                WifiManager wifi = (WifiManager) Global.getSystemService(Context.WIFI_SERVICE);
                WifiInfo info = wifi.getConnectionInfo();
                MAC_ADDRESS = info.getMacAddress();
            }

        } catch (Exception e) {
            // no-op
        }
    }

    public static int getNumCores() {
        try {
            //Get directory containing CPU info
            File dir = new File("/sys/devices/system/cpu/");
            //Filter to only list the devices we care about
            File[] files = dir.listFiles(new FileFilter() {

                @Override
                public boolean accept(File pathname) {
                    //Check if filename is "cpu", followed by a single digit number
                    if (Pattern.matches("cpu[0-9]", pathname.getName())) {
                        return true;
                    }
                    return false;
                }

            });
            //Return the number of cores (virtual CPU devices)
            return files.length;
        } catch (Exception e) {
            //Default to return 1 core
            return 1;
        }
    }

    /**
     * 获取CPU主频
     */
    public static long getCpuFrequence() {
        ProcessBuilder cmd;
        try {
            String[] args = {"/system/bin/cat", "/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq"};
            cmd = new ProcessBuilder(args);

            Process process = cmd.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = reader.readLine();
            return Long.valueOf(line);
        } catch (IOException ex) {
            // ex.printStackTrace();
        } catch (NumberFormatException e) {

        }
        return 0;
    }


    @Override
    public void initialize(Global box) {
        updateDeviceInfo();
        updateMacAddress();
        BRAND_XIAOMI = "xiaomi".equals(Build.BRAND.toLowerCase());
    }

    @Override
    public void destroy() {

    }

    public static boolean isPreM() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M;
    }

    public static boolean isPreL() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP;
    }

    public static int getScreenWidth() {
        return Global.getContext().getResources().getDisplayMetrics().widthPixels;
    }

    public static int getScreenHeight() {
        return Global.getContext().getResources().getDisplayMetrics().heightPixels;
    }

    public static boolean checkPermissions(String permission) {
        PackageManager localPackageManager = Global.getContext().getPackageManager();
        return localPackageManager.checkPermission(permission, Global.getContext().getPackageName()) == PackageManager.PERMISSION_GRANTED;
    }

}
