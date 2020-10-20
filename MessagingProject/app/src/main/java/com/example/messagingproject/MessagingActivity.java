package com.example.messagingproject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.NavUtils;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MessagingActivity extends AppCompatActivity {
    public static String chatID;
    public static String chatName;
    private String chatImageUrl;
    public static DatabaseReference chatRoot;
    private DatabaseReference groupChatRoot;
    public static String groupPath = "/Groups/";
    public static String userChatPath = "/Users/";
    public static String otherUserChatPath = "/Users/";
    private FirebaseUser user;
    private String username, userID;
    public static boolean isGroup;
    private RecyclerView recyclerViewMessages;
    private static RecyclerMessagesAdapter adapter;
    private LinearLayoutManager recyclerLayoutManager;

    private Button newMessagesBtn;
    private int newMessagesNum=0;
    private TextView newMessagesNumText;
    private boolean didScrolled = false;
    private EditText inputText;
    private ImageButton sendImageBtn;
    private TextWatcher inputTextWatcher;
    public static StorageReference MsgImagesRef;
    private ViewImageFragment imageFragment=null;
    private FrameLayout viewImageFragment;
    public static Message currMessageWithImageToSend;
    public static  boolean isOtherChatDelete=false;

    private static final int GalleryRequestCode=1;

    private Menu chatMenu;


    /** @pre putExtra: ChatID ChatName chatImage */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messaging);
        //manage opened massaging activities
        MainActivity.chatActivities.set(getIntent().getIntExtra("chatIndex",0),this);

        sendImageBtn=findViewById(R.id.sendImageBtn);
        inputText=findViewById(R.id.inputText);
        InitInputTextWatcher();
        inputText.addTextChangedListener(inputTextWatcher);

        sendImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent= new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent,GalleryRequestCode);
            }
        });
        MsgImagesRef= FirebaseStorage.getInstance().getReference().child("msgImages");

        viewImageFragment=findViewById(R.id.viewImageFragment);
        viewImageFragment.setVisibility(View.INVISIBLE);

        newMessagesBtn = findViewById(R.id.newMessagesBtn);
        newMessagesNumText=findViewById(R.id.newMessagesNumText);
        newMessagesBtn.setVisibility(View.INVISIBLE);
        newMessagesNumText.setVisibility(View.INVISIBLE);
        newMessagesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //scroll to bottom
                recyclerViewMessages.scrollToPosition(adapter.chatHistory.size() - 1);
                newMessagesBtn.setVisibility(View.INVISIBLE);
                newMessagesNumText.setVisibility(View.INVISIBLE);
                newMessagesNum=0;
            }
        });



        Intent intent = getIntent();

        //get user
        user = FirebaseAuth.getInstance().getCurrentUser();
        username = MainActivity.username;
        userID = user.getUid();

        //get chat info
         chatName = intent.getStringExtra("ChatName");
         chatID = intent.getStringExtra("ChatID");
         chatImageUrl=intent.getStringExtra("chatImage");
         Log.d("DebugChat",chatName+", "+chatID+", "+chatImageUrl);

        setTitle(chatName);



        //init paths
        groupPath = "/Groups/" + chatID + "/chatHistory/";
        userChatPath = "/Users/" + userID + "/Chats/" + chatID + "/chatHistory/";
        otherUserChatPath = "/Users/" + chatID + "/Chats/" + userID + "/chatHistory/";

        //init chat class
        groupChatRoot = FirebaseDatabase.getInstance().getReference().child("Groups").child(chatID); // if not a group it will be null
        chatRoot = FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Chats").child(chatID);
        chatRoot.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //set isGroup
                if(dataSnapshot.child("isGroup").exists()) {
                    isGroup = (boolean) dataSnapshot.child("isGroup").getValue();
                }else{
                    //if it doesn't exist it cant be group
                    isGroup=false;
                    chatRoot.child("isGroup").setValue(false);
                }
                if(dataSnapshot.child("isOtherDelete").exists()){
                    isOtherChatDelete= (boolean) dataSnapshot.child("isOtherDelete").getValue();
                }
                // init Recycler view
                adapter.initAdapter();

                //if create menu executed before firebase on data change (need to know isGroup first)
                initMenu();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        // init Recycler view
        recyclerViewMessages = findViewById(R.id.RecyclerViewMessages);
        adapter = new RecyclerMessagesAdapter();
        recyclerLayoutManager = new LinearLayoutManager(this);
        recyclerLayoutManager.setStackFromEnd(true);


        recyclerViewMessages.setAdapter(adapter);
        recyclerViewMessages.setLayoutManager(recyclerLayoutManager);
        recyclerViewMessages.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                // scroll to bottom will invisible new messages text
                didScrolled = true;
                int scrollPosition = recyclerLayoutManager.findLastVisibleItemPosition();
                if (scrollPosition >= adapter.chatHistory.size() - 1) {
                    newMessagesBtn.setVisibility(View.INVISIBLE);
                    newMessagesNumText.setVisibility(View.INVISIBLE);
                    newMessagesNum=0;
                }

            }
        });

        //value listener to name and image
        UpdateNameAndImage();


    }

    /**
     *  set the value listener so chat name and image url will be updated as changed in user's chat database
     *  if it is a group, in main activity it updates from group database to user's chat and then this function will execute
      */
    private void UpdateNameAndImage() {
        chatRoot.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // must exits from beginning
                chatName= (String) snapshot.child("name").getValue();
                chatImageUrl= (String) snapshot.child("image").getValue();
                //updates title
                setTitle(chatName);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void InitInputTextWatcher() {
        inputTextWatcher=new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                sendImageBtn.setVisibility(View.VISIBLE);
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    if(charSequence.length()==0){
                        sendImageBtn.setVisibility(View.VISIBLE);
                    }else{
                        sendImageBtn.setVisibility(View.INVISIBLE);
                    }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        };
    }

    private class RecyclerMessagesAdapter extends RecyclerView.Adapter<RecyclerMessagesAdapter.RecyclerViewHolder> {
        private ArrayList<Message> chatHistory;

        public RecyclerMessagesAdapter() {
            chatHistory = new ArrayList<>();
        }
        public void ClearChatHistory(){
            chatHistory = new ArrayList<>();
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public RecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_layout, parent, false);
            RecyclerViewHolder viewHolder = new RecyclerViewHolder(view);

            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerViewHolder holder, int position) {
            final Message currMsg = chatHistory.get(position);
            holder.msgText.setText(currMsg.getText());
            holder.timeText.setText(currMsg.getTime());
            holder.usernameText.setText(currMsg.getSenderUsername());
            //if its image message
            if(currMsg.getType()!=null && currMsg.getType().equals("image")){
                final String imageUrl=currMsg.getImage();
                Picasso.get().load(imageUrl).resize(600,600).centerCrop().into(holder.imageMsg);
                // on image click- Open image in view image fragment
                holder.imageMsg.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //to view image fragment
                        imageFragment=new ViewImageFragment(imageUrl,currMsg.getText());
                        // show fragment
                        getSupportFragmentManager().beginTransaction().replace(R.id.viewImageFragment, imageFragment).commit();
                        viewImageFragment.setVisibility(View.VISIBLE);

                    }
                });
            }else{
                Picasso.get().load(Uri.EMPTY).into(holder.imageMsg);
            }

            CardView cardView = holder.cardView;
            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) cardView.getLayoutParams();
            if (currMsg.getSenderUserID().equals(userID)) { // if i am the sender of this message
                // set cardView to right side
                params.endToEnd = R.id.parent_layout_message;
                params.startToStart = ConstraintLayout.LayoutParams.UNSET;
                cardView.requestLayout();
                //change color
                cardView.setCardBackgroundColor(getResources().getColor(R.color.colorBackgroundMyMessage));

                //no username needed
                holder.usernameText.setText("");
                holder.usernameText.setTextSize(1);
            } else {
                // set cardView to left side
                params.startToStart = R.id.parent_layout_message;
                params.endToEnd = ConstraintLayout.LayoutParams.UNSET;
                cardView.requestLayout();
                //change color
                cardView.setCardBackgroundColor(getResources().getColor((android.R.color.white)));
                holder.usernameText.setTextSize(12);
            }
            if (!isGroup) {
                //no username needed
                holder.usernameText.setText("");
                holder.usernameText.setTextSize(1);
            }

        }

        private void initAdapter() {
            //connect chatHistory to database - on change it wil be updated
            if (isGroup) {
                groupChatRoot.child("chatHistory").addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        if (dataSnapshot.exists()) {
                            AddMessageToChatHistory(dataSnapshot);
                        }
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        if (dataSnapshot.exists()) {

                        }
                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            } else { // it's a private chat
                chatRoot.child("chatHistory").addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        if (dataSnapshot.exists()) {
                            AddMessageToChatHistory(dataSnapshot);
                        }
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        if (dataSnapshot.exists()) {

                        }
                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        }

        private void AddMessageToChatHistory(DataSnapshot dataSnapshot) {
            chatHistory.add(dataSnapshot.getValue(Message.class));
            // update the recycler view
            notifyDataSetChanged();
            //scroll to bottom if its my message
            if (didScrolled) { // only if the user already scrolled so it will start from the bottom
                if (dataSnapshot.getValue(Message.class).getSenderUserID().equals(userID)) {
                    if(chatHistory!=null && chatHistory.size()>=1) {
                        recyclerViewMessages.smoothScrollToPosition(chatHistory.size() - 1);
                    }
                } else {
                    int scrollPosition = recyclerLayoutManager.findLastVisibleItemPosition();
                    //if not bottom or close to bottom show new messages else scroll to bottom
                    if (scrollPosition <= adapter.chatHistory.size() - 4) {
                        newMessagesBtn.setVisibility(View.VISIBLE);
                        newMessagesNumText.setVisibility(View.VISIBLE);
                        newMessagesNum++;
                        newMessagesNumText.setText(String.valueOf(newMessagesNum));
                    } else {
                        //scroll to bottom
                        recyclerViewMessages.scrollToPosition(adapter.chatHistory.size() - 1);
                    }
                }
            }


        }

    @Override
    public int getItemCount() {
        return chatHistory.size();
    }


    private class RecyclerViewHolder extends RecyclerView.ViewHolder {
        private TextView msgText;
        private TextView usernameText;
        private TextView timeText;
        private CardView cardView;
        private ImageView imageMsg;

        public RecyclerViewHolder(@NonNull View itemView) {
            super(itemView);
            msgText = itemView.findViewById(R.id.messageText);
            usernameText = itemView.findViewById(R.id.usernameText);
            timeText = itemView.findViewById(R.id.timeSentText);
            cardView = itemView.findViewById(R.id.cardViewMessage);
            imageMsg=itemView.findViewById(R.id.msgImage);
        }
    }
}




    public void SendButton(View v){
        String message=inputText.getText().toString().trim();
       sendMessage(message);

    }

    private void sendMessage(String message){
        if(!TextUtils.isEmpty(message)) {
            //save in database
            Message currMessage=new Message(message,username,userID);
            currMessage.setType("message");
            DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
            String messageKey=chatRoot.child("chatHistory").push().getKey();
            Map<String, Object> childUpdates = new HashMap<>();
            // in groups chat history is saved only in Groups path. in private chats it saves in both users paths.
            if(isGroup) { // save in overall groups
                childUpdates.put(groupPath + messageKey + "/", currMessage);
            }else {
                // save in user's chats
                childUpdates.put(userChatPath + messageKey + "/", currMessage);
                //save in other's chats
                childUpdates.put(otherUserChatPath + messageKey + "/", currMessage);

                // if other delete this chat (only n private chat)
                if(isOtherChatDelete) {
                    ChooseChatActivity.CreatePrivateChat(chatName, chatID);
                }

            }
            rootRef.updateChildren(childUpdates);

            Toast.makeText(this, "message sent", Toast.LENGTH_SHORT).show();
            //reset text box
            inputText.setText("");


        }else{
            Toast.makeText(this, "can't send empty message", Toast.LENGTH_SHORT).show();
        }
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GalleryRequestCode) {
            if (data == null) {
                Toast.makeText(this, "no image has been chosen", Toast.LENGTH_LONG).show();
            } else {
                Uri imageUri=data.getData();
                if(imageUri!=null) {
                    SendImageMsg(imageUri);
                }else{
                    Toast.makeText(this, "no image has been chosen..", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private void SendImageMsg(Uri imageUri) {
        final Message currMessage=new Message("",username,userID);
        currMessage.setType("image");
        currMessageWithImageToSend=currMessage;
        final DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        final String messageKey=chatRoot.child("chatHistory").push().getKey();
        ToSendImageActivity(messageKey,imageUri);

    }

    private void ToSendImageActivity(String messageKey, Uri imageUri) {
        Intent intent=new Intent(this,SendImageActivity.class);
        intent.putExtra("messageKey",messageKey);
        intent.putExtra("imageUri",imageUri.toString());
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        if (imageFragment!=null) {
            getSupportFragmentManager().beginTransaction().remove(imageFragment).commit();
            viewImageFragment.setVisibility(View.INVISIBLE);
            imageFragment=null;
        } else {
            super.onBackPressed();
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.chat_menu,menu);

        chatMenu=menu;
        initMenu();

        return true;
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);

        switch (item.getItemId()) {
            case R.id.clearChatHistory:
                ClearChatHistory();
                break;
            case R.id.editGroup:
                ToEditGroupActivity();
                break;
            case R.id.inviteToGroup:
                ToChooseInviteGroupActivity();
                break;
            case android.R.id.home:
                //Take me back to the main activity
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }

        return true;
    }

    private void ToChooseInviteGroupActivity() {
        Intent toInvite=new Intent(this,ChooseInviteGroupActivity.class);
        toInvite.putExtra("groupName",chatName);
        toInvite.putExtra("groupId",chatID);
        toInvite.putExtra("groupImage",chatImageUrl);
        startActivity(toInvite);
    }

    private void ToEditGroupActivity() {
        Intent toEdit=new Intent(this,EditGroupActivity.class);
        toEdit.putExtra("groupName",chatName);
        toEdit.putExtra("groupId",chatID);
        toEdit.putExtra("groupImage",chatImageUrl);
        startActivity(toEdit);

    }

    private void ClearChatHistory() {
        //show alert dialog to delete chat history
        final AlertDialog deleteHistoryDialog=new AlertDialog.Builder(this)
                .setTitle("Clear Chat History")
                .setMessage("Are you sre you want to delete ALL chat history?")
                .setPositiveButton("Clear", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //the clear
                        chatRoot.child("chatHistory").removeValue();
                        adapter.ClearChatHistory();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .create();
        deleteHistoryDialog.show();
        chatRoot.child("chatHistory").removeValue();
    }

    private void initMenu() {
        if(chatMenu!=null) {
            //group menu
            MenuItem editGroupItem=chatMenu.findItem(R.id.editGroup);
            MenuItem inviteGroup=chatMenu.findItem(R.id.inviteToGroup);
            // private chat menu
            MenuItem clearHistory=chatMenu.findItem(R.id.clearChatHistory);

            if(!isGroup) {
                editGroupItem.setVisible(false);
                inviteGroup.setVisible(false);
                clearHistory.setVisible(true);
            }else{
                editGroupItem.setVisible(true);
                inviteGroup.setVisible(true);
                clearHistory.setVisible(false);
            }
        }
    }

}