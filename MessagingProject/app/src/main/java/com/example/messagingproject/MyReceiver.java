package com.example.messagingproject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

public class MyReceiver extends BroadcastReceiver {
    private static final String SMS_Receive="android.provider.Telephony.SMS_RECEIVED";
    private static final String SMS_Received="android.intent.action.DATA_SMS_RECEIVED";

    private String msg="",phoneNum="";

    MainActivity mainAct = null;

    void setActivityHandler(MainActivity mainAct) {
        this.mainAct = mainAct;
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        Log.d("DebugReceiver","Intent Received: "+intent.getAction());

        if(intent.getAction().equals(SMS_Receive)||intent.getAction().equals(SMS_Received)){ // when receiving a SMS
            Bundle dataBundle= intent.getExtras();
            if(dataBundle!=null){
                String info = "SMS from ";
                String sender = "";

                Object[] pdus = (Object[]) dataBundle.get("pdus");
                final SmsMessage[] messages=new SmsMessage[pdus.length];

                byte[] data = null;

                for (int i = 0; i < messages.length; i++) {
                    messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                    sender += messages[i].getOriginatingAddress();
                    info += messages[i].getOriginatingAddress() + "\n";

                    data = messages[i].getUserData();

                    msg="";
                    for (int index = 0; index < data.length; index++) {
                        msg += Character.toString((char) data[index]);
                    }
                }

                Toast.makeText(context,"msg: "+msg+"  phone: "+info,Toast.LENGTH_LONG).show();
                try {
                    //mainAct.onReceiveMessage(msg,info);
                }catch (Exception e){
                    Toast.makeText(context,"received sms and not in message activity",Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }

            }
        }

    }

}
