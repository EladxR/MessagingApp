package com.example.messagingproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import android.view.LayoutInflater;
import android.view.View;

import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;


public class ChooseChatActivity extends AppCompatActivity {
    ActionBar toolbar;
    RecyclerView newChatRecyclerView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_chat);

        newChatRecyclerView=findViewById(R.id.newChatRecyclerList);

       // toolbar=this.getActionBar();
        toolbar=this.getSupportActionBar();
        toolbar.setDisplayHomeAsUpEnabled(true);
        toolbar.setDisplayShowHomeEnabled(true);
        toolbar.setTitle("Add Chat");

    }

    @Override
    protected void onStart() {
        super.onStart();

        //init recycler view
        FirebaseRecyclerOptions<Contact> options=new FirebaseRecyclerOptions.Builder<Contact>()
                .setQuery(FirebaseDatabase.getInstance().getReference().child("Users"), Contact.class)
                .build();

        FirebaseRecyclerAdapter<Contact,NewChatViewHolder> adapter=new FirebaseRecyclerAdapter<Contact, NewChatViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull NewChatViewHolder holder, final int position, @NonNull final Contact model) {
                holder.name.setText(model.getUsername());
                Picasso.get().load(model.getProfileImage()).placeholder(R.drawable.contact_image1).into(holder.profileImage);

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        CreatePrivateChat(model.getUsername(),getRef(position).getKey()); // anyway create the private chat (even if it is already exist)
                        ToMessagingActivity(model,getRef(position).getKey());
                    }
                });

            }

            @NonNull
            @Override
            public NewChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.contacts_layout, parent,false);
                NewChatViewHolder viewHolder=new NewChatViewHolder(view);

                return viewHolder;
            }
        };
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        newChatRecyclerView.setLayoutManager(llm);
        newChatRecyclerView.setAdapter(adapter);


       adapter.startListening();



    }

    private void ToMessagingActivity(Contact model, String userID) {
        Intent intent=new Intent(ChooseChatActivity.this,MessagingActivity.class);
        intent.putExtra("ChatID",userID);
        intent.putExtra("ChatName",model.getUsername());
        startActivity(intent);
        finish();
    }

    public static class NewChatViewHolder extends RecyclerView.ViewHolder{
        TextView name;
        ImageView profileImage;

        public NewChatViewHolder(@NonNull View itemView) {
            super(itemView);
            name=itemView.findViewById(R.id.ContactName);
            profileImage=itemView.findViewById(R.id.ContactImage);
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

    public static void CreatePrivateChat(String otherUsername, String otherUserID) {
        String userID=FirebaseAuth.getInstance().getCurrentUser().getUid();
        Chat privateChat=new Chat(otherUsername,otherUserID,false);
        Chat otherPrivateChat=new Chat(MainActivity.username,userID,false);
        //update in other user
        UpdateChatValues(otherUserID,userID,otherPrivateChat);
        // update in this user
        UpdateChatValues(userID,otherUserID,privateChat);

        //chats is updated in onDataChange
    }
    public static void UpdateChatValues(String thisUserID,String otherUserID, Chat chat){ // only update so it wont delete if already exist
        DatabaseReference rootRef= FirebaseDatabase.getInstance().getReference();
        rootRef.child("Users").child(thisUserID).child("Chats").child(otherUserID).child("name").setValue(chat.getName());
        rootRef.child("Users").child(thisUserID).child("Chats").child(otherUserID).child("id").setValue(chat.getId());
        rootRef.child("Users").child(thisUserID).child("Chats").child(otherUserID).child("isGroup").setValue(chat.isGroup());
    }

    @Override
    public void onBackPressed() {
        finish();

    }
}
