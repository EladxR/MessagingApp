package com.example.messagingproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Debug;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.HashSet;

public class ChooseInviteGroupActivity extends AppCompatActivity {
    private RecyclerView usersRecyclerView; //the recycler view will be the same as ChooseChatActivity but the onItemClick
    private int totalChose; // should equal userIdChose.size()
    private Button btnSend;
    private String groupID;
    private String groupName;
    private String groupImage;

    private HashSet<String> usersIdChose; // contains all usersID to send invitation

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_invite_group);

        usersRecyclerView=findViewById(R.id.usersRecyclerView);
        btnSend=findViewById(R.id.btnSendInvitationGroup);

        usersIdChose=new HashSet<>();
        groupID=getIntent().getStringExtra("groupId");
        groupName=getIntent().getStringExtra("groupName");
        groupImage=getIntent().getStringExtra("groupImage");

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AddTheUserToGroup();
                finish();
            }
        });

    }

    private void AddTheUserToGroup() {
        for (String userID:usersIdChose) {
            DatabaseReference userChatRef=FirebaseDatabase.getInstance().getReference().child("Users").child(userID).child("Chats").child(groupID);
            Chat groupChat=new Chat(groupName,groupID,true,groupImage);

            userChatRef.setValue(groupChat);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        //init recycler view
        FirebaseRecyclerOptions<Contact> options=new FirebaseRecyclerOptions.Builder<Contact>()
                .setQuery(FirebaseDatabase.getInstance().getReference().child("Users"), Contact.class)
                .build();

        FirebaseRecyclerAdapter<Contact, ChooseInviteGroupActivity.usersViewHolder> adapter=new FirebaseRecyclerAdapter<Contact, ChooseInviteGroupActivity.usersViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ChooseInviteGroupActivity.usersViewHolder holder, final int position, @NonNull final Contact model) {
                holder.name.setText(model.getUsername());
                Picasso.get().load(model.getProfileImage()).placeholder(R.drawable.contact_image1).into(holder.profileImage);

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String userID=getRef(position).getKey(); //the key is the userID
                        //check if checked:
                        if(holder.isChecked){
                            totalChose--;
                            view.setBackgroundColor(Color.WHITE);
                            holder.setChecked(false);
                            usersIdChose.remove(userID);
                        }else{
                            totalChose++;
                            view.setBackgroundColor(getResources().getColor(R.color.colorPrimaryLight));
                            holder.setChecked(true);
                            usersIdChose.add(userID);
                        }


                        //btn send enabled only if at least 1 user was chosen
                        if(totalChose>0){
                            btnSend.setEnabled(true);
                        }else{
                            btnSend.setEnabled(false);
                        }

                    }
                });
            }

            @NonNull
            @Override
            public ChooseInviteGroupActivity.usersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.contacts_layout, parent,false);
                ChooseInviteGroupActivity.usersViewHolder viewHolder=new ChooseInviteGroupActivity.usersViewHolder(view);

                return viewHolder;
            }
        };

        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        usersRecyclerView.setLayoutManager(llm);
        usersRecyclerView.setAdapter(adapter);

        adapter.startListening();

    }

    public static class usersViewHolder extends RecyclerView.ViewHolder{
        TextView name;
        ImageView profileImage;
        private boolean isChecked=false;

        public void setChecked(boolean checked) {
            isChecked = checked;
        }

        public boolean isChecked() {
            return isChecked;
        }

        public usersViewHolder(@NonNull View itemView) {
            super(itemView);
            name=itemView.findViewById(R.id.ContactName);
            profileImage=itemView.findViewById(R.id.ContactImage);
        }
    }
}