package com.example.dell.fireapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

/* add to menifest <activity android:name="com.theartofdev.edmodo.cropper.CropImageActivity"
        android:theme="@style/Base.Theme.AppCompat"/>*/

public class MainActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    Toolbar mToolbar;

    private DatabaseReference mUserRef;

    private ViewPager mViewPager;
    private SectionPagerAdapter mSectionpagerAdapter;

    private TabLayout mTablayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth=FirebaseAuth.getInstance();
        if(mAuth.getCurrentUser()!=null) {
            mUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());
        }

        mToolbar= (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Lapichat App");

        //tabs

        mViewPager= (ViewPager) findViewById(R.id.main_tabPager);
        mSectionpagerAdapter=new SectionPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mSectionpagerAdapter);

        mTablayout= (TabLayout) findViewById(R.id.main_tabs);
        mTablayout.setupWithViewPager(mViewPager);




    }

    @Override
    protected void onStart() {
        super.onStart();

        //checking if any user has sign in or not
        FirebaseUser currentUserId=mAuth.getCurrentUser();

        if (currentUserId==null){
            sendToStart();

        }else {
            mUserRef.child("online").setValue("true");
        }

    }

    @Override
    protected void onStop() {
        super.onStop();

        FirebaseUser currentUser=FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser!=null) {
            mUserRef.child("online").setValue(ServerValue.TIMESTAMP);
            //mUserRef.child("last_seen").setValue(ServerValue.TIMESTAMP);
        }

    }

    private void sendToStart() {
        Intent startIntent=new Intent(MainActivity.this,StartActivity.class);
        startActivity(startIntent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        switch (item.getItemId()){
            case R.id.log_out:
                FirebaseAuth.getInstance().signOut();
                sendToStart();
                break;
            case R.id.setting:
                Intent settingIntent=new Intent(MainActivity.this,SettingsActivity.class);
                startActivity(settingIntent);
                break;
            case R.id.all_user:
                Intent allUserIntent=new Intent(MainActivity.this,UserActivity.class);
                startActivity(allUserIntent);
                break;
        }

        return true;
    }
}
