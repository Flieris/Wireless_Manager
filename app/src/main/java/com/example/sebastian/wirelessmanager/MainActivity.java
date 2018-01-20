/*
 * Copyright (c) Sebastian Lenkiewicz 2018.
 */

package com.example.sebastian.wirelessmanager;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sebastian.wirelessmanager.firebase.LoginActivity;
import com.example.sebastian.wirelessmanager.firebase.SignupActivity;
import com.example.sebastian.wirelessmanager.telephony.TelephonyFragment;
import com.example.sebastian.wirelessmanager.telephony.heatmap.HeatMapActivity;
import com.example.sebastian.wirelessmanager.wifi.WifiFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.READ_PHONE_STATE;

public class MainActivity extends AppCompatActivity {
    String[] permissionsRequired = new String[]{READ_PHONE_STATE,
    ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION};
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    private static final int REQUEST_MULTIPLE_PERMISSIONS = 322;
    private ActionBarDrawerToggle mDrawerToggle;
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        permissionsCheck();
        mAuth = FirebaseAuth.getInstance();
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation);
        updateUI(user);
        setupNavigationDrawer();
        drawerLayout.setDrawerListener(mDrawerToggle);
        ViewPager viewPager = findViewById(R.id.viewpager);
        FragmentAdapter fragmentAdapter = new FragmentAdapter(getSupportFragmentManager());
        fragmentAdapter.addFragment(new TelephonyFragment(), "Telephony");
        fragmentAdapter.addFragment(new WifiFragment(), "Wi-Fi");
        viewPager.setAdapter(fragmentAdapter);
        TabLayout tabLayout = findViewById(R.id.tab_id);
        tabLayout.setupWithViewPager(viewPager);
    }
    @Override
    public void onStart(){
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
    }

    @Override
    public void onBackPressed(){
        if (drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawers();
            return;
        }
        super.onBackPressed();
    }

    private void setupNavigationDrawer(){
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.firebase_login:
                        Toast.makeText(getApplicationContext(), "Login!",Toast.LENGTH_LONG).show();
                        startActivity(new Intent(MainActivity.this, LoginActivity.class));
                        finish();
                        break;
                    case R.id.firebase_register:
                        Toast.makeText(getApplicationContext(), "Signup!",Toast.LENGTH_LONG).show();
                        startActivity(new Intent(MainActivity.this, SignupActivity.class));
                        finish();
                        break;
                    case R.id.firebase_signOut:
                        FirebaseUser currentUser = mAuth.getCurrentUser();
                        if (currentUser != null) {
                            Toast.makeText(getApplicationContext(), "Sing out!",Toast.LENGTH_LONG).show();
                            FirebaseAuth.getInstance().signOut();
                        }
                        break;
                    case R.id.telephony_heatmap:
                        Toast.makeText(getApplicationContext(), "Telephony heatmap!",Toast.LENGTH_LONG).show();
                        startActivity(new Intent(MainActivity.this, HeatMapActivity.class));
                        finish();
                        break;
                    default:
                        break;
                }
                return true;
            }
        });
        mDrawerToggle = new ActionBarDrawerToggle(this,drawerLayout,
                R.string.drawer_open,R.string.drawer_close){
            public void onDrawerClosed(View view){
                super.onDrawerClosed(view);
            }
            public void onDrawerOpened(View drawerView){
                super.onDrawerOpened(drawerView);
            }
        };
    }

    private void updateUI(FirebaseUser user){

        View hView = navigationView.getHeaderView(0);
        TextView firebase_status = hView.findViewById(R.id.firebase_status);
        ImageView firebase_image = hView.findViewById(R.id.firebase_image);
        if (user != null){
            String email = "Connected User:" + user.getEmail();
            firebase_image.setImageResource(R.drawable.firebase_on);
            firebase_status.setText(email);
        } else {
            firebase_image.setImageResource(R.drawable.firebase_off);
            firebase_status.setText(R.string.firebase_disconnected);
        }
    }

    private void permissionsCheck(){
        if ((ContextCompat.checkSelfPermission(MainActivity.this,permissionsRequired[0]) != PackageManager.PERMISSION_GRANTED)
            ||(ContextCompat.checkSelfPermission(MainActivity.this,permissionsRequired[1]) != PackageManager.PERMISSION_GRANTED)
            || (ContextCompat.checkSelfPermission(MainActivity.this,permissionsRequired[2]) != PackageManager.PERMISSION_GRANTED)){
            if(ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,permissionsRequired[0])
                ||ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,permissionsRequired[1])
                || ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,permissionsRequired[2])){
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Need Multiple Permissions");
                builder.setMessage("This app needs Telephony and Location permissions.");
                builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        ActivityCompat.requestPermissions(MainActivity.this,permissionsRequired,REQUEST_MULTIPLE_PERMISSIONS);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            } else {
                ActivityCompat.requestPermissions(MainActivity.this,permissionsRequired,REQUEST_MULTIPLE_PERMISSIONS);
            }
        } else {
            return;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        switch(requestCode){
            case REQUEST_MULTIPLE_PERMISSIONS:
                if (grantResults.length > 0 ){
                    boolean allgranted = false;
                    for(int i=0;i<grantResults.length;i++){
                        if(grantResults[i]==PackageManager.PERMISSION_GRANTED){
                            allgranted = true;
                        } else {
                            allgranted = false;
                            break;
                        }
                    }
                    if (allgranted){
                        Toast.makeText(getBaseContext(), "We got All Permissions", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getBaseContext(), "Unable to get Permissions", Toast.LENGTH_LONG).show();
                    }
                }
        }
    }
}
