package com.cloudspace.jindun.sip;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.sip.SipAudioCall;
import android.net.sip.SipException;
import android.net.sip.SipProfile;
import android.util.Log;

import com.cloudspace.jindun.log.APPLog;

/**
 * Created by zengxianhua on 16/12/17.
 */

public class CallReceiver extends BroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent) {
        SipAudioCall call;
        final int CALL_ANSWER_EXPIRE_TIME = 30;

        SipAudioCall.Listener listener = new SipAudioCall.Listener(){
            @Override
            public void onRinging(SipAudioCall call, SipProfile caller) {
                if (call != null){
                    try {
                        call.answerCall(CALL_ANSWER_EXPIRE_TIME);
                    } catch (SipException e) {
                        APPLog.d("call receiver error:" + Log.getStackTraceString(e));
                    }
                }

            }
        };

        call = MySipManager.getInstance().takeAudioCall(intent, listener);
        if (call != null){
            try {
                call.answerCall(CALL_ANSWER_EXPIRE_TIME);
                call.startAudio();
                call.setSpeakerMode(true);

                if (call.isMuted()){
                    call.toggleMute();
                }
            } catch (SipException e) {
                APPLog.d("CallReceiver : " + Log.getStackTraceString(e));
                if (call != null){
                    call.close();
                }
            }
        }

    }
}
