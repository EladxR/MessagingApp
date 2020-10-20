package com.example.messagingproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private FirebaseUser user;
    public static String username; // changes on data change
    public static String profileImage=null;
    private FirebaseAuth mAuth;
    public static List<Chat> chats;
    private ListView listViewChats;
    public static long MaxGroupID;

    private ProgressDialog loadingBar;

    private boolean welcomeOnlyOnce=false;

    public static ArrayList<Contact> allContacts;
    public static ArrayList<Activity> chatActivities; // manage the opened messages activities
    private boolean isInitChatActivities =false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d("DebugMain","StartedMain");

        chatActivities=new ArrayList<>();


        loadingBar=new ProgressDialog(this);
        loadingBar.setTitle("Logging in..");
        loadingBar.setCanceledOnTouchOutside(true);
        loadingBar.show();

        //init empty chats
        chats=new ArrayList<>();

        listViewChats=findViewById(R.id.listViewChats);


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
                    if(dataSnapshot.child("profileImage").exists()){
                        profileImage=String.valueOf(dataSnapshot.child("profileImage").getValue());
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

    public void GetAllContacts() {
        ContentResolver contentResolver = getContentResolver();
        Cursor cursor_contacts = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);

        if (cursor_contacts.getCount() > 0) { // if there are contacts
            final ArrayList<Contact> contactList = new ArrayList<>();

            while (cursor_contacts.moveToNext()) {
                // init contact
                Contact contact = new Contact();
                String contactID = cursor_contacts.getString(cursor_contacts.getColumnIndex(ContactsContract.Contacts._ID));
                contact.username = cursor_contacts.getString(cursor_contacts.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                contact.profileImage = cursor_contacts.getString(cursor_contacts.getColumnIndex(ContactsContract.Contacts.PHOTO_URI));

                //get phone number
                int hasPhoneNumber = Integer.parseInt(cursor_contacts.getString(cursor_contacts.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)));
                if (hasPhoneNumber > 0) {

                    Cursor phoneCursor = contentResolver.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI
                            , null
                            , ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?"
                            , new String[]{contactID}
                            , null);

                    while (phoneCursor.moveToNext()) {
                        String phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        //< set >
                        contact.setPhoneNumber(phoneNumber); // actually just take the last phone number // to change.. ****************
                        //</ set >
                    }
                    phoneCursor.close();
                }
                contactList.add(contact);
            }

            //sort contacts by name
            Collections.sort(contactList);
            allContacts=contactList;

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


    public void toChooseChat(View v){
        Intent intent=new Intent(this,ChooseChatActivity.class);
        startActivity(intent);
    }

    private void UpdateChats(){
            SetToHasChats();

            final ChatAdapter adapter=new ChatAdapter(chats,this);

            final DatabaseReference chatsRoot= FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Chats");
            chatsRoot.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    //get all chats names

                    List<Chat> chats=new ArrayList<>();
                    Iterator<DataSnapshot> iterator=dataSnapshot.getChildren().iterator();
                    while(iterator.hasNext()) {
                        DataSnapshot chatData = iterator.next();
                        final Chat chat = chatData.getValue(Chat.class);
                        if (chat.getId() != null) { // happens when new chat added and data didn't fully changed (will call again when change is fully finished)
                            chats.add(chat);

                            if (chat.isGroup) {
                                DatabaseReference groupRoot = FirebaseDatabase.getInstance().getReference().child("Groups").child(chat.getId());
                                groupRoot.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        // set name and image of user's chat from group database
                                        chat.setName((String) snapshot.child("name").getValue());
                                        chat.setImage((String) snapshot.child("image").getValue());
                                        DatabaseReference userGroupRoot = chatsRoot.child(chat.getId());
                                        userGroupRoot.child("name").setValue(chat.getName());
                                        userGroupRoot.child("image").setValue(chat.getImage());
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }
                        }
                    }
                    MainActivity.chats=chats;
                    adapter.UpdateChats(chats);
                    adapter.notifyDataSetChanged();
                    if(chats.size()==0){
                        SetToEmptyChats();
                    }else{
                        SetToHasChats();
                    }
                    //only works first time
                    initChatActivities();
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
                    toMessaging.putExtra("chatIndex",i);
                    toMessaging.putExtra("chatImage",chats.get(i).getImage());
                    startActivity(toMessaging);
                }
            });

            listViewChats.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                    ShowDialogToDeleteChat(chats.get(i).getId(),i,chats.get(i).isOtherDelete);
                    return true;
                }
            });

    }

    private void initChatActivities() {
        if(!isInitChatActivities){
            isInitChatActivities =true;
            for (int i = 0; i < chats.size(); i++) {
                chatActivities.add(null);
            }

        }else{
            if(chatActivities.size()!=chats.size()){ // someone send msg first time and new chat adds
                int numberOfAddedChats=chats.size()-chatActivities.size();
                for(int i=0;i<numberOfAddedChats;i++){
                    chatActivities.add(null);
                }
            }
        }
    }

    private void ShowDialogToDeleteChat(final String chatId, final int index, final boolean isOtherDelete) {
        AlertDialog deleteDialog=new AlertDialog.Builder(this)
                .setTitle("Delete Chat")
                .setMessage("delete chat and all it's content?")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // delete the chat only from this user
                        onDeleteChat(chatId,index,isOtherDelete);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .create();
        deleteDialog.show();
    }

    private void onDeleteChat(String chatId, int index, boolean isOtherDelete) {
        //remove in firebase (it will update the list view)
        DatabaseReference chatRef=FirebaseDatabase.getInstance().getReference()
                .child("Users").child(user.getUid()).child("Chats").child(chatId);
        chatRef.removeValue();
        if(!isOtherDelete) {
            //set isOtherDelete only if the other user didn't delete you too
            DatabaseReference otherChatRed = FirebaseDatabase.getInstance().getReference()
                    .child("Users").child(chatId).child("Chats").child(user.getUid());
            otherChatRed.child("isOtherDelete").setValue(true);
        }

        //finish the opened messaging activity of this chat (if opened)
        if(chatActivities.get(index)!=null){
            try {
                chatActivities.get(index).finish();
            }catch (Exception e){
                Log.d("DebugTryCatch","failed to finish activity");
            }finally {
                // always execute
                chatActivities.set(index,null);

            }
        }

    }


    private void SetToHasChats() {
        listViewChats.setEnabled(true);
        listViewChats.setVisibility(View.VISIBLE);
        TextView textNoChats=findViewById(R.id.TextNoChats);
        textNoChats.setVisibility(View.INVISIBLE);
        textNoChats.setEnabled(false);
    }

    private void SetToEmptyChats() {
        listViewChats.setEnabled(false);
        listViewChats.setVisibility(View.INVISIBLE);
        TextView textNoChats=findViewById(R.id.TextNoChats);
        textNoChats.setVisibility(View.VISIBLE);
        textNoChats.setEnabled(true);
    }


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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode==0){
            if(grantResults.length>=1 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                // sendMessage();
            }else{
                Toast.makeText(this,"no permission",Toast.LENGTH_SHORT);
            }
        }else if(requestCode==2){
            if(grantResults.length>=1 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                GetAllContacts();
            }else{
                Toast.makeText(this,"no permission",Toast.LENGTH_SHORT);
            }
        }
    }
}

