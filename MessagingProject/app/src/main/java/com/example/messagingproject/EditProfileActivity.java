package com.example.messagingproject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

public class EditProfileActivity extends AppCompatActivity {
    private String username;
    private EditText usernameText;
    private ImageView profileImage;
    private StorageReference profileImageRef;
    private String userID;

    private static final int GalleryRequestCode=1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        username=getIntent().getStringExtra("username");
        usernameText=findViewById(R.id.inputUsernameEditProfile);
        profileImage=findViewById(R.id.editProfileImage);

        profileImageRef=FirebaseStorage.getInstance().getReference().child("profileImages");
        userID= FirebaseAuth.getInstance().getCurrentUser().getUid();

        usernameText.setText(username);
        RetrieveProfileImage();

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
             //   Intent galleryIntent= new Intent();
             //   galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
              //  galleryIntent.setType("image/*");
              //  startActivityForResult(galleryIntent,GalleryRequestCode);
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1,1)
                        .start(EditProfileActivity.this);
            }
        });
    }

    private void RetrieveProfileImage() {
        DatabaseReference userRoot=FirebaseDatabase.getInstance().getReference().child("Users").child(userID);
        userRoot.child("profileImage").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    String imageUrl = (String) dataSnapshot.getValue();
                    //from library - get image from url
                    Picasso.get().load(imageUrl).into(profileImage);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

      //  if(requestCode==GalleryRequestCode && resultCode==RESULT_OK && data!=null){
     //       Uri imageUri=data.getData();
     //       CropImage.activity()
     //               .setGuidelines(CropImageView.Guidelines.ON)
     //               .setAspectRatio(1,1)
     //               .start(EditProfileActivity.this);
     //   }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            Uri resultUri=result.getUri();
            profileImage.setImageURI(resultUri);
            //save image
            StorageReference userProfileImageRef = profileImageRef.child(userID+".jpg");
            userProfileImageRef.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if(task.isSuccessful()){
                        task.getResult().getStorage().getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                final String imageUrl= task.getResult().toString();
                                DatabaseReference userRoot=FirebaseDatabase.getInstance().getReference().child("Users").child(userID);
                                userRoot.child("profileImage").setValue(imageUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            Toast.makeText(EditProfileActivity.this, "image uploaded", Toast.LENGTH_SHORT).show();
                                        }else{
                                            Toast.makeText(EditProfileActivity.this,"Error: "+task.getException().toString(),Toast.LENGTH_LONG).show();

                                        }
                                    }
                                });
                            }
                        });


                    }else{
                        Toast.makeText(EditProfileActivity.this,"Error: "+task.getException().toString(),Toast.LENGTH_LONG).show();
                    }
                }
            });
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //save when activity finished
        SaveProfile();
    }

    private void SaveProfile() {
        DatabaseReference userRoot=FirebaseDatabase.getInstance().getReference().child("Users").child(userID);
        String newUsername=usernameText.getText().toString();

        //save username
        if(TextUtils.isEmpty(newUsername)){
            Toast.makeText(this, "can't save empty fields", Toast.LENGTH_SHORT).show();
        }else{
            //set new user name in data base
            userRoot.child("username").setValue(newUsername);
            Toast.makeText(this, "Profile saved", Toast.LENGTH_SHORT).show();
        }



    }
}