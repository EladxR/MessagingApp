package com.example.messagingproject.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.messagingproject.App;
import com.example.messagingproject.MainActivity;
import com.example.messagingproject.Message;
import com.example.messagingproject.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Iterator;

public class NotificationService extends FirebaseMessagingService {
    FirebaseAuth mAuth;
    FirebaseUser user;
    MainActivity mainActivity;

    public NotificationService(MainActivity mainActivity){
        this.mainActivity=mainActivity;
    }
    @Override
    public void onCreate() {
        super.onCreate();

    }
    public void CreateNotificationService(){
        mAuth=FirebaseAuth.getInstance();
        user=mAuth.getCurrentUser();
        if(user!=null){ // logged in
            DatabaseReference userChatRef= FirebaseDatabase.getInstance().getReference().child("Users").child(user.getUid()).child("Chats");
            userChatRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Iterator<DataSnapshot> iteratorChats = snapshot.getChildren().iterator();
                    while (iteratorChats.hasNext()){
                        // add value event listener for every chat
                        DataSnapshot chatRef=iteratorChats.next();
                        chatRef.child("chatHistory").getRef().addChildEventListener(new ChildEventListener() {
                            @Override
                            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                                //new message added!
                                Message newMsg=snapshot.getValue(Message.class);
                                sendNewMessageNotification(newMsg);
                            }

                            @Override
                            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                            }

                            @Override
                            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                            }

                            @Override
                            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        }
    }

    private void sendNewMessageNotification(Message newMsg) {
        try {
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(mainActivity);
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, App.ChannelNewMessageID);

            notificationBuilder.setAutoCancel(true).setDefaults(Notification.DEFAULT_ALL)
                    .setWhen(System.currentTimeMillis())
                    .setSmallIcon(R.drawable.notification_icon)
                    .setContentTitle(newMsg.getSenderUsername())
                    .setContentText(newMsg.getText())
                    .setContentInfo("info");

            notificationManager.notify(1,notificationBuilder.build());

        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
