package com.example.messagingproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;


public class SendImageActivity extends AppCompatActivity {
    private Message currMessage;
    private ImageView msgImage;
    private EditText inputText;
    private String messageKey;
    private String imageUri;

    //@pre started from messaging activity and currMessageWithImageToSend is set with imageUrl
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_image);

        currMessage=MessagingActivity.currMessageWithImageToSend;
        inputText=findViewById(R.id.inputTextImage);
        msgImage=findViewById(R.id.imageToSend);
        messageKey=getIntent().getStringExtra("messageKey");
        imageUri=getIntent().getStringExtra("imageUri");
        //set image from curr message
        msgImage.setImageURI(Uri.parse(imageUri));
    }

    public void onSendImageButton(View v){
        String message = inputText.getText().toString();
        SendImage(message);


    }

    private void SendImage(String message) {
        //send even if message is empty
        currMessage.setText(message);

        final DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();

        StorageReference userProfileImageRef = MessagingActivity.MsgImagesRef.child(messageKey+MessagingActivity.chatRoot.getKey() + ".jpg"); // msg id + chat id for unique key
        userProfileImageRef.putFile(Uri.parse(imageUri)).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()) {
                    task.getResult().getStorage().getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            final String imageUrl = task.getResult().toString();
                            currMessage.setImage(imageUrl);
                            // from here same as text message
                            Map<String, Object> childUpdates = new HashMap<>();
                            // in groups chat history is saved only in Groups path. in private chats it saves in both users paths.
                            if(MessagingActivity.isGroup) { // save in overall groups
                                childUpdates.put(MessagingActivity.groupPath + messageKey + "/", currMessage);
                            }else {
                                // save in user's chats
                                childUpdates.put(MessagingActivity.userChatPath + messageKey + "/", currMessage);
                                //save in other's chats
                                childUpdates.put(MessagingActivity.otherUserChatPath + messageKey + "/", currMessage);

                                if(MessagingActivity.isOtherChatDelete) {
                                    ChooseChatActivity.CreatePrivateChat(MessagingActivity.chatName, MessagingActivity.chatID);
                                }

                            }
                            rootRef.updateChildren(childUpdates);

                            Toast.makeText(SendImageActivity.this, "image sent", Toast.LENGTH_SHORT).show();

                        }
                    });
                } else {
                    Toast.makeText(SendImageActivity.this, "Error: " + task.getException().toString(), Toast.LENGTH_LONG).show();
                }
            }
        });


        Toast.makeText(SendImageActivity.this, "sending...", Toast.LENGTH_SHORT).show();

        //after sent finish activity
        finish();

    }
}