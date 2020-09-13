package com.example.messagingproject;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class EditProfileActivity extends AppCompatActivity {
    private String username;
    private EditText usernameText;
    private ImageView profileImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        username=getIntent().getStringExtra("username");
        usernameText=findViewById(R.id.inputUsernameEditProfile);
        profileImage=findViewById(R.id.editProfileImage);

        usernameText.setText(username);

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //save when activity finished
        SaveProfile();
    }

    private void SaveProfile() {
        String userID= FirebaseAuth.getInstance().getCurrentUser().getUid();
        String newUsername=usernameText.getText().toString();
        if(TextUtils.isEmpty(newUsername)){
            Toast.makeText(this, "can't save empty fields", Toast.LENGTH_SHORT).show();
        }else{
            //set new user name in data base
            FirebaseDatabase.getInstance().getReference().child("Users").child(userID).child("username").setValue(newUsername);
            Toast.makeText(this, "Profile saved", Toast.LENGTH_SHORT).show();
        }

    }
}