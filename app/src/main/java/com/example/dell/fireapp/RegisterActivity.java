package com.example.dell.fireapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {
    EditText reg_name,reg_email,reg_password;
    Button reg_create_btn;

    //firebase Auth
    private FirebaseAuth mAuth;

    private DatabaseReference mDatabase;
    //toolbar
    Toolbar mToolbar;
    //progress dialog
    private ProgressDialog mRegProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        //toolbarsetup
        mToolbar= (Toolbar) findViewById(R.id.register_appbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Create Account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //progress dialog setup
        mRegProgress=new ProgressDialog(RegisterActivity.this);


        //register ui setup
        reg_name= (EditText) findViewById(R.id.register_name_text);
        reg_email= (EditText) findViewById(R.id.register_email_text);
        reg_password= (EditText) findViewById(R.id.register_password_text);

        reg_create_btn= (Button) findViewById(R.id.register_create_btn);
        reg_create_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mName=reg_name.getText().toString();
                String mEmail=reg_email.getText().toString();
                String mPassword=reg_password.getText().toString();

                if (!TextUtils.isEmpty(mName) || !TextUtils.isEmpty(mEmail) || !TextUtils.isEmpty(mPassword)){
                    mRegProgress.setTitle("Creating Account");
                    mRegProgress.setMessage("Please wait until account is created");
                    mRegProgress.setCanceledOnTouchOutside(false);
                    mRegProgress.show();

                    registerAccount(mName,mEmail,mPassword);
                }else {
                    Toast.makeText(RegisterActivity.this,"enter all data",Toast.LENGTH_LONG).show();
                }


            }
        });
    }

    private void registerAccount(final String name, final String email, String password) {
       mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
           @Override
           public void onComplete(@NonNull Task<AuthResult> task) {
               if (task.isSuccessful()){

                   FirebaseUser current_user=FirebaseAuth.getInstance().getCurrentUser();
                   String current_uid=current_user.getUid();

                   mDatabase=FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid);

                   String device_token= FirebaseInstanceId.getInstance().getToken();

                   HashMap<String,String> userMap=new HashMap<String, String>();
                   userMap.put("name",name);
                   userMap.put("status","please update your status");
                   userMap.put("image","default");
                   userMap.put("thumbImage","thumb_default");
                   userMap.put("token",device_token);
                   mDatabase.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                       @Override
                       public void onComplete(@NonNull Task<Void> task) {
                           if (task.isSuccessful()){
                               mRegProgress.dismiss();
                               Intent mainIntent=new Intent(RegisterActivity.this,MainActivity.class);
                               mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                               startActivity(mainIntent);
                               finish();
                           }
                       }
                   });

               }else {
                   mRegProgress.hide();
                   Toast.makeText(RegisterActivity.this,"Connot signin,please signin again",Toast.LENGTH_SHORT).show();
               }
           }
       });
    }
}
