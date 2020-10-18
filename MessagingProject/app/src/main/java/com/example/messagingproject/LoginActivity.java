package com.example.messagingproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

public class LoginActivity extends AppCompatActivity {
    private FirebaseUser user;
    private FirebaseAuth mAuth;
    private EditText userEmail,userPassword;

    private ProgressDialog loadingBar;

    public static Activity LastLoginActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        userEmail=findViewById(R.id.inputEmail);
        userPassword=findViewById(R.id.inputPassword);

        mAuth=FirebaseAuth.getInstance();
        user=mAuth.getCurrentUser();

        loadingBar=new ProgressDialog(this);

    }

    protected void onStart() {
        super.onStart();
        LastLoginActivity=this;

        if(user!=null){
            ToMainActivity();
            finish();
        }

    }

    private void ToMainActivity() {
        Intent intent=new Intent(this,MainActivity.class);
        startActivity(intent);
    }

    public void onLogin(View v){
        String email=userEmail.getText().toString();
        String password=userPassword.getText().toString();

        if(TextUtils.isEmpty(email)){
            Toast.makeText(this,"enter email..",Toast.LENGTH_SHORT).show();
            return;
        }
        if(TextUtils.isEmpty(password)){
            Toast.makeText(this,"enter password..",Toast.LENGTH_SHORT).show();
            return;
        }
        //no empty fields
        loadingBar.setTitle("Logging in..");
        loadingBar.setCanceledOnTouchOutside(true);
        loadingBar.show();

        mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                loadingBar.dismiss();
                if(task.isSuccessful()){
                    SaveToken();
                    ToMainActivity();
                    finish();
                }else{
                    Toast.makeText(LoginActivity.this,"Error: "+task.getException().toString(),Toast.LENGTH_LONG).show();
                }
            }
        });


    }

    private void SaveToken() {
        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
            @Override
            public void onComplete(@NonNull Task<InstanceIdResult> task) {
                if(task.isSuccessful()) {
                    String deviceToken = task.getResult().getToken();
                    user=mAuth.getCurrentUser();
                    DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(user.getUid());
                    userRef.child("deviceToken").setValue(deviceToken);
                }else{
                    Toast.makeText(LoginActivity.this,"error save token",Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void onToRegister(View v){
        Intent intent=new Intent(this,RegisterActivity.class);
        startActivity(intent);
    }

    public void onToPhoneLogin(View view) {
        Intent intent=new Intent(this,PhoneLoginActivity.class);
        startActivity(intent);
    }
}