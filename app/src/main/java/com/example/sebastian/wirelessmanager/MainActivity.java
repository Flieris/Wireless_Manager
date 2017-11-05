/*
 * Copyright (c) Sebastian Lenkiewicz 2017.
 */

package com.example.sebastian.wirelessmanager;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sebastian.wirelessmanager.firebase.LoginActivity;
import com.example.sebastian.wirelessmanager.firebase.SignupActivity;
import com.example.sebastian.wirelessmanager.telephony.TelephonyFragment;
import com.example.sebastian.wirelessmanager.wifi.WifiFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    private ActionBarDrawerToggle mDrawerToggle;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.navigation);
        updateUI(user);
        setupNavigationDrawer();
        drawerLayout.setDrawerListener(mDrawerToggle);
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        FragmentAdapter fragmentAdapter = new FragmentAdapter(getSupportFragmentManager());
        fragmentAdapter.addFragment(new WifiFragment(), "Wi-Fi");
        fragmentAdapter.addFragment(new TelephonyFragment(), "Telephony");
        viewPager.setAdapter(fragmentAdapter);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_id);
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
                    case R.id.wifi_test1:
                        break;
                    case R.id.wifi_test2:
                        break;
                    case R.id.telephony_test1:
                        break;
                    case R.id.telephony_test2:
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
                //invalidateOptionsMenu();
            }
            public void onDrawerOpened(View drawerView){
                super.onDrawerOpened(drawerView);
                //invalidateOptionsMenu();
            }
        };
    }

    private void updateUI(FirebaseUser user){

        View hView = navigationView.getHeaderView(0);
        TextView firebase_status = (TextView)hView.findViewById(R.id.firebase_status);
        if (user != null){
            firebase_status.setText(R.string.firebase_connected);
        } else {
            firebase_status.setText(R.string.firebase_disconnected);
        }

    }
}
