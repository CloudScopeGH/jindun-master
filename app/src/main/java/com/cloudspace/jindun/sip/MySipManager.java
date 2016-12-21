package com.cloudspace.jindun.sip;

import android.app.PendingIntent;
import android.content.Intent;
import android.net.sip.SipAudioCall;
import android.net.sip.SipException;
import android.net.sip.SipManager;
import android.net.sip.SipProfile;
import android.net.sip.SipRegistrationListener;
import android.text.TextUtils;
import android.util.Log;

import com.cloudspace.jindun.UCAPIApp;
import com.cloudspace.jindun.constants.Constants;
import com.cloudspace.jindun.log.APPLog;

/**
 * Created by zengxianhua on 16/12/17.
 */

public class MySipManager {
    private static MySipManager instance;
    private SipManager sipManager;
    private SipProfile sipProfile = null;

    public static synchronized  MySipManager getInstance(){
        if (instance == null){
            instance = new MySipManager();
            instance.sipManager = SipManager.newInstance(UCAPIApp.getInstance());
        }

        return instance;
    }

    SipAudioCall takeAudioCall(Intent intent, SipAudioCall.Listener listener){
        if (instance != null && sipManager != null){
            try {
                return sipManager.takeAudioCall(intent, listener);
            } catch (SipException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    public void register(String username, String domain, String password, int timeExpire){
        if (TextUtils.isEmpty(username)
                || TextUtils.isEmpty(domain)
                || TextUtils.isEmpty(password)){
            APPLog.d("Sip register parameter error");
            return;
        }

        try {
            SipProfile.Builder builder = new SipProfile.Builder(username, domain);
            builder.setPassword(password);
            sipProfile = builder.build();

            Intent intent = new Intent();
            intent.setAction(Constants.SIP_INCOMING_CALL_ACTION);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(UCAPIApp.getInstance(), 0,
                    intent, Intent.FILL_IN_DATA);
            sipManager.open(sipProfile, pendingIntent, null);
            sipManager.setRegistrationListener(sipProfile.getUriString(), new SipRegistrationListener(){

                @Override
                public void onRegistering(String localProfileUri) {

                }

                @Override
                public void onRegistrationDone(String localProfileUri, long expiryTime) {
                    APPLog.d("Sip register done");
                }

                @Override
                public void onRegistrationFailed(String localProfileUri, int errorCode, String errorMessage) {
                    APPLog.d(String.format("Sip register fail[code:%d, message:%s]", errorCode, errorMessage));
                }
            });
        } catch (Exception e) {
            APPLog.d("Sip register fail" + Log.getStackTraceString(e));
        }
    }

    public void close(){
        if (sipManager == null){
            return;
        }

        if (sipProfile != null){
            try {
                sipManager.close(sipProfile.getUriString());
            } catch (SipException e) {
                APPLog.d("Sip close fail" + Log.getStackTraceString(e));
            }
        }
    }


    public void makeCall(SipProfile peerProfile, SipAudioCall.Listener listener, int timeout){
        if (sipManager != null){
            try {
                sipManager.makeAudioCall(sipProfile, peerProfile, listener, timeout);
            } catch (SipException e) {
                APPLog.d("Sip make call exception:" + Log.getStackTraceString(e));
            }
        }
    }
}
