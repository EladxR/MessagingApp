package com.example.messagingproject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.internal.Objects;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MessagingActivity extends AppCompatActivity {
    private Chat chat;
    private int contactIndex;
    private DatabaseReference chatRoot;
    private DatabaseReference groupChatRoot;
    private String groupPath="/Groups/";
    private String userChatPath="/Users/";
    private String otherUserChatPath="/Users/";
    private FirebaseUser user;
    private String username,userID;
    private boolean isGroup;
    private ListView listViewMessages;

    public static MessagesAdapter messagesAdapter;

    // @pre putExtra: ChatID ChatName
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messaging);

        Intent intent=getIntent();

        //get user
        user=FirebaseAuth.getInstance().getCurrentUser();
        username= MainActivity.username;
        userID=user.getUid();

        //get chat info
        String chatName=intent.getStringExtra("ChatName");
        final String chatID=intent.getStringExtra("ChatID");

        setTitle(chatName);

        //init paths
        groupPath="/Groups/"+chatID+"/chatHistory/";
        userChatPath="/Users/"+userID+"/Chats/"+chatID+"/chatHistory/";
        otherUserChatPath="/Users/"+chatID+"/Chats/"+userID+"/chatHistory/";

        //init chat class
        groupChatRoot=FirebaseDatabase.getInstance().getReference().child("Groups").child(chatID); // if not a group it will be null
        chatRoot= FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Chats").child(chatID);
        chatRoot.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //set isGroup
                isGroup= (boolean) dataSnapshot.child("isGroup").getValue();
                // init adapter after we know if it is a group
                messagesAdapter.initAdapter();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        //init listView
        listViewMessages=findViewById(R.id.listViewMessages);
        messagesAdapter=new MessagesAdapter();
        listViewMessages.setAdapter(messagesAdapter);
        // always scroll to bottom (can also in xml)
        listViewMessages.setStackFromBottom(true);
        listViewMessages.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);


    }

    private class MessagesAdapter extends BaseAdapter{
        private ArrayList<Message> chatHistory;

        //public MessagesAdapter(ArrayList<String> chatHistory){
       //     this.chatHistory=chatHistory;
     //   }
      //  public void refresh(ArrayList<String> chatHistory)
      //  {
      //      this.chatHistory=chatHistory;
     //       notifyDataSetChanged();
      //  }
        public MessagesAdapter(){
            chatHistory=new ArrayList<>();

        }
        private void initAdapter(){
            //connect chatHistory to database - on change it wil be updated
            if(isGroup) {
                groupChatRoot.child("chatHistory").addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        if (dataSnapshot.exists()) {
                            AddMessageToChatHistory(dataSnapshot);
                        }
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        if (dataSnapshot.exists()) {

                        }
                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }else{ // it's a private chat
                chatRoot.child("chatHistory").addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        if (dataSnapshot.exists()) {
                            AddMessageToChatHistory(dataSnapshot);
                        }
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        if (dataSnapshot.exists()) {

                        }
                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        }

        private void AddMessageToChatHistory(DataSnapshot dataSnapshot) {
            chatHistory.add(dataSnapshot.getValue(Message.class));
            // update the listView
            notifyDataSetChanged();
            //scroll to bottom
            listViewMessages.setSelection(listViewMessages.getCount()-1);


        }

        @Override
        public int getCount() {
            return chatHistory.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            view=getLayoutInflater().inflate(R.layout.message_layout,null);
            TextView msgText=(TextView)view.findViewById(R.id.messageText);
            TextView usernameText=(TextView)view.findViewById(R.id.usernameText);
            TextView timeText=(TextView)view.findViewById(R.id.timeSentText);
            Message msg=chatHistory.get(i);
            msgText.setText(msg.getText());
            usernameText.setText(msg.getSenderUsername());
            timeText.setText(msg.getTime());
            if(msg.getSenderUserID().equals(userID)) { // if i am the sender of this message
                // set cardView to right side
                CardView cardView = view.findViewById(R.id.cardViewMessage);
                ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) cardView.getLayoutParams();
                params.endToEnd = R.id.parent_layout_message;
                params.startToStart = ConstraintLayout.LayoutParams.UNSET;
                cardView.requestLayout();
                //change color
                cardView.setCardBackgroundColor(getResources().getColor(R.color.colorBackgroundMyMessage));

                //no username needed
                usernameText.setText("");
                usernameText.setTextSize(1);
            }
            if(!isGroup){
                //no username needed
                usernameText.setText("");
                usernameText.setTextSize(1);
            }

            return view;
        }
    }

    public void SendButton(View v){
       /* int permissionCheck= ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS);
        if(permissionCheck== PackageManager.PERMISSION_GRANTED){
            sendMessage();
            RefreshListViewMessaging(chat);
        }else{
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.SEND_SMS},0); // ask permission for send SMS
        }*/
       sendMessage();

    }

    private void sendMessage(){
        EditText input=findViewById(R.id.inputText);
        String message=input.getText().toString().trim();
        if(!TextUtils.isEmpty(message)) {
//          chat.chatHistory.add("0" + message);

            //save in database
            Message currMessage=new Message(message,username,userID);
            DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
            String messageKey=chatRoot.child("chatHistory").push().getKey();
            //DatabaseReference messageKeyRef = chatRoot.child("chatHistory").child(messageKey);
            Map<String, Object> childUpdates = new HashMap<>();
            // in groups chat history is saved only in Groups path. in private chats it saves in both users paths.
            if(isGroup) { // save in overall groups
                childUpdates.put(groupPath + messageKey + "/", currMessage);
            }else {
                // save in user's chats
                childUpdates.put(userChatPath + messageKey + "/", currMessage);
                //save in other's chats
                childUpdates.put(otherUserChatPath + messageKey + "/", currMessage);

            }
            rootRef.updateChildren(childUpdates);

            Toast.makeText(this, "sent message", Toast.LENGTH_SHORT).show();
            //reset text box
            input.setText("");
        }else{
            Toast.makeText(this, "can't send empty message", Toast.LENGTH_SHORT).show();
        }
       // String phoneNum=chat.getPhoneNumber();
        //Log.d("DebugSend",phoneNum+" msg: "+message);

      /*  short SMS_PORT = 9512; // my port for sms

        if(!message.equals("")&&!phoneNum.equals("")) {
            SmsManager manager = SmsManager.getDefault();
            manager.sendDataMessage(phoneNum, null,SMS_PORT, message.getBytes(), null, null);
            Toast.makeText(this, "sms sent", Toast.LENGTH_SHORT).show();
            chat.chatHistory.add("0"+message); // 0 for sent message, 1 for received message
        }else{
            Toast.makeText(this, "no message or phone number", Toast.LENGTH_SHORT).show();
        }

        input.setText("");*/
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode==0){
            if(grantResults.length>=1 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                sendMessage();
            }else{
                Toast.makeText(this,"no permission",Toast.LENGTH_SHORT);
            }
        }
    }

    public static void RefreshListViewMessaging(Chat c){
     //   messagesAdapter.refresh(c.chatHistory);
    }
}