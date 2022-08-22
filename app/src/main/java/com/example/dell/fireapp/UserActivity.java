package com.example.dell.fireapp;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private RecyclerView userListView;

    DatabaseReference mUserDatabaseRefrence;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        mToolbar= (Toolbar) findViewById(R.id.user_appbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("All Users");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mUserDatabaseRefrence= FirebaseDatabase.getInstance().getReference().child("Users");

        mUserDatabaseRefrence.keepSynced(true);

        userListView= (RecyclerView) findViewById(R.id.uesrList_recyclerView);
        userListView.setHasFixedSize(true);
        userListView.setLayoutManager(new LinearLayoutManager(this));

    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Users,UserViewHolder> firebaseRecyclerAdapter=new FirebaseRecyclerAdapter<Users, UserViewHolder>(
                Users.class,
                R.layout.item_user_list_layout,
                UserViewHolder.class,
                mUserDatabaseRefrence

        ) {
            @Override
            protected void populateViewHolder(UserViewHolder viewHolder, final Users model, final int position) {
                viewHolder.setName(model.getName());
                viewHolder.setStatus(model.getStatus());
                viewHolder.setThumbImage(model.getThumb_image(),UserActivity.this);


                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String thumbnail=model.getThumb_image();

                        final String user_id=getRef(position).getKey();
                        Intent profileIntent=new Intent(UserActivity.this,ProfileActivity.class);
                        profileIntent.putExtra("user_id",user_id);
                        profileIntent.putExtra("thumb_image",thumbnail);
                        startActivity(profileIntent);
                    }
                });
            }
        };

        userListView.setAdapter(firebaseRecyclerAdapter);

    }

    public static class UserViewHolder extends RecyclerView.ViewHolder{
        View mView;
        public UserViewHolder(View itemView) {
            super(itemView);
            mView=itemView;
        }

        public void setName(String name) {
            TextView mName= (TextView) mView.findViewById(R.id.user_profile_name);
            mName.setText(name);
        }

        public void setStatus(String status) {
            TextView mStatus= (TextView) mView.findViewById(R.id.user_profile_status);
            mStatus.setText(status);
        }

        public void setThumbImage(String thumbImage, Context applicationContext) {
            CircleImageView image= (CircleImageView) mView.findViewById(R.id.user_profile_image);
            Picasso.with(applicationContext).load(thumbImage).placeholder(R.mipmap.ic_profile).into(image);


        }
    }


}
