package com.example.messagingproject;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

public class App extends Application {
    public static final String ChannelNewMessageID="chanel1";
    @Override
    public void onCreate() {
        super.onCreate();

        createNotificationChannel();
    }

    private void createNotificationChannel() {
        // notification channels only relevant to android oreo+
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            NotificationChannel channel1=new NotificationChannel(ChannelNewMessageID,
                    "channel new messages", NotificationManager.IMPORTANCE_HIGH);
            channel1.setDescription("channel for new messages sent");

            NotificationManager manager=getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel1);

        }

    }
}
