package com.example.dell.fireapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class SettingsActivity extends AppCompatActivity {

    DatabaseReference mDatabase;
    FirebaseAuth mAuth;
    FirebaseUser mCurrentUser;

    private StorageReference mImageStorage;


    //android ui
    TextView nameText,statusText;
    ImageView profileImageView;
    Button change_img_btn,change_status_btn;

    private static final int GALARY_PICK=1;

    Toolbar mToolbar;

    private ProgressDialog mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mToolbar= (Toolbar) findViewById(R.id.setting_appbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Profile Setting");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        nameText= (TextView) findViewById(R.id.userName_text);
        statusText= (TextView) findViewById(R.id.user_status_text);
        profileImageView= (CircleImageView) findViewById(R.id.setting_image);
        change_status_btn= (Button) findViewById(R.id.change_status_btn);
        change_img_btn= (Button) findViewById(R.id.change_image_btn);


        mImageStorage= FirebaseStorage.getInstance().getReference();

        mCurrentUser=FirebaseAuth.getInstance().getCurrentUser();
        String current_uid=mCurrentUser.getUid();

        mDatabase=FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid);
        mDatabase.keepSynced(true);

        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name=dataSnapshot.child("name").getValue().toString();
                String status=dataSnapshot.child("status").getValue().toString();
                final String image=dataSnapshot.child("image").getValue().toString();

                nameText.setText(name);
                statusText.setText(status);

                if (!image.equals("default")){
                    //Picasso.with(SettingsActivity.this).load(image).placeholder(R.mipmap.ic_profile).into(profileImageView);

                    Picasso.with(SettingsActivity.this).load(image).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.mipmap.ic_profile).into(profileImageView, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError() {
                            Picasso.with(SettingsActivity.this).load(image).placeholder(R.mipmap.ic_profile).into(profileImageView);

                        }
                    });
                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        change_status_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String statusData=statusText.getText().toString();
                Intent statusIntent=new Intent(SettingsActivity.this,StatusUpdateActivity.class);
                statusIntent.putExtra("status",statusData);
                startActivity(statusIntent);
            }
        });

        change_img_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galaryIntent=new Intent();
                galaryIntent.setType("image/*");
                galaryIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(galaryIntent,"SELECT IMAGE"),GALARY_PICK);
            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);



        if(requestCode==GALARY_PICK && resultCode==RESULT_OK){
            Toast.makeText(SettingsActivity.this, "photo", Toast.LENGTH_SHORT).show();
            Uri imageUri=data.getData();
            CropImage.activity(imageUri).setAspectRatio(1,1).start(SettingsActivity.this);
           // CropImage.activity(imageUri).start(SettingsActivity.this);
        }


        if (requestCode== CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result=CropImage.getActivityResult(data);

            if (resultCode==RESULT_OK){

                mProgress=new ProgressDialog(SettingsActivity.this);
                mProgress.setTitle("Uploading Image");
                mProgress.setMessage("please wait while uploading");
                mProgress.setCanceledOnTouchOutside(false);
                mProgress.show();


                Uri resultUri=result.getUri();

                File thumb_filePath=new File(resultUri.getPath());

                String current_user_id=mCurrentUser.getUid();

                final Bitmap thumb_bitmap=new Compressor(this)
                        .setMaxWidth(200)
                        .setMaxHeight(200)
                        .setQuality(75)
                        .compressToBitmap(thumb_filePath);

                ByteArrayOutputStream baos=new ByteArrayOutputStream();
                thumb_bitmap.compress(Bitmap.CompressFormat.JPEG,100,baos);
                final byte[] thumb_byte=baos.toByteArray();

                StorageReference filePath=mImageStorage.child("profile_images").child(current_user_id+".jpg");

                final StorageReference thumb_filepath=mImageStorage.child("profile_images").child("thumbs").child(current_user_id+".jpg");

                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()){

                            final String download_uri=task.getResult().getDownloadUrl().toString();

                            UploadTask uploadTask=thumb_filepath.putBytes(thumb_byte);
                            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> thumb_task) {
                                    String thumb_download=thumb_task.getResult().getDownloadUrl().toString();
                                     if (thumb_task.isSuccessful()){

                                         Map upload_hashmap=new HashMap<String, String>();
                                         upload_hashmap.put("image",download_uri);
                                         upload_hashmap.put("thumbImage",thumb_download);

                                         mDatabase.updateChildren(upload_hashmap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                             @Override
                                             public void onComplete(@NonNull Task<Void> task) {

                                                 if (task.isSuccessful()){


                                                        Toast.makeText(SettingsActivity.this,"upload successful",Toast.LENGTH_SHORT).show();
                                                     mProgress.dismiss();
                                                 }
                                             }
                                         });

                                     }else {
                                         mProgress.dismiss();
                                     }
                                }
                            });



                        }else {
                            mProgress.dismiss();
                        }
                    }
                });

            }else if (resultCode==CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE){
                Exception error=result.getError();
            }
        }
    }
}
