package com.dudkovlad.wolbeta;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.telephony.SmsMessage;

/**
 * Created by vlad on 06.12.2014.
 */
public class SMSMonitor  extends BroadcastReceiver {
    private static final String ACTION = "android.provider.Telephony.SMS_RECEIVED";
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null && intent.getAction() != null &&
                ACTION.compareToIgnoreCase(intent.getAction()) == 0) {

            Object[] pduArray = (Object[]) intent.getExtras().get("pdus");
            SmsMessage message = SmsMessage.createFromPdu((byte[]) pduArray[0]);

            String body = message.getMessageBody();
            //String sms_from = messages.getDisplayOriginatingAddress();
            SharedPreferences mSettings = context.getSharedPreferences(Network_setup.APP_PREF, context.MODE_MULTI_PROCESS);
            if (body.equals("comp_run " + mSettings.getString ("APP_PREF_MAC", ""))) {
                Intent i = new Intent();
                i.setClass(context, Network_setup.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(i);

                abortBroadcast();
            }
        }


    }
}


//+79268805161
//6C-F0-49-0F-22-E0