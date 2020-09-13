package com.example.messagingproject;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
    }

    public void onContinue(View view) {
        EditText inputUsername=findViewById(R.id.inputUsernameWelcome);
        String username=inputUsername.getText().toString();
        if(TextUtils.isEmpty(username)){
            Toast.makeText(this,"please enter username",Toast.LENGTH_LONG).show();
        }else{
            SaveUsername(username);
            SendToMainActivity();
        }

    }

    private void SendToMainActivity() {
        Intent mainIntent = new Intent(this, MainActivity.class);
        startActivity(mainIntent);
        finish();
    }

    private void SaveUsername(String username) {
        String userID= FirebaseAuth.getInstance().getCurrentUser().getUid(); // shouldn't be null
        //set username
        FirebaseDatabase.getInstance().getReference().child("Users").child(userID).child("username").setValue(username);
    }
}