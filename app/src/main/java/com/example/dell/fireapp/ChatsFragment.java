package com.example.dell.fireapp;


import android.content.Context;
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
public class ChatsFragment extends Fragment {

    private View chat_view;
    private RecyclerView chat_list_recycler_view;

    private DatabaseReference mChatsDatabase;
    private FirebaseAuth mAuth;
    private DatabaseReference mUserDatabase;

    String mCurrent_user_id;


    public ChatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        chat_view= inflater.inflate(R.layout.fragment_chats, container, false);



        //firebase database dealing

        mAuth=FirebaseAuth.getInstance();
        mCurrent_user_id=mAuth.getCurrentUser().getUid();
        mChatsDatabase= FirebaseDatabase.getInstance().getReference().child("Chats").child(mCurrent_user_id);
        mChatsDatabase.keepSynced(true);
        mUserDatabase=FirebaseDatabase.getInstance().getReference().child("Users");
        mUserDatabase.keepSynced(true);

        chat_list_recycler_view=(RecyclerView) chat_view.findViewById(R.id.chatlist_recycler_view);
        chat_list_recycler_view.setHasFixedSize(true);
        chat_list_recycler_view.setLayoutManager(new LinearLayoutManager(getContext()));



        return  chat_view;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Chats,FriendsFragment.FriendsViewHolder> friends_recycler_adapter=new FirebaseRecyclerAdapter<Chats, FriendsFragment.FriendsViewHolder>(
                Chats.class,
                R.layout.item_user_list_layout,
                FriendsFragment.FriendsViewHolder.class,
                mChatsDatabase

        ) {
            @Override
            protected void populateViewHolder(final FriendsFragment.FriendsViewHolder viewHolder,Chats model, int position) {
                viewHolder.setDate(model.getLast_meaasge());

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

                                Intent chatIntent=new Intent(getContext(),ChatActivity.class);
                                chatIntent.putExtra("user_id",list_user_id);
                                chatIntent.putExtra("user_name",userNAme);
                                startActivity(chatIntent);
                            }
                        });


                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        };

        chat_list_recycler_view.setAdapter(friends_recycler_adapter);

    }

    public static class ChatsViewHolder extends RecyclerView.ViewHolder{
        View mView;


        public ChatsViewHolder(View itemView) {

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
