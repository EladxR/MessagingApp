package com.example.messagingproject;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class ChatAdapter extends BaseAdapter {
    Activity act;
    List<Chat> chats;
    DatabaseReference usersRoot;

    public ChatAdapter(List<? extends Chat> contacts, Activity act){

        this.act=act;
        chats=new ArrayList<>();
        usersRoot=FirebaseDatabase.getInstance().getReference().child("Users");

    }
    public void UpdateChats(List<Chat> lst){
        chats=lst;
    }


    @Override
    public int getCount() {
        return chats.size();
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
    public View getView(final int i, View view, ViewGroup viewGroup) {
        view=act.getLayoutInflater().inflate(R.layout.contacts_layout,null);
        final ImageView imageView=(ImageView) view.findViewById(R.id.ContactImage);
        TextView textView=(TextView)view.findViewById(R.id.ContactName);


       textView.setText(chats.get(i).getName());

       // default image
        final String imageUrl=chats.get(i).getImage();
        if(imageUrl==null) {
            imageView.setImageResource(R.drawable.contact_image1);
        }else{
            try {
                Picasso.get().load(imageUrl).placeholder(R.drawable.contact_image1).into(imageView);
            }catch (Exception e){
                imageView.setImageResource(R.drawable.contact_image1); // anyway put this image
            }
        }
        if(!chats.get(i).isGroup){ // if its private chat
            // set image from user's info
            DatabaseReference imageRef=usersRoot.child(chats.get(i).getId()).child("profileImage");
            imageRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()){
                        Picasso.get().load((String) snapshot.getValue()).placeholder(R.drawable.contact_image1).into(imageView);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }


        return view;
    }
}
