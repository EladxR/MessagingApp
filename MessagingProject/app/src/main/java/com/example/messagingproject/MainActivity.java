package com.example.messagingproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private FirebaseUser user;
    private String username;
    private FirebaseAuth mAuth;
    public static List<Chat> chats;
    public static MyReceiver smsReceiver;
    public static Contact currContactOpen=null;
    public static long MaxGroupID;

    private ProgressDialog loadingBar;

    private boolean welcomeOnlyOnce=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

       // int permissionCheck= ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS);
       // if(permissionCheck!= PackageManager.PERMISSION_GRANTED){
      //      ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.RECEIVE_SMS},3); // ask permission for send SMS
      //  }
      //  Log.d("Debug","permission: "+permissionCheck);

        loadingBar=new ProgressDialog(this);
        loadingBar.setTitle("Logging in..");
        loadingBar.setCanceledOnTouchOutside(true);
        loadingBar.show();

        //init empty chats
        chats=new ArrayList<>();

        //set listView for the chats
       // UpdateChats();

        // receiver init
        smsReceiver = new MyReceiver();
        smsReceiver.setActivityHandler(this);
        IntentFilter portIntentFilter = new IntentFilter("android.intent.action.DATA_SMS_RECEIVED");
        portIntentFilter.addDataAuthority("*", "9512");
        portIntentFilter.addDataScheme("sms");
        registerReceiver(smsReceiver, portIntentFilter);

    }

    @Override
    protected void onStart() {
        super.onStart();

        mAuth=FirebaseAuth.getInstance();

        user=mAuth.getCurrentUser();


        if(user==null){
            ToLoginActivity();
            loadingBar.dismiss();
            finish();
        }else{
            //first get data
            FirebaseDatabase.getInstance().getReference().child("Users").child(user.getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    username= String.valueOf(dataSnapshot.child("username").getValue());
                    if(dataSnapshot.child("username").exists() && !TextUtils.isEmpty(username)) {
                        if(!welcomeOnlyOnce)
                        //welcome toast
                        Toast.makeText(MainActivity.this, "Welcome " + username, Toast.LENGTH_LONG).show();
                        welcomeOnlyOnce=true;

                    }else{ // no user name init-> to welcome activity
                        SendToWelcomeActivity();
                        loadingBar.dismiss();
                        finish(); // so user will not be able to go back here
                    }

                    // finished loading
                    loadingBar.dismiss();

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            // get data to chats list
            UpdateChats();
        }
    }

    private void SendToWelcomeActivity() {
        Intent intent=new Intent(this,WelcomeActivity.class);
        startActivity(intent);
    }

    private void ToLoginActivity() {
        Intent intent=new Intent(this,LoginActivity.class);
        startActivity(intent);
        finish();
    }

   /* public void onReceiveMessage(String message, String phoneNum){
        Log.d("DebugReceive",message);
        Contact receivedContact = SearchByPhone(chats,phoneNum);

        if(receivedContact!=null) {
            receivedContact.chatHistory.add("1" + message); // first letter 1-> received message
            try {
                MessagingActivity.RefreshListViewMessaging(receivedContact);
            }catch (NullPointerException e){
                Log.d("DebugReceive","no contact open");
            }
        }
    }


    public static Contact SearchByPhone(ArrayList<Chat> chats, String phoneNum) {
        for (Chat c:chats) {
            if(PhoneNumberUtils.compare(c.getPhoneNumber(),phoneNum)) { // equal phone numbers by android package
                return c;
            }
        }
        return null;
    }*/

    public void toChooseChat(View v){
        Intent intent=new Intent(this,ChooseChatActivity.class);
        startActivity(intent);
    }

    private void UpdateChats(){
        // if there are bo previous chats
        ListView listViewChats=findViewById(R.id.listViewChats);
      /*  if(chats.size()==0){
            listViewChats.setEnabled(false);
            listViewChats.setVisibility(View.INVISIBLE);
            TextView textNoChats=findViewById(R.id.TextNoChats);
            textNoChats.setVisibility(View.VISIBLE);
            textNoChats.setEnabled(true);
        }else{*/
            listViewChats.setEnabled(true);
            listViewChats.setVisibility(View.VISIBLE);
            TextView textNoChats=findViewById(R.id.TextNoChats);
            textNoChats.setVisibility(View.INVISIBLE);
            textNoChats.setEnabled(false);

            final ChatAdapter adapter=new ChatAdapter(chats,this);

            final DatabaseReference chatsRoot= FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Chats");
            chatsRoot.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    //get all chats names

                    List<Chat> chats=new ArrayList<>();
                    Iterator<DataSnapshot> iterator=dataSnapshot.getChildren().iterator();
                    while(iterator.hasNext()){
                        DataSnapshot chatData=iterator.next();
                        Chat chat=chatData.getValue(Chat.class);
                        //String name=(String) chatData.child("Name").getValue();
                       // boolean isGroup=(boolean) chatData.child("isGroup").getValue();
                       // String id=(String) chatData.getKey(); // key is the id
                        chats.add(chat);
                    }
                    MainActivity.chats=chats;
                    adapter.UpdateChats(chats);
                    adapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            listViewChats.setAdapter(adapter);
            listViewChats.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    //open Messaging Activity with current chat details
                    Intent toMessaging=new Intent(getBaseContext(),MessagingActivity.class);
                   // toMessaging.putExtra("ContactIndex",i);
                    String name= chats.get(i).getName();
                    toMessaging.putExtra("ChatName",name);
                    toMessaging.putExtra("ChatID",chats.get(i).getId());
                    toMessaging.putExtra("username",username);
                    startActivity(toMessaging);
                }
            });
       // }
    }

   /* @Override
    public void onActivityReenter(int resultCode, Intent data) {
        super.onActivityReenter(resultCode, data);
        UpdateChats();
    }

    @Override
    protected void onResume() {
        super.onResume();
        UpdateChats();
    }*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.main_menu,menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);

        if(item.getItemId()==R.id.settingsOption){

        }
        if(item.getItemId()==R.id.signOutOption){
            mAuth.signOut();
            ToLoginActivity();
        }
        if(item.getItemId()==R.id.editProfileOption){
            ToEditProfileActivity();
        }

        return true;
    }

    private void ToEditProfileActivity() {
        Intent intent=new Intent(this,EditProfileActivity.class);
        intent.putExtra("username",username);
        startActivity(intent);
    }
}

