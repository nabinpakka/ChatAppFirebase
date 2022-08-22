package com.example.dell.fireapp;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private String mChat_user_id,user_name;
    private String mCurrent_user_id,thumbnail;


    private Toolbar mToolbar;

    public DatabaseReference mRootRef;
    private FirebaseAuth mAuth;

    private TextView mTitleView;
    private TextView mLastSeenView;
    private CircleImageView mProfileImageView;

    private EditText mChat_message_edit_text;
    private Button mAdd_btn,mSend_message_btn;

    private RecyclerView mMessagesList;

    private List<Messages> messagesList=new ArrayList<>();
    private LinearLayoutManager mLinearLayout;
    private MessageAdapter mAdapter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mRootRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        mChat_user_id = getIntent().getStringExtra("user_id");
        user_name = getIntent().getStringExtra("user_name");
        thumbnail = getIntent().getStringExtra("thumb_nail");

        mToolbar = (Toolbar) findViewById(R.id.chat_appbar);
        setSupportActionBar(mToolbar);

        ActionBar actionBar = getSupportActionBar();

        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = inflater.inflate(R.layout.chat_custom_action_bar, null);
        actionBar.setCustomView(action_bar_view);

        //---------------custom Aciton bar dealing----------//
        mTitleView = (TextView) findViewById(R.id.custon_app_bar_title);
        mLastSeenView = (TextView) findViewById(R.id.last_seen_status);
        mProfileImageView = (CircleImageView) findViewById(R.id.custom_bar_image);

        mAdd_btn = (Button) findViewById(R.id.chat_add_btn);
        mSend_message_btn = (Button) findViewById(R.id.chat_send_btn);
        mChat_message_edit_text = (EditText) findViewById(R.id.chat_message_view);

        mAdapter = new MessageAdapter(messagesList,thumbnail,ChatActivity.this);

        mMessagesList = (RecyclerView) findViewById(R.id.message_list);

        mLinearLayout = new LinearLayoutManager(this);

        mMessagesList.setHasFixedSize(true);
        mMessagesList.setLayoutManager(mLinearLayout);
        mMessagesList.setAdapter(mAdapter);

        mTitleView.setText(user_name);
        Picasso.with(ChatActivity.this).load(thumbnail).placeholder(R.mipmap.ic_profile).into(mProfileImageView);


        mCurrent_user_id = mAuth.getCurrentUser().getUid();

        loadMessages();

        mRootRef.child("Users").child(mChat_user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String online = dataSnapshot.child("online").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();
                Picasso.with(ChatActivity.this).load(image).placeholder(R.mipmap.ic_profile).into(mProfileImageView);

                if (online.equals("true")) {
                    mLastSeenView.setText("online");

                } else {

                    GetTimeAgo getTimeAgo = new GetTimeAgo();
                    long lastTime = Long.parseLong(online);
                    String lastSeen = getTimeAgo.getTimeAgo(lastTime, getApplicationContext());
                    mLastSeenView.setText(lastSeen);


                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

       /* mRootRef.child("Chat").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (!dataSnapshot.hasChild(mChat_user_id)){
                    Map chatAddMap=new HashMap();

                    chatAddMap.put("seen",false);
                    chatAddMap.put("timestamp", ServerValue.TIMESTAMP);

                    Map chatUserMap=new HashMap();
                    chatUserMap.put("Chat/"+mCurrent_user_id+"/"+mChat_user_id,chatAddMap);
                    chatUserMap.put("Chat/"+mChat_user_id+"/"+mCurrent_user_id,chatAddMap);

                    mRootRef.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if (databaseError!=null){
                                Log.d("CHAT ERROR", databaseError.getMessage().toString());
                            }
                        }
                    });
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });*/





        mSend_message_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });




    }


    private void loadMessages() {

        mRootRef.child("messages").child(mCurrent_user_id).child(mChat_user_id).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Messages messages=dataSnapshot.getValue(Messages.class);
                messagesList.add(messages);
                mAdapter.notifyDataSetChanged();

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void sendMessage() {
        final String message=mChat_message_edit_text.getText().toString();
        if (!TextUtils.isEmpty(message)){
            String currentUserRef="messages/"+mCurrent_user_id+"/"+mChat_user_id;
            String chatUserRef="messages/"+mChat_user_id+"/"+mCurrent_user_id;

            DatabaseReference user_message_push=mRootRef.child("messages").child(mCurrent_user_id).child(mChat_user_id).push();
            String push_id=user_message_push.getKey();

            Map messageMap=new HashMap();
            messageMap.put("message",message);
            messageMap.put("type","text");
            messageMap.put("seen",false);
            messageMap.put("time",ServerValue.TIMESTAMP);
            messageMap.put("from",mCurrent_user_id);

            Map messageUserMap=new HashMap();
            messageUserMap.put(currentUserRef+"/"+push_id,messageMap);
            messageUserMap.put(chatUserRef+"/"+push_id,messageMap);

            mChat_message_edit_text.setText("");

            mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                    mRootRef.child("Chats").addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                            Map chatAddMap=new HashMap();
                            chatAddMap.put("last_meaasge",message);
                            // chatAddMap.put("seen",false);
                            chatAddMap.put("timestamp", ServerValue.TIMESTAMP);

                            Map chatUserMap=new HashMap();
                            chatUserMap.put("Chats/"+mCurrent_user_id+"/"+mChat_user_id,chatAddMap);
                            chatUserMap.put("Chats/"+mChat_user_id+"/"+mCurrent_user_id,chatAddMap);

                            mRootRef.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                    if (databaseError!=null){
                                        Log.d("CHAT ERROR", databaseError.getMessage().toString());


                                    }
                                }

                            });

                        }

                        @Override
                        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                        }

                        @Override
                        public void onChildRemoved(DataSnapshot dataSnapshot) {

                        }

                        @Override
                        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            });


        }
    }

    public void updateChat(final String message){
        mRootRef.child("Chats").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (!dataSnapshot.hasChild(mChat_user_id)){
                    Map chatAddMap=new HashMap();
                    chatAddMap.put("last_meaasge",message);
                   // chatAddMap.put("seen",false);
                   // chatAddMap.put("timestamp", ServerValue.TIMESTAMP);

                    Map chatUserMap=new HashMap();
                    chatUserMap.put("Chats/"+mCurrent_user_id+"/"+mChat_user_id,chatAddMap);
                    chatUserMap.put("Chats/"+mChat_user_id+"/"+mCurrent_user_id,chatAddMap);

                    mRootRef.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if (databaseError!=null){
                                Log.d("CHAT ERROR", databaseError.getMessage().toString());
                            }
                        }
                    });
                }else{
                    Map chatAddMap=new HashMap();
                    chatAddMap.put("last_meaasge",message);
                   // chatAddMap.put("seen",false);
                   // chatAddMap.put("timestamp", ServerValue.TIMESTAMP);

                    Map chatUserMap=new HashMap();
                    chatUserMap.put("Chats/"+mCurrent_user_id+"/"+mChat_user_id,chatAddMap);
                    chatUserMap.put("Chats/"+mChat_user_id+"/"+mCurrent_user_id,chatAddMap);

                    mRootRef.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if (databaseError!=null){
                                Log.d("CHAT ERROR", databaseError.getMessage().toString());


                            }
                        }

                    });
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });




    }

}
