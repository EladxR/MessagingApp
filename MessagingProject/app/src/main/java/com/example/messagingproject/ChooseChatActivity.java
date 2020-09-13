package com.example.messagingproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.View;

import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;


public class ChooseChatActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_chat);

        int permissionCheck= ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS);
        if(permissionCheck== PackageManager.PERMISSION_GRANTED){
            GetAllContacts();
        }else{
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_CONTACTS},2); // ask permission for send SMS
        }
    }

    private void GetAllContacts() {
        ContentResolver contentResolver=getContentResolver();
        Cursor cursor_contacts=contentResolver.query(ContactsContract.Contacts.CONTENT_URI,null,null,null,null);

        if(cursor_contacts.getCount()>0){ // if there are contacts
            final ArrayList<Contact> contactList=new ArrayList<>();

            while(cursor_contacts.moveToNext()) {
                // init contact
                Contact contact = new Contact();
                String contactID = cursor_contacts.getString(cursor_contacts.getColumnIndex(ContactsContract.Contacts._ID));
                contact.name = cursor_contacts.getString(cursor_contacts.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                contact.image = cursor_contacts.getString(cursor_contacts.getColumnIndex(ContactsContract.Contacts.PHOTO_URI));

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

            ListView listViewContacts=findViewById(R.id.listViewContacts);
            ChatAdapter adapter=new ChatAdapter(contactList,this);
           // listViewContacts.setAdapter(adapter);

            listViewContacts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    // add contact to database (maybe change later to add only if message sent)
                    if(!MainActivity.chats.contains(contactList.get(i))){ // add only if there is no previous chat
                        MainActivity.chats.add(contactList.get(i));
                        //save chats to database
                        FirebaseDatabase.getInstance().getReference().child("Users").child("Chats").setValue(MainActivity.chats);

                    }
                    //open Messaging Activity with current contact
                    Intent toMessaging=new Intent(getBaseContext(),MessagingActivity.class);
                    toMessaging.putExtra("ContactIndex",MainActivity.chats.indexOf(contactList.get(i)));
                    startActivity(toMessaging);
                    finish();
                }
            });
        }
    }

    public void onCreateGroup(View v){
        AlertDialog.Builder builder=new AlertDialog.Builder(this,R.style.AlertDialog);
        builder.setTitle("Group Name:");

        final EditText groupNameText=new EditText(this);
        builder.setView(groupNameText);
        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String groupName=groupNameText.getText().toString();
                if(TextUtils.isEmpty(groupName)){
                    Toast.makeText(ChooseChatActivity.this,"Enter Group Name first",Toast.LENGTH_LONG).show();
                }else{
                    CreateNewGroup(groupName);
                    Toast.makeText(ChooseChatActivity.this,"Group Created",Toast.LENGTH_LONG).show();
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });

        builder.show();
    }

    private void CreateNewGroup(String groupName) {
        MainActivity.MaxGroupID++; // new group id
       // HashMap<String,Message> chatHistory=new HashMap<>();
     //   chatHistory.put(FirebaseAuth.getInstance().getCurrentUser().getUid(),new Message("I create this new group","firstSender")); // add first message by me
        Chat groupChat=new Chat(groupName,String.valueOf(MainActivity.MaxGroupID),true);
        DatabaseReference rootRef= FirebaseDatabase.getInstance().getReference();
        //update in all groups
        rootRef.child("Groups").child(String.valueOf(MainActivity.MaxGroupID)).setValue(groupChat);
        // update in user's groups
        rootRef.child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Chats").child(String.valueOf(MainActivity.MaxGroupID)).setValue(groupChat);
        //update MaxID
        rootRef.child("Groups").child("MaxID").setValue(MainActivity.MaxGroupID);

        //chats is updated in onDataChange


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
