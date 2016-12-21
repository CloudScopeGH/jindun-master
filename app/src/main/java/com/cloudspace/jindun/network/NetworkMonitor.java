package com.cloudspace.jindun.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.cloudspace.jindun.UCAPIApp;
import com.cloudspace.jindun.utils.TaskUtil;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;


public class NetworkMonitor {

    private static int currState = NetworkState.TYPE_NET_DISABLED;
    private static int lastState = NetworkState.TYPE_NET_DISABLED;

    private static final List<WeakReference<NetworkStateListener>> listeners = new ArrayList<>();

    public final static void addListener(NetworkStateListener listener) {
        synchronized (listeners) {
            listeners.add(new WeakReference<NetworkStateListener>(listener));
        }
    }

    public final static void removeListener(NetworkStateListener listener) {
        synchronized (listeners) {
            WeakReference<NetworkStateListener> reference = null;

            for (WeakReference<NetworkStateListener> weakReference : listeners) {
                NetworkStateListener aListener = weakReference.get();
                if (aListener.equals(listener)) {
                    reference = weakReference;
                    break;
                }
            }
            listeners.remove(reference);

        }
    }

    public final static boolean updateNetworkState() {
        synchronized (NetworkMonitor.class) {
            NetworkInfo networkInfo = null;
            try {
                ConnectivityManager connectivityManager =
                        (ConnectivityManager) UCAPIApp.getInstance().getSystemService(Context.CONNECTIVITY_SERVICE);
                networkInfo = connectivityManager.getActiveNetworkInfo();
            } catch (Exception e) {
                networkInfo = null;
            }

            boolean changed = setCurrState(NetworkState.fromInfo(networkInfo));

            if (changed) {
                notifyNetworkStateChange();
            }
            return changed;
        }
    }

    private final static boolean setCurrState(int newState) {
        synchronized (NetworkMonitor.class) {
            lastState = currState;
            currState = newState;
            return currState != lastState;
        }
    }

    private static void notifyNetworkStateChange() {
       TaskUtil.executeTask(new Runnable() {
            @Override
            public void run() {
                synchronized (listeners) {
                    for (WeakReference<NetworkStateListener> weakReference : listeners) {
                        NetworkStateListener listener = weakReference.get();
                        if (listener != null) {
                            listener.onNetworkStateChanged(getCurrState(), getLastState());
                        }
                    }
                }
            }
        });

    }

    public static int getCurrState() {
        return currState;
    }

    protected static int getLastState() {
        return lastState;
    }

    public static boolean isNetworkAvailable() {
        updateNetworkState();
        return !NetworkState.isDisabledMode(getCurrState());
    }

    public static boolean isWifi() {
        updateNetworkState();
        return NetworkState.isWiFiMode(getCurrState());
    }

    public static boolean isMobile() {
        updateNetworkState();
        return NetworkState.isMobileMode(getCurrState());
    }

}
