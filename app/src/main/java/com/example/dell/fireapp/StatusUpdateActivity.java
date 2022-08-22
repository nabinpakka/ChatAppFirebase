package com.example.dell.fireapp;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class StatusUpdateActivity extends AppCompatActivity {

    Toolbar mToolbar;

    //ui dealing
    private TextInputLayout mStatus;
    private Button mUpdateStatusBtn;

    //firebase
    private DatabaseReference mStatusDatabase;
    private FirebaseUser mCurrentUser;

    //progressDialogue
    private ProgressDialog mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status_update);

        //firebase database
        mCurrentUser= FirebaseAuth.getInstance().getCurrentUser();
        String current_uid=mCurrentUser.getUid();

        mStatusDatabase= FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid);

        //toolbar setup
        mToolbar= (Toolbar) findViewById(R.id.status_toobar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Account Status");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);



        //ui ddealing
        mStatus= (TextInputLayout) findViewById(R.id.status_change_text);
        mUpdateStatusBtn= (Button) findViewById(R.id.update_status_btn);

        String status_value=getIntent().getStringExtra("status");
        mStatus.getEditText().setText(status_value);


        mUpdateStatusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //progress
                mProgress=new ProgressDialog(StatusUpdateActivity.this);
                mProgress.setTitle("Uploading Status");
                mProgress.setMessage("Please wait while status is uploading");
                mProgress.setCanceledOnTouchOutside(false);
                mProgress.show();

                String status=mStatus.getEditText().getText().toString();

                mStatusDatabase.child("status").setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            mProgress.dismiss();
                        }else {
                            mProgress.hide();
                            Toast.makeText(StatusUpdateActivity.this,"Error in uploading status",Toast.LENGTH_LONG).show();
                        }
                    }
                });

            }
        });

    }
}
