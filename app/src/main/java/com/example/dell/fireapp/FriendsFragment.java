package com.example.dell.fireapp;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class FriendsFragment extends Fragment {

    private View mainView;
    private RecyclerView mRecyclerView;

    private DatabaseReference mFriendsDatabase;
    private FirebaseAuth mAuth;
    private DatabaseReference mUserDatabase;

    String mCurrent_user_id;


    public FriendsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mainView=inflater.inflate(R.layout.fragment_friends, container, false);

        mRecyclerView= (RecyclerView) mainView.findViewById(R.id.friends_recycler_view);

        mAuth=FirebaseAuth.getInstance();

        mCurrent_user_id=mAuth.getCurrentUser().getUid();

        mFriendsDatabase= FirebaseDatabase.getInstance().getReference().child("Friends").child(mCurrent_user_id);
        mFriendsDatabase.keepSynced(true);
        mUserDatabase=FirebaseDatabase.getInstance().getReference().child("Users");
        mUserDatabase.keepSynced(true);

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));



        return mainView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Friends,FriendsViewHolder> friends_recycler_adapter=new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>(
                Friends.class,
                R.layout.item_user_list_layout,
                FriendsViewHolder.class,
                mFriendsDatabase

        ) {
            @Override
            protected void populateViewHolder(final FriendsViewHolder viewHolder, Friends model, int position) {
                viewHolder.setDate(model.getDate());

                final String list_user_id=getRef(position).getKey();

                mUserDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        final String userNAme = dataSnapshot.child("name").getValue().toString();
                        String thumb_image = dataSnapshot.child("thumbImage").getValue().toString();

                        if (dataSnapshot.hasChild("online")){
                            String mOnline= dataSnapshot.child("online").getValue().toString();
                            viewHolder.setOnlineState(mOnline);
                        }


                        viewHolder.setName(userNAme);
                        viewHolder.setThumbImage(thumb_image,getContext());

                        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                CharSequence options[]=new CharSequence[]{"open profile","send message"};

                                AlertDialog.Builder builder=new AlertDialog.Builder(getContext());
                                builder.setTitle("Select Options");
                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        //----------click event for alert dialogue

                                        if (which==0){

                                            Intent profileIntent=new Intent(getContext(),ProfileActivity.class);
                                            profileIntent.putExtra("user_id",list_user_id);
                                            startActivity(profileIntent);

                                        }else if (which==1){

                                            Intent chatIntent=new Intent(getContext(),ChatActivity.class);
                                            chatIntent.putExtra("user_id",list_user_id);
                                            chatIntent.putExtra("user_name",userNAme);
                                            startActivity(chatIntent);

                                        }
                                    }
                                });
                                builder.show();
                            }
                        });


                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        };
        mRecyclerView.setAdapter(friends_recycler_adapter);
    }

    public static class FriendsViewHolder extends RecyclerView.ViewHolder{
        View mView;
        private String name;

        public FriendsViewHolder(View itemView) {

            super(itemView);
            mView=itemView;
        }

        public  void setDate(String date) {
            TextView mDate=(TextView) mView.findViewById(R.id.user_profile_status);
            mDate.setText(date);


        }

        public void setName(String name) {
            TextView mName= (TextView) mView.findViewById(R.id.user_profile_name);
            mName.setText(name);
        }

        public void setThumbImage(String thumbImage, Context applicationContext) {
            CircleImageView image= (CircleImageView) mView.findViewById(R.id.user_profile_image);
            Picasso.with(applicationContext).load(thumbImage).placeholder(R.mipmap.ic_profile).into(image);

        }


        public void setOnlineState(String mOnline) {
            ImageView onLine_icon= (ImageView) mView.findViewById(R.id.online_check_icon);
            if (mOnline.equals("true")){
                onLine_icon.setVisibility(View.VISIBLE);
            }else{
                onLine_icon.setVisibility(View.INVISIBLE);
            }
        }
    }
}
