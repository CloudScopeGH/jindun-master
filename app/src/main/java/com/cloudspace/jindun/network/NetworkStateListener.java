package com.cloudspace.jindun.network;

/**
 * ckb on 15/11/26.
 */
public interface NetworkStateListener {

    void onNetworkStateChanged(int currState, int lastState);

}
