package com.example.dell.fireapp;

import android.app.ProgressDialog;
import android.icu.text.DateFormat;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Date;
import java.util.HashMap;

public class ProfileActivity extends AppCompatActivity {

    private ImageView profileImage;
    private TextView profileName,profileStatus,profileFriendsCount;
    private Button sendReq_btn,decline_btn;

    DatabaseReference mUserDatabase;
    private DatabaseReference mFriendRequestDatabase;
    private FirebaseUser mCurrentUser;
    private DatabaseReference mFriendDatabase;
    private DatabaseReference mNotificationDatabase;

    ProgressDialog mProgress;

    private String mState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        final String user_uid= getIntent().getStringExtra("user_id");

        mUserDatabase= FirebaseDatabase.getInstance().getReference().child("Users").child(user_uid);
        mFriendRequestDatabase=FirebaseDatabase.getInstance().getReference().child("Friend_Request");
        mFriendDatabase=FirebaseDatabase.getInstance().getReference().child("Friends");
        mNotificationDatabase=FirebaseDatabase.getInstance().getReference().child("Notifications");
        mCurrentUser= FirebaseAuth.getInstance().getCurrentUser();

        profileImage= (ImageView) findViewById(R.id.profileImageView);
        profileName= (TextView) findViewById(R.id.profile_displayName);
        profileStatus= (TextView) findViewById(R.id.profile_status);
        profileFriendsCount= (TextView) findViewById(R.id.profile_totolFriends);

        sendReq_btn= (Button) findViewById(R.id.profile_send_req_btn);
        decline_btn= (Button) findViewById(R.id.profile_decline_btn);

        decline_btn.setVisibility(View.INVISIBLE);
        decline_btn.setEnabled(false);

        mState="not_friends";

        mProgress=new ProgressDialog(ProfileActivity.this);
        mProgress.setTitle("Loading User Data");
        mProgress.setMessage("Please wait while loading");
        mProgress.setCanceledOnTouchOutside(false);
        mProgress.show();


        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name=dataSnapshot.child("name").getValue().toString();
                String status=dataSnapshot.child("status").getValue().toString();
                String image=dataSnapshot.child("image").getValue().toString();

                profileName.setText(name);
                profileStatus.setText(status);

                Picasso.with(ProfileActivity.this).load(image).placeholder(R.mipmap.ic_profile).into(profileImage);

                //-----------FRIENDS LIST / FRIEND REQUEST FEATURE-------------------//
                mFriendRequestDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.hasChild(user_uid)){
                            String req_type=dataSnapshot.child(user_uid).child("Request_Type").getValue().toString();

                            if (req_type.equals("received")){

                                mState="Request_received";
                                sendReq_btn.setText("Accept Friend Request");
                                decline_btn.setVisibility(View.VISIBLE);
                                decline_btn.setEnabled(true);


                            }else if(req_type.equals("sent")){
                                mState="Request_sent";
                                sendReq_btn.setText(R.string.cancel_friend_request);

                            }
                            mProgress.dismiss();

                        }else {
                            mFriendDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.hasChild(user_uid)){
                                        mState="friends";
                                        sendReq_btn.setText("Unfriend this person");
                                    }else {
                                        mState="not_friends";
                                        sendReq_btn.setText("Send Friend Request");
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                            mProgress.dismiss();
                        }


                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        sendReq_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendReq_btn.setEnabled(false);


                //------------not friends--------//

                if (mState.equals("not_friends")){
                    mFriendRequestDatabase.child(mCurrentUser.getUid()).child(user_uid).child("Request_Type").setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull final Task<Void> task) {
                            if (task.isSuccessful()){

                                mFriendRequestDatabase.child(user_uid).child(mCurrentUser.getUid()).child("Request_Type").setValue("received").addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        HashMap<String,String> notificationData=new HashMap<String, String>();
                                        notificationData.put("from",mCurrentUser.getUid());
                                        notificationData.put("type","request");

                                        mNotificationDatabase.child(user_uid).push().setValue(notificationData).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                mState="Request_sent";
                                                sendReq_btn.setText(R.string.cancel_friend_request);
                                                decline_btn.setVisibility(View.INVISIBLE);
                                                decline_btn.setEnabled(false);
                                            }
                                        });



                                        Toast.makeText(ProfileActivity.this,"Sent Friend Request Successfully",Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }else {
                                Toast.makeText(ProfileActivity.this,"Failed to send request,try again later",Toast.LENGTH_SHORT).show();

                            }

                            sendReq_btn.setEnabled(true);
                        }
                    });

                }

                ///----cancel friend request-------/////

                else if (mState.equals("Request_sent")){

                    mFriendRequestDatabase.child(mCurrentUser.getUid()).child(user_uid).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            mFriendRequestDatabase.child(user_uid).child(mCurrentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {


                                    mState="not_friends";
                                    sendReq_btn.setText("Send Friend Request");
                                    decline_btn.setVisibility(View.INVISIBLE);
                                    decline_btn.setEnabled(false);

                                    Toast.makeText(ProfileActivity.this,"Sent Friend Request Successfully",Toast.LENGTH_SHORT).show();
                                }
                            });

                        }

                    });
                }

                //---------------Accept Friends Request--------------//
                else if (mState.equals("Request_received")){

                    final String date= DateFormat.getDateTimeInstance().format(new Date());

                    mFriendDatabase.child(mCurrentUser.getUid()).child(user_uid).child("date").setValue(date).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mFriendDatabase.child(user_uid).child(mCurrentUser.getUid()).child("date").setValue(date).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    mFriendRequestDatabase.child(mCurrentUser.getUid()).child(user_uid).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {

                                            mFriendRequestDatabase.child(user_uid).child(mCurrentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {


                                                    mState="friends";
                                                    sendReq_btn.setText("Unfriend this person");
                                                    decline_btn.setVisibility(View.INVISIBLE);
                                                    decline_btn.setEnabled(false);

                                                    Toast.makeText(ProfileActivity.this,"Now you are friends",Toast.LENGTH_SHORT).show();
                                                }
                                            });

                                        }

                                    });

                                }
                            });
                        }
                    });


                }
                //---------Unfriend feature---------------//
                else if (mState.equals("friends")){
                    mFriendDatabase.child(mCurrentUser.getUid()).child(user_uid).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            mFriendDatabase.child(user_uid).child(mCurrentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    mState="not_friends";
                                    sendReq_btn.setText("Send Friend Request");

                                    Toast.makeText(ProfileActivity.this,"Successfully unFriendes",Toast.LENGTH_SHORT).show();
                                }
                            });

                        }
                    });
                }

            }
        });

        decline_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                mFriendRequestDatabase.child(mCurrentUser.getUid()).child(user_uid).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        mFriendRequestDatabase.child(user_uid).child(mCurrentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {


                                mState="not_friends";
                                sendReq_btn.setText("Send Friend Request");
                                decline_btn.setVisibility(View.INVISIBLE);
                                decline_btn.setEnabled(false);

                                //Toast.makeText(ProfileActivity.this,"",Toast.LENGTH_SHORT).show();
                            }
                        });

                    }

                });

            }
        });






    }
}
