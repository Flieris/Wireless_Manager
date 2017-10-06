package com.example.sebastian.wirelessmanager;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

public class FragmentAdapter extends FragmentPagerAdapter {
    private final List<Fragment> fragment_list = new ArrayList<>();
    private final List<String> fragment_names = new ArrayList<>();

    public FragmentAdapter(FragmentManager fm){
        super(fm);
    }
    @Override
    public Fragment getItem(int position) {
        return fragment_list.get(position);
    }

    @Override
    public int getCount() {
        return fragment_list.size();
    }

    public void addFragment(Fragment fragment, String name){
        fragment_list.add(fragment);
        fragment_names.add(name);
    }

    @Override
    public CharSequence getPageTitle(int position){
        return fragment_names.get(position);
    }
}
