package com.example.messagingproject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.internal.Storage;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

public class EditGroupActivity extends AppCompatActivity {
    private String groupName;
    private TextView groupNameText;
    private ImageView groupImage;
    private StorageReference groupImageRef;
    private String groupId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_group);

        groupName=getIntent().getStringExtra("groupName");
        groupNameText=findViewById(R.id.inputGroupNameEditGroup);
        groupImage=findViewById(R.id.editGroupImage);

        groupImageRef= FirebaseStorage.getInstance().getReference().child("profileImages");
        groupId= getIntent().getStringExtra("groupId");

        //set group name in edit text
        groupNameText.setText(groupName);

        setTitle("Edit "+groupName);

        //set the curr image
        String groupImageUrl=getIntent().getStringExtra("groupImage");
        Picasso.get().load(groupImageUrl).placeholder(R.drawable.contact_image1).into(groupImage);

        groupImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1,1)
                        .start(EditGroupActivity.this);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE ) {
            if (data == null) {
                Toast.makeText(this, "no image has been chosen", Toast.LENGTH_LONG).show();
            } else {
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                Uri resultUri = result.getUri();
                groupImage.setImageURI(resultUri);
                //save image
                StorageReference userProfileImageRef = groupImageRef.child(groupId + ".jpg");
                userProfileImageRef.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            task.getResult().getStorage().getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    final String imageUrl = task.getResult().toString();
                                    DatabaseReference groupRoot = FirebaseDatabase.getInstance().getReference().child("Groups").child(groupId);
                                    groupRoot.child("image").setValue(imageUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(EditGroupActivity.this, "image uploaded", Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(EditGroupActivity.this, "Error: " + task.getException().toString(), Toast.LENGTH_LONG).show();

                                            }
                                        }
                                    });
                                }
                            });


                        } else {
                            Toast.makeText(EditGroupActivity.this, "Error: " + task.getException().toString(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //save when activity finished
        SaveGroup();
    }

    private void SaveGroup() {
        String userID=FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference groupRoot=FirebaseDatabase.getInstance().getReference().child("Groups").child(groupId);
        DatabaseReference userGroupRoot=FirebaseDatabase.getInstance().getReference().child("Users").child(userID).child("Chats").child(groupId);
        String newUsername=groupNameText.getText().toString();

        //save username
        if(TextUtils.isEmpty(newUsername)){
            Toast.makeText(this, "can't save empty fields", Toast.LENGTH_SHORT).show();
        }else{
            //set new user name in data base
            groupRoot.child("name").setValue(newUsername);
            //save in both places
            userGroupRoot.child("name").setValue(newUsername);
            Toast.makeText(this, "Profile saved", Toast.LENGTH_SHORT).show();
        }
    }
}