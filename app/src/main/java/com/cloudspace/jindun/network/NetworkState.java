package com.cloudspace.jindun.network;

import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.text.TextUtils;

import com.cloudspace.jindun.UCAPIApp;
import com.cloudspace.jindun.utils.IOUtils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;

public class NetworkState {
    /**
     * Called when the activity is first created.
     * 中国联通的2G业务WAP浏览器中使用的APN为“UNIWAP”，
     * 3G业务为“3GWAP”；中国联通的2G上公网使用的APN为“UNINET”，
     * 3G业务为“3GNET”。 中国移动上内网的APN为“CMWAP”，
     * 上网卡及上公网使用的APN为“CMNET”。
     * 中国电信上内网的APN为“CTWAP”，
     * 上网卡及上公网使用的APN为“CTNET”。
     */
    public static final String OTHER = "other"; // disable
    public static final String DISABLE = "disable"; // disable
    public static final String WIFI = "wifi"; // wifi
    // 电信
    public static final String CTWAP = "ctwap"; // 电信　wap 10.0.0.200
    public static final String CTNET = "ctnet";
    //移动
    public static final String CMWAP = "cmwap";  // 移动2g wap 10.0.0.172
    public static final String CMNET = "cmnet";
    //联通2G
    public static final String UNIWAP = "uniwap"; // 联通2g wap 10.0.0.172
    public static final String UNINET = "uninet";
    //联通 3G网络 
    public static final String WAP_3G = "3gwap";
    public static final String NET_3G = "3gnet";
    private static final Uri PREFERRED_APN_URI = Uri.parse("content://telephony/carriers/preferapn");
    public static final int TYPE_NET_DISABLED = -1;// 网络不可用  
    public static final int TYPE_NET_WIFI = 1;            // wifi
    public static final int TYPE_CM_NET = 2;            // 移动 net 2G
    public static final int TYPE_CM_WAP = 3;            // 移动 wap 2G
    public static final int TYPE_UNI_NET = 4;            // 联通 net 2G
    public static final int TYPE_UNI_WAP = 5;            // 联通 wap 2G
    public static final int TYPE_3G_NET = 6;            // 联通 net 3G
    public static final int TYPE_3G_WAP = 7;            // 联通 wap 3G
    public static final int TYPE_CT_NET = 8;            // 电信 net 2G
    public static final int TYPE_CT_WAP = 9;            // 电信 wap 2G

    public static final int TYPE_OTHER_NET = 10;        //其他

    private static Context mContext = UCAPIApp.getInstance();

    /**
     * 判断Network具体类型（联通移动wap，电信wap，其他net）
     */
    public static int fromInfo(final NetworkInfo networkInfo) {
        try {
            if (networkInfo == null || !networkInfo.isAvailable()) {
                // 注意一：
                // NetworkInfo 为空或者不可以用的时候正常情况应该是当前没有可用网络，  
                // 但是有些电信机器，仍可以正常联网，  
                // 所以当成net网络处理依然尝试连接网络。  
                // （然后在socket中捕捉异常，进行二次判断与用户提示）。  

                return TYPE_NET_DISABLED;  //简单处理
            } else {

                // NetworkInfo不为null开始判断是网络类型  

                int netType = networkInfo.getType();
                if (netType == ConnectivityManager.TYPE_WIFI) {
                    return TYPE_NET_WIFI;                                 // wifi net处理
                } else if (netType == ConnectivityManager.TYPE_MOBILE) {

                    // 注意二：
                    // 判断是移动联通wap:  
                    // 其实还有一种方法通过getString(c.getColumnIndex("proxy")获取代理ip  
                    // 来判断接入点，10.0.0.172就是移动联通wap，10.0.0.200就是电信wap，但在  
                    // 实际开发中并不是所有机器都能获取到接入点代理信息，例如魅族M9 （2.2）等...  
                    // 所以采用getExtraInfo获取接入点名字进行判断  

                    String netMode = networkInfo.getExtraInfo();
                    if (netMode != null) {
                        // 通过apn名称判断是否是联通和移动wap  
                        netMode = netMode.toLowerCase();
                        if (netMode.equals(CMWAP)) {
                            return TYPE_CM_WAP;
                        } else if (netMode.equals(CMNET)) {
                            return TYPE_CM_NET;
                        } else if (netMode.equals(UNIWAP)) {
                            return TYPE_UNI_WAP;
                        } else if (netMode.equals(UNINET)) {
                            return TYPE_UNI_NET;
                        } else if (netMode.equals(WAP_3G)) {
                            return TYPE_3G_WAP;
                        } else if (netMode.equals(NET_3G)) {
                            return TYPE_3G_NET;
                        }
                    }
                    // 注意三：  
                    // 判断是否电信wap:  
                    // 不要通过getExtraInfo获取接入点名称来判断类型，  
                    // 因为通过目前电信多种机型测试发现接入点名称大都为#777或者null，  
                    // 电信机器wap接入点中要比移动联通wap接入点多设置一个用户名和密码,  
                    // 所以可以通过这个进行判断！  

                    final Cursor c = mContext.getContentResolver().query(PREFERRED_APN_URI, null, null, null, null);
                    if (c != null && c.getCount() > 0) {
                        c.moveToFirst();
                        final String user = c.getString(c.getColumnIndex("user"));
                        if (!TextUtils.isEmpty(user)) {
                            if (user.startsWith(CTWAP) || user.startsWith(CTNET)) {
                                return TYPE_CT_NET;
                            }
                        }
                        c.close();
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return TYPE_OTHER_NET;
        }
        return TYPE_OTHER_NET;

    }

    //尝试一次socket连接，判断是不是真的没有网络
    public static boolean hasOtherNetwork() {
        Socket socket = new Socket();
        try {
            socket.setSoTimeout(1000);
            socket.setTcpNoDelay(true);
            socket.connect(new InetSocketAddress("ns1.dnspod.net", 6666), 1000);
            List<String> lines = IOUtils.readLines(socket.getInputStream());
            if (lines != null && !lines.isEmpty()) {
                return true;
            }
        } catch (SocketException e) {
        } catch (IOException e) {
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
            }
        }
        return false;
    }


    //没有网络
    public static boolean isDisabledMode(int networkType) {
        return networkType == TYPE_NET_DISABLED;
    }

    /**
     * 网络速度来分类
     */

    //wifi网络
    public static boolean isWiFiMode(int networkType) {
        return (networkType == TYPE_NET_WIFI);
    }

    //联通 3G 网络
    public static boolean is3GNetMode(int networkType) {
        return (networkType == TYPE_3G_NET) || (networkType == TYPE_3G_WAP);
    }

    //联通 2G 网络
    public static boolean is2GNetMode(int networkType) {
        return (networkType == TYPE_UNI_NET) || (networkType == TYPE_UNI_WAP);
    }

    //联通网络（2G + 3G）
    public static boolean isUnicomMode(int networkType) {
        return is3GNetMode(networkType) || is2GNetMode(networkType);
    }

    //移动网络
    public static boolean isChinaMobileMode(int networkType) {
        return (networkType == TYPE_CM_NET) || (networkType == TYPE_CM_WAP);
    }

    //电信网络 chinatelecom
    public static boolean isChinaTeleMode(int networkType) {
        return (networkType == TYPE_CT_NET) || (networkType == TYPE_CT_WAP);
    }

    //慢速网络
    public static boolean isLowSpeedMode(int networkType) {
        return isChinaMobileMode(networkType) ||
                is2GNetMode(networkType) ||
                isChinaTeleMode(networkType) ||
                (networkType == TYPE_OTHER_NET);
    }

    //快速网络
    public static boolean isHighSpeedMode(int networkType) {
        return is3GNetMode(networkType) ||
                isWiFiMode(networkType);
    }

    /**
     * 网络大类型来分类 GPRS/wifi
     */
    //GPRS移动/电信/联通网络(统称)
    public static boolean isMobileMode(int networkType) {
        return isUnicomMode(networkType) ||
                isChinaMobileMode(networkType) ||
                isChinaTeleMode(networkType) ||
                (networkType == TYPE_OTHER_NET);
    }

    /**
     * wap网络子类型
     */
    public static boolean isWapMode(int networkType) {
        return (networkType == TYPE_3G_WAP) ||
                (networkType == TYPE_CM_WAP) ||
                (networkType == TYPE_CT_WAP) ||
                (networkType == TYPE_UNI_WAP);
    }

    /**
     * net网络子类型
     */
    public static boolean isNetMode(int networkType) {
        return (networkType == TYPE_3G_NET) ||
                (networkType == TYPE_CM_NET) ||
                (networkType == TYPE_CT_NET) ||
                (networkType == TYPE_UNI_NET);
    }

    /**
     * 联通net网络子类型
     */
    public static boolean isUniComNetMode(int networkType) {
        return (networkType == TYPE_3G_NET) ||
                (networkType == TYPE_UNI_NET);
    }

    /**
     * 联通 wap网络子类型
     */
    public static boolean isUnicomWapMode(int networkType) {
        return (networkType == TYPE_3G_WAP) ||
                (networkType == TYPE_UNI_WAP);
    }


    public static String getNetMode(int networkType) {
        String mode = OTHER;
        switch (networkType) {
            case TYPE_NET_DISABLED:
                mode = DISABLE;
                break;
            case TYPE_NET_WIFI:
                mode = WIFI;
                break;
            case TYPE_CM_NET:
                mode = CMNET;
                break;
            case TYPE_CM_WAP:
                mode = CMWAP;
                break;
            case TYPE_UNI_NET:
                mode = UNINET;
                break;
            case TYPE_UNI_WAP:
                mode = UNIWAP;
                break;
            case TYPE_3G_NET:
                mode = NET_3G;
                break;
            case TYPE_3G_WAP:
                mode = WAP_3G;
                break;
            case TYPE_CT_NET:
                mode = CTNET;
                break;
            case TYPE_CT_WAP:
                mode = CTWAP;
                break;
            default:
                mode = OTHER;
                break;
        }
        return mode;
    }
}  