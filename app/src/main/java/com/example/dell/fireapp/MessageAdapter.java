package com.example.dell.fireapp;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by DELL on 10/31/2017.
 */

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<Messages> mMessageList;
    String thumb_image;
    private FirebaseAuth mAuth;
    Context context;

    public MessageAdapter(List<Messages> mMessageList,String thumb_image,Context context) {
        this.mMessageList = mMessageList;
        this.context=context;
        this.thumb_image=thumb_image;
    }


    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent,int viewType){
        View v= LayoutInflater.from(parent.getContext()).inflate(R.layout.item_single_message_layout,parent,false);

        return new MessageViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MessageViewHolder holder, int position) {

        mAuth=FirebaseAuth.getInstance();

        String current_uid=mAuth.getCurrentUser().getUid();

        Messages c=mMessageList.get(position);
        String from=c.getFrom();

        if (from.equals(current_uid)){
            holder.mProfileImage.setVisibility(View.GONE);
            holder.message_mine_text_view.setText(c.getMessage());
            holder.message_other_text_view.setVisibility(View.GONE);
        }else {
            Picasso.with(context).load(thumb_image).placeholder(R.mipmap.ic_profile).into(holder.mProfileImage);
            holder.message_other_text_view.setText(c.getMessage());
            holder.message_mine_text_view.setVisibility(View.GONE);

        }



    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }


    public  class MessageViewHolder extends RecyclerView.ViewHolder{
         public TextView message_mine_text_view;
        public TextView message_other_text_view;
         public CircleImageView mProfileImage;

        View view;

        public MessageViewHolder(View itemView) {
            super(itemView);
            view =itemView;

            mProfileImage= (CircleImageView) view.findViewById(R.id.message_profile_icon);
            message_mine_text_view= (TextView) view.findViewById(R.id.message_mine_view);
            message_other_text_view= (TextView) view.findViewById(R.id.message_other_view);


        }
    }
}
