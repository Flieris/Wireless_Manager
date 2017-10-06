package com.example.sebastian.wirelessmanager;

import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v7.widget.Toolbar;

public class MainActivity extends AppCompatActivity {
    private Toolbar toolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //toolbar = (Toolbar)findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);

        ViewPager viewPager = (ViewPager)findViewById(R.id.viewpager);
        FragmentAdapter fragmentAdapter = new FragmentAdapter(getSupportFragmentManager());
        fragmentAdapter.addFragment(new WifiFragment(), "Wi-Fi");
        fragmentAdapter.addFragment(new TelephonyFragment(), "Telephony");
        viewPager.setAdapter(fragmentAdapter);

        TabLayout tabLayout = (TabLayout)findViewById(R.id.tab_id);
        tabLayout.setupWithViewPager(viewPager);
    }
}
